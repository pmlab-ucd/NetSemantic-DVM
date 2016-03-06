package fu.hao.trust.dvm;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import fu.hao.trust.analysis.Plugin;
import fu.hao.trust.utils.Log;
import fu.hao.trust.utils.Settings;
import patdroid.core.ClassInfo;
import patdroid.core.FieldInfo;
import patdroid.core.MethodInfo;
import patdroid.core.ReflectionClassDetailLoader;
import patdroid.dalvik.Instruction;
import patdroid.smali.SmaliClassDetailLoader;

public class DalvikVM {
	// The nested class to implement singleton
	private static class SingletonHolder {
		private static final DalvikVM instance = new DalvikVM();
	}

	// Get THE instance
	public static final DalvikVM v() {
		return SingletonHolder.instance;
	}

	private final String tag = getClass().toString();

	// Dalvik VM Register Bank
	public class Register {
		ClassInfo type = null;
		// data can be instance of: PrimitiveInfo, DVMObject and any class
		// reflection supports
		Object data = null;
		int count = -1;

		public void copy(Register y) {
			this.type = y.type;
			this.data = y.data;
		}

		public void copy(ClassInfo type, Object data) {
			this.type = type;
			this.data = data;
		}

		public Object getData() {
			return data;
		}

		public String toString() {
			return "reg " + count;
		}

	}

	public class StackFrame {
		MethodInfo method;
		// in theory, pc should not be here
		// but it's easier to implement in our case
		// since we know we do not need pc to cross procedure
		int pc;
		Set<Object> pluginRes;
		DVMObject thisObj;
		private Register[] regs = new Register[65536]; // locals

		StackFrame(MethodInfo method) {
			this.method = method;
			pc = 0;
			retValReg = new Register();
			pluginRes = new HashSet<>();
			
			for (int i = 0; i < regs.length; i++) {
				regs[i] = new Register();
				regs[i].count = i;
			}

			int counter = 0;

			Log.debug(tag, "New method call: " + method);
			for (Instruction ins : method.insns) {
				Log.debug(tag, "[" + counter + "]" + ins.toString());
				counter++;
			}

		}

		public StackFrame clone() {
			StackFrame frame = new StackFrame(method);
			frame.thisObj = thisObj;
			frame.pc = pc;
			frame.pluginRes.addAll(pluginRes);
			
			for (int i = 0; i < regs.length; i++) {
				frame.regs[i].copy(regs[i]);
				if (pluginRes.contains(regs[i])) {
					frame.pluginRes.remove(regs[i]);
					frame.pluginRes.add(frame.regs[i]);
				}
			}
			return frame;
		}
	}
	
	public LinkedList<StackFrame> cloneStack() {
		LinkedList<StackFrame> newStack = new LinkedList<>();
		for (StackFrame frame : stack) {
			newStack.add(frame);
		}
		
		newStack.set(newStack.size() - 1, stack.getLast().clone());
		
		return newStack;
	}

	class Heap {
		Map<ClassInfo, DVMClass> dvmClasses = new HashMap<>();
		Map<ClassInfo, Set<DVMObject>> dvmObjs = new HashMap<>();

		private void setClass(ClassInfo type, DVMClass dvmClass) {
			dvmClasses.put(type, dvmClass);
		}

		private void setClass(DVMClass dvmClass) {
			dvmClasses.put(dvmClass.getType(), dvmClass);
		}
		
		private void setClass(DalvikVM vm, ClassInfo type) {
			Log.debug(tag, "new class representation for " + type + " at " + vm.heap);
			dvmClasses.put(type, new DVMClass(vm, type));
		}

		private DVMClass getClass(DalvikVM vm, ClassInfo type) {
			Log.debug(tag, "getClass " + type + " at " + vm.heap);
			if (!dvmClasses.containsKey(type)) {
				setClass(vm, type);
			}
			return dvmClasses.get(type);
		}

		private void setObj(ClassInfo type, DVMObject dvmObj) {
			if (dvmObjs.get(type) == null) {
				dvmObjs.put(type, new HashSet<DVMObject>());
			}
			dvmObjs.get(type).add(dvmObj);
		}

		public void setObj(DVMObject dvmObj) {
			if (dvmObjs.get(dvmObj.getType()) == null) {
				dvmObjs.put(dvmObj.getType(), new HashSet<DVMObject>());
			}
			dvmObjs.get(dvmObj.getType()).add(dvmObj);
		}
	}

	public State storeState() {
		Heap backHeap = new Heap();

		Map<DVMClass, DVMClass> classMap = new HashMap<>();
		Map<DVMObject, DVMObject> objMap = new HashMap<>();

		// Backup classes.
		for (ClassInfo type : heap.dvmClasses.keySet()) {
			DVMClass oldClass = heap.getClass(this, type);
			DVMClass newClass = oldClass.clone();
			for (String fieldName : newClass.getFields().keySet()) {
				Object field = oldClass.getStatField(fieldName);
				newClass.setStatField(fieldName, backupField(field, objMap));
				classMap.put(oldClass, newClass);
			}
		}

		// Backup objs
		for (ClassInfo type : heap.dvmObjs.keySet()) {
			for (DVMObject obj : heap.dvmObjs.get(type)) {
				DVMObject newObj;
				if (objMap.containsKey(obj)) {
					newObj = objMap.get(obj);
				} else {
					newObj = obj.clone(backHeap);
				}

				// Fix fields.
				for (FieldInfo fieldInfo : newObj.getFields().keySet()) {
					Object field = newObj.getFieldObj(fieldInfo);
					newObj.setField(fieldInfo, backupField(field, objMap));
				}

				newObj.setDvmClass(classMap.get(obj.getDvmClass()));
				newObj.setSuperObj(backupField(obj.getSuperObj(), objMap));
			}
		}

		for (DVMClass dvmClass : classMap.values()) {
			backHeap.setClass(dvmClass);
		}

		for (DVMObject dvmObj : objMap.values()) {
			backHeap.setObj(dvmObj);
		}

		// backup jvm stack
		LinkedList<StackFrame> newStack = cloneStack();

		// backup retValReg
		Register returnReg = new Register();
		returnReg.type = retValReg.type;
		returnReg.data = backupField(retValReg.data, objMap);

		// calling ctx
		Register[] newCTX = null;
		if (callingCtx != null) {
			newCTX = new Register[callingCtx.length];
			for (int i = 0; i < callingCtx.length; i++) {
				newCTX[i].copy(callingCtx[i]);
			}
		}

		// FIXME backup plugin res
		Set<Object> currtRes = new HashSet<>();
		currtRes.addAll(plugin.currtRes);
		Method pluginMethod = plugin.method;

		State state = new State(backHeap, newStack,
				returnReg, newCTX, pc, currtRes, pluginMethod);
		states.add(state);
		return state;
	}

	private Object backupField(Object field, Map<DVMObject, DVMObject> objMap) {
		if (field instanceof DVMObject) {
			DVMObject newField;
			if (objMap.containsKey(field)) {
				newField = objMap.get(field);
			} else {
				newField = new DVMObject(this, ((DVMObject) field).getType());
				objMap.put((DVMObject) field, newField);
			}
			return newField;
		} else {
			// TODO no way to store reflectable objs
			return field;
		}
	}

	Stack<State> states = new Stack<>();

	public State popState() {
		return states.pop();
	}

	public void restoreState() {
		if (states.isEmpty()) {
			return;
		}
		State state = popState();
		heap = state.heap;
		stack = state.stack;
		retValReg = state.retValReg;
		callingCtx = state.callingCtx;
		pc = state.pc;
		plugin.currtRes = state.currtRes;
		plugin.method = state.pluginMethod;
	}

	class State {
		Heap heap;
		int pc;
		LinkedList<StackFrame> stack;

		Register retValReg;

		Register[] callingCtx;

		Set<Object> currtRes;
		Method pluginMethod;

		State(Heap heap, LinkedList<StackFrame> stack, Register retValReg,
				Register[] callingCtx, int pc,
				Set<Object> currRes, Method pluginMethod) {
			this.heap = heap;
			this.stack = stack;
			this.retValReg = retValReg;
			this.callingCtx = callingCtx;
			this.pc = pc;
			this.currtRes = currRes;
			this.pluginMethod = pluginMethod;
		}

	}

	// We directly use underlying jvm who runs this interpreter to manage memory
	Heap heap;
	
	LinkedList<StackFrame> stack;  
	// invoke_parameters p;
	// int[] result = new int[8];
	int pc;
	
	Interpreter interpreter;

	Register test = new Register();
	// which reg store the return value of callee called by this method
	Register retValReg;
	
	private Register[] callingCtx;
	

	Plugin plugin;

	public Register getReg(int i) {
		return stack.getLast().regs[i];
	}

	/**
	 * @Title: getClass
	 * @Description: Get the class in the heap.
	 * @return
	 * @see java.lang.Object#getClass()
	 */
	public DVMClass getClass(ClassInfo type) {
		if (type == null) {
			return null;
		}
		if (heap.getClass(this, type) == null) {
			setClass(type, new DVMClass(this, type));
		}
		return heap.getClass(this, type);
	}

	public void setClass(ClassInfo type, DVMClass dvmClass) {
		heap.setClass(type, dvmClass);
	}

	public void setObj(ClassInfo type, DVMObject dvmObj) {
		heap.setObj(type, dvmObj);
	}

	public Set<DVMObject> getObjs(ClassInfo type) {
		return heap.dvmObjs.get(type);
	}

	public Register[] getContext() {
		return callingCtx;
	}

	public void setContext(int[] is) {
		if (is == null) {
			callingCtx = null;
			return;
		}
		callingCtx = new Register[is.length];
		for (int i = 0; i < is.length; i++) {
			callingCtx[i] = getReg(is[i]);
		}
	}

	public DalvikVM() {
		heap = new Heap();
		interpreter = Interpreter.v();
		retValReg = new Register();
		stack = new LinkedList<>(); 
	}

	public void reset() {
		heap = new Heap();
		interpreter = Interpreter.v();
		retValReg = new Register();
		stack = new LinkedList<>(); 
		if (plugin != null) {
			plugin.reset();
		}
		retValReg.data = null;
		retValReg.type = null;
		pc = 0;
	}

	public Register getReturnReg() {
		return retValReg;
	}
	
	public StackFrame getCurrStackFrame() {
		if (stack.isEmpty()) {
			return null;
		}
		return stack.getLast();
	}

	public StackFrame newStackFrame(MethodInfo mi) {
		StackFrame newStackFrame = new StackFrame(mi);
		pc = 0;

		return newStackFrame;
	}

	/**
	 * @Title: backCallCtx
	 * @Author: Hao Fu
	 * @Description: Restore context before the call
	 * @param
	 * @return void
	 * @throws
	 */
	public void backCallCtx(Register retReg) {
		stack.remove(stack.size() - 1);
		if (!stack.isEmpty()) {
			StackFrame currtStack = stack.getLast();
			pc = currtStack.pc;

			for (Object res : plugin.currtRes) {
				if (res instanceof Register) {
					if (res == retReg) {
						currtStack.pluginRes.add(retValReg);
					}
					continue;
				}
				currtStack.pluginRes.add(res);
			}
			plugin.currtRes = currtStack.pluginRes;

			Log.debug(tag, "pc " + pc + " " + currtStack.pc);
		} else {
			pc = Integer.MAX_VALUE;
			// backtrace to last unknown branch
			restoreState();
			Log.warn(tag, "Backtrace begin!!!");
		}
	}

	ClassLoader loader;

	/**
	 * @Title: runMethod
	 * @Author: hao
	 * @Description: For external usage.
	 * @param @param apk
	 * @param @param className
	 * @param @param main
	 * @param @param plugin
	 * @param @throws ZipException
	 * @param @throws IOException
	 * @return void
	 * @throws
	 */
	public void runMethod(String apk, String className, String main,
			Plugin plugin) throws ZipException, IOException {
		Log.msg(tag, "Begin run " + main + " at " + apk);
		// for normal java run-time classes
		// when a class is not loaded, load it with reflection
		ClassInfo.rootDetailLoader = new ReflectionClassDetailLoader();
		// pick an apk
		ZipFile apkFile;
		apkFile = new ZipFile(new File(apk));
		// load all classes, methods, fields and instructions from an apk
		// we are using smali as the underlying engine
		new SmaliClassDetailLoader(apkFile, true).loadAll();
		// get the class representation for the MainActivity class in the
		// apk
		ClassInfo c = ClassInfo.findClass(className);
		// find all methods with the name "onCreate", most likely there is
		// only one
		MethodInfo[] methods = c.findMethodsHere(main);
		this.plugin = plugin;
		runMethod(methods[0]);
	}

	public void runMethods(String apk, String[] chain, Plugin plugin)
			throws ZipException, IOException {
		// for normal java run-time classes
		// when a class is not loaded, load it with reflection
		ClassInfo.rootDetailLoader = new ReflectionClassDetailLoader();
		// pick an apk
		ZipFile apkFile;
		apkFile = new ZipFile(new File(apk));
		// load all classes, methods, fields and instructions from an apk
		// we are using smali as the underlying engine
		new SmaliClassDetailLoader(apkFile, true).loadAll();
		// get the class representation for the MainActivity class in the
		// apk
		
		Log.debug(tag, "apk " + apkFile + " " + apk);
		
		// find all methods with the name "onCreate", most likely there is
		// only one
		this.plugin = plugin;

		for (int i = 1; i < chain.length; i++) {			
			Settings.suspClass = chain[i].split(":")[0];
			ClassInfo c = ClassInfo.findClass(Settings.suspClass);
			Log.debug(tag, "class " + c);
			MethodInfo[] methods = c.findMethodsHere(chain[i].split(":")[1]);
			Log.warn(tag, "Run chain " + chain[i] + " at " + c);
			// TODO Multiple methods have the same name.
			runMethod(methods[0]);
		}
	}

	/**
	 * @Title: runMethod
	 * @Author: hao
	 * @Description: For internal use
	 * @param @param method
	 * @return void
	 * @throws
	 */
	public void runMethod(MethodInfo method) {
		Log.msg(tag, "RUN Method " + method);

		if (method.insns == null) {
			Log.warn(tag, "Empty body of " + method);
			return;
		}

		interpreter.runMethod(this, method);
	}

}
