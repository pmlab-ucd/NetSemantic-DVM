package fu.hao.trust.dvm;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import fu.hao.trust.analysis.Plugin;
import fu.hao.trust.utils.Log;
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

	// ---------------------------------
	/*
	 * generic parameter parser for 35c
	 */
	class invoke_parameters {
		int method_id;
		int reg_count;
		int[] reg_idx = new int[5]; // 0-5 map C-G
	}

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

	public class JVM_STACK_FRAME {
		int[] local_var_table;
		// int[] operand_stack; // dvm leverages reg, not a stack
		MethodInfo method;
		int return_addr;
		long offset;
		long max_stack;
		long max_locals;
		JVM_STACK_FRAME prev_stack;
		// in theory, pc should not be here
		// but it's easier to implement in our case
		// since we know we do not need pc to cross procedure
		int pc;

		JVM_STACK_FRAME(MethodInfo method) {
			this.method = method;
			pc = 0;
			return_val_reg = new Register();
		}
		
		public JVM_STACK_FRAME clone() {
			JVM_STACK_FRAME frame = new JVM_STACK_FRAME(method);
			frame.pc = pc;
			frame.prev_stack = prev_stack;
			return frame;
		}
	}

	class constant_info_st {
		int index;
		int tag;
		int[] base;
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

		private DVMClass getClass(DalvikVM vm, ClassInfo type) {
			Log.debug(tag, "getClass " + type);
			if (!dvmClasses.containsKey(type)) {
				dvmClasses.put(type, new DVMClass(vm, type));
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
		
		// backup regs
		Register[] newRegs = new Register[65536];
		for (int i = 0; i < newRegs.length; i++) {
			newRegs[i] = new Register();
			newRegs[i].copy(regs[i]);
			newRegs[i].count = i;
			if (newRegs[i].data instanceof DVMObject) {
				newRegs[i].data = objMap.get(regs[i].data);
			} else if (newRegs[i].data instanceof DVMClass) {
				newRegs[i].data = classMap.get(regs[i].data);
			}
		}
		
		Log.debug(tag, "new Reg[0" + newRegs[0]);
		Log.debug(tag, "old regs[0]" + regs[0]);
		
		// backup jvm stack
		JVM_STACK_FRAME newFrame = curr_jvm_stack.clone();
		
		// backup return_val_reg
		Register returnReg = new Register(); 
		returnReg.type = return_val_reg.type;
		returnReg.data = backupField(return_val_reg.data, objMap);
		
		// backup this obj
		DVMObject newThis = objMap.get(thisObj);
		
		// calling ctx
		int[] newCTX = null;
		if (calling_ctx != null) {
			newCTX = new int[calling_ctx.length];
			for (int i = 0; i < calling_ctx.length; i++) {
				newCTX[i] = calling_ctx[i];
			}
		}
		
		// backup plugin res 
		Set<Object> currtRes = new HashSet<>(); 
		currtRes.addAll(plugin.currtRes);
		Method pluginMethod = plugin.method;
		
		State state = new State(backHeap, newRegs, newFrame, jvm_stack_depth, returnReg, newCTX, newThis, pc, currtRes, pluginMethod);
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
		regs = state.regs;
		Log.debug(tag, "reg[0] " + regs[0]);
		curr_jvm_stack = state.curr_jvm_stack;
		return_val_reg = state.return_val_reg;
		calling_ctx = state.calling_ctx;
		thisObj = state.thisObj;
		pc = state.pc;
		plugin.currtRes = state.currtRes;
		plugin.method = state.pluginMethod;
	}

	class State {
		Heap heap;
		Register[] regs;  // 32
		// invoke_parameters p;
		// int[] result = new int[8];
		int pc;

		JVM_STACK_FRAME curr_jvm_stack;
		int jvm_stack_depth;

		Register return_val_reg;

		int[] calling_ctx;
		DVMObject thisObj;
		
		Set<Object> currtRes; 
		Method pluginMethod;

		State(Heap heap, Register[] regs,
				JVM_STACK_FRAME curr_jvm_stack, int jvm_stack_depth,
				Register return_val_reg, int[] calling_ctx,
				DVMObject thisObj, int pc, Set<Object> currRes, Method pluginMethod) {
			this.heap = heap;
			this.regs = regs;
			Log.debug(tag, "back reg[0] " + regs[0]);
			this.curr_jvm_stack = curr_jvm_stack;
			this.return_val_reg = return_val_reg;
			this.calling_ctx = calling_ctx;
			this.thisObj = thisObj;
			this.pc = pc;
			this.currtRes = currRes;
			this.pluginMethod = pluginMethod;
		}

	}

	// We directly use underlying jvm who runs this interpreter to manage memory
	Heap heap;
	Register[] regs = new Register[65536]; // 32
	// invoke_parameters p;
	// int[] result = new int[8];
	int pc;

	JVM_STACK_FRAME curr_jvm_stack;
	int jvm_stack_depth = 0;

	Interpreter interpreter;

	Register test = new Register();
	// which reg store the return value of callee called by this method
	Register return_val_reg;

	private int[] calling_ctx;
	DVMObject thisObj;
	// DVMClass thisClass;

	Plugin plugin;

	public Register getReg(int i) {
		return regs[i];
	}

	/**
	 * @Title: getClass
	 * @Description: Get the class in the heap.
	 * @return
	 * @see java.lang.Object#getClass()
	 */
	public DVMClass getClass(ClassInfo type) {
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

	public int[] getContext() {
		return calling_ctx;
	}

	public void setContext(int[] is) {
		calling_ctx = is;
	}

	public DalvikVM() {
		heap = new Heap();
		interpreter = Interpreter.v();
		for (int i = 0; i < regs.length; i++) {
			regs[i] = new Register();
			regs[i].count = i;
		}
		return_val_reg = new Register();
	}

	public void reset() {
		heap = new Heap();
		for (int i = 0; i < regs.length; i++) {
			regs[i].data = null;
			regs[i].type = null;
			regs[i].count = -1;
		}

		return_val_reg.data = null;
		return_val_reg.type = null;
	}

	public Register getReturnReg() {
		return return_val_reg;
	}

	public JVM_STACK_FRAME newStackFrame(MethodInfo mi) {
		JVM_STACK_FRAME newStackFrame = new JVM_STACK_FRAME(mi);
		// ctx switch
		newStackFrame.prev_stack = curr_jvm_stack;
		curr_jvm_stack = newStackFrame;
		jvm_stack_depth++;
		pc = 0;
		return newStackFrame;
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

	/**
	 * @Title: runMethod
	 * @Author: hao
	 * @Description: For internal usage
	 * @param @param method
	 * @return void
	 * @throws
	 */
	public void runMethod(MethodInfo method) {
		// print all instructions
		int counter = 0;
		for (Instruction ins : method.insns) {
			Log.debug(tag, "opcode: " + ins.opcode + " " + ins.opcode_aux);
			Log.debug(tag, "[" + counter + "]" + ins.toString());
			counter++;
		}

		interpreter.invocation(this, method);
	}

}
