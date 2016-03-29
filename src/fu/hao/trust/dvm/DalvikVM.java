package fu.hao.trust.dvm;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import fu.hao.trust.analysis.Plugin;
import fu.hao.trust.analysis.PluginManager;
import fu.hao.trust.data.VMState;
import fu.hao.trust.solver.BiDirBranch;
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

	private final String tag = getClass().getSimpleName();

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

		public void copy(Register y, Map<DVMObject, DVMObject> objMap,
				Map<DVMClass, DVMClass> classMap) {
			this.type = y.type;
			this.data = y.data;

			if (this.data instanceof DVMObject) {
				Log.bb(tag,
						"backobj for reg " + y.data + " as "
								+ objMap.get(y.data));
				this.data = objMap.get(y.data);
			} else if (this.data instanceof DVMClass) {
				this.data = classMap.get(y.data);
			}
		}

		public void copy(ClassInfo type, Object data) {
			this.type = type;
			this.data = data;
		}

		public Object getData() {
			return data;
		}

		public void setData(Object data) {
			this.data = data;
		}

		public ClassInfo getType() {
			return type;
		}

		public String toString() {
			return "reg " + count + "@" + getCurrStackFrame();
		}

	}

	public class StackFrame {
		MethodInfo method;
		// in theory, pc should not be here
		// but it's easier to implement in our case
		// since we know we do not need pc to cross procedure
		int pc;
		Map<Plugin, Map<Object, Instruction>> pluginRes;
		DVMObject thisObj;
		private Register[] regs = new Register[65536]; // locals
		Register exceptReg; // Register to store exceptional obj.

		StackFrame(MethodInfo method) {
			this.method = method;
			pc = 0;
			retValReg = new Register();
			pluginRes = new HashMap<>();
			
			for (int i = 0; i < regs.length; i++) {
				regs[i] = new Register();
				regs[i].count = i;
			}
			
			if (method == null) {
				return;
			}

			int counter = 0;

			Log.bb(tag, "New method call: " + method);
			for (Instruction ins : method.insns) {
				Log.bb(tag, "[" + counter + "]" + ins.toString());
				counter++;
			}
			
			if (pluginManager == null) {
				pluginManager = new PluginManager();
			}

			for (Plugin plugin : pluginManager.getCurrRes().keySet()) {
				Map<Object, Instruction> clonedRes = new HashMap<>();
				Map<Object, Instruction> originRes = pluginManager.getCurrRes()
						.get(plugin);
				for (Object obj : originRes.keySet()) {
					if (obj instanceof Register) {
						continue;
					}
					clonedRes.put(obj, originRes.get(obj));
				}

				pluginRes.put(plugin, clonedRes);
			}

		}

		public StackFrame clone(Map<DVMObject, DVMObject> objMap,
				Map<DVMClass, DVMClass> classMap) {
			StackFrame frame = new StackFrame(method);
			frame.thisObj = thisObj;
			frame.pc = pc;
			frame.pluginRes = new HashMap<>(pluginRes);

			for (Plugin plugin : pluginRes.keySet()) {
				Map<Object, Instruction> clonedRes = new HashMap<>(
						pluginRes.get(plugin));
				frame.pluginRes.put(plugin, clonedRes);
			}

			// Backup register
			for (int i = 0; i < regs.length; i++) {
				frame.regs[i].copy(regs[i], objMap, classMap);
				if (regs[i].data != null) {
					Log.bb(tag,
							"BackupReg " + i + " " + frame.regs[i].getData());
				}
				for (Plugin plugin : pluginRes.keySet()) {
					Map<Object, Instruction> clonedRes = frame.pluginRes
							.get(plugin);
					if (pluginRes.get(plugin).containsKey(regs[i])) {
						clonedRes.remove(regs[i]);
						clonedRes.put(frame.regs[i],
								pluginRes.get(plugin).get(regs[i]));
					}
				}
			}

			// Backup heap.
			for (Plugin plugin : pluginRes.keySet()) {
				Map<Object, Instruction> oriRes = pluginRes.get(plugin);
				Map<Object, Instruction> clonedRes = frame.pluginRes
						.get(plugin);
				for (Object obj : oriRes.keySet()) {
					if (obj instanceof DVMClass) {
						clonedRes.remove(obj);
						clonedRes.put(classMap.get(obj), oriRes.get(obj));
					} else if (obj instanceof DVMObject) {
						clonedRes.remove(obj);
						clonedRes.put(classMap.get(obj), oriRes.get(obj));
					}
				}
			}
			Log.bb(tag, "BB " + pluginRes);
			Log.bb(tag, "BB " + frame.pluginRes);

			return frame;
		}

		public Register getExceptReg() {
			return exceptReg;
		}

		public Instruction getInst(int index) {
			return method.insns[index];
		}

		public int getPC() {
			return pc;
		}

		public MethodInfo getMethod() {
			return method;
		}

		public String toString() {
			return "StackFrame " + method;
		}

		public void printLocals() {
			for (int i = 0; i < 65536; i++) {
				if (regs[i].data != null) {
					Log.bb(tag, "StackFrame " + method + ", reg " + i + ": "
							+ regs[i].data);
				}
			}
		}

	}

	public LinkedList<StackFrame> cloneStack(Map<DVMObject, DVMObject> objMap,
			Map<DVMClass, DVMClass> classMap) {
		LinkedList<StackFrame> newStack = new LinkedList<>();
		for (StackFrame frame : stack) {
			newStack.add(frame.clone(objMap, classMap));
		}

		// newStack.set(newStack.size() - 1, stack.getLast().clone());

		return newStack;
	}

	public VMState storeState() {
		Log.warn(
				tag,
				"++++++++++++++++++++++++++++++++++++++Store state! +++++++++++++++++++++++++++++++++++++++++++");
		getCurrStackFrame().printLocals();
		VMHeap backHeap = new VMHeap();

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
					objMap.put(obj, newObj);
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

		// Backup jvm stack
		LinkedList<StackFrame> newStack = cloneStack(objMap, classMap);

		// Backup retValReg
		Register returnReg = new Register();
		returnReg.type = retValReg.type;
		returnReg.data = backupField(retValReg.data, objMap);

		// Calling ctx
		Register[] newCTX = null;
		if (callingCtx != null) {
			newCTX = new Register[callingCtx.length];
			for (int i = 0; i < callingCtx.length; i++) {
				newCTX[i] = new Register();
				newCTX[i].copy(callingCtx[i], objMap, classMap);
			}
		}

		// FIXME Backup plugin res
		Method pluginMethod = pluginManager.getMethod();

		VMState state = new VMState(backHeap, newStack, returnReg, newCTX, pc,
				pluginMethod);
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

	// Stack<State> states = new Stack<>();
	/**
	 * @fieldName: unknownCond
	 * @fieldType: Stack<Instruction>
	 * @Description: To store the unknown branches met for this trace.
	 */
	private LinkedList<BiDirBranch> bidirBranches = new LinkedList<>();
	
	public void addBiDirBranch(BiDirBranch branch) {
		bidirBranches.push(branch);
	}
	
	public LinkedList<BiDirBranch> getBiDirBranches() {
		return bidirBranches;
	}
	

	public void restoreState() {
		if (bidirBranches.isEmpty()) {
			pluginManager.setCondition(null);
			return;
		}
		
		Log.warn(
				tag,
				"++++++++++++++++++++++++++++++++++++++BackTrace++++++++++++++++++++++++++++++++++++++++++++++");
		Log.msg(tag, "bidibranches: " + bidirBranches);
		BiDirBranch focusBranch = bidirBranches.removeLast();
		Log.msg(tag, " bidirBrach: " + focusBranch);
		VMState state = focusBranch.getState();
		heap = state.getHeap();
		stack = state.getStack();
		retValReg = state.getRetValReg();
		callingCtx = state.getCallingCtx();
		pc = state.getPC();
		pluginManager.setCurrRes(getCurrStackFrame().pluginRes);
		Log.bb(tag, "Res objs " + pluginManager.getCurrRes());
		pluginManager.setMethod(state.getPluginMethod());
		
		pluginManager.setCondition(focusBranch.getInstruction());
		interpreter.jump(this, focusBranch.getInstruction(), false);
		pc--;
		getCurrStackFrame().pc--;
		getCurrStackFrame().printLocals();
		
		lastBranch = focusBranch.getInstruction();

		if (focusBranch.getMethod() != getCurrStackFrame().getMethod()) {
			Log.err(tag, "BackTracing Error! Not the same method! "
					+ focusBranch.getMethod() + " expected, but now is "
					+ getCurrStackFrame().getMethod());
		}

	}

	// We directly use underlying jvm who runs this interpreter to manage memory
	VMHeap heap;

	private LinkedList<StackFrame> stack;
	private int pc; // Point to the position of next instruction
	private int nowPC; // Point to the current instruction.
	
	// Help identify the loop.
	Instruction lastBranch;

	Interpreter interpreter;

	Register test = new Register();
	// which reg store the return value of callee called by this method
	Register retValReg;

	private Register[] callingCtx;

	// The "this" instance of a component.
	DVMObject callbackOwner;

	PluginManager pluginManager;

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

	public void setCallContext(int[] is) {
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
		heap = new VMHeap();
		interpreter = Interpreter.v();
		retValReg = new Register();
		stack = new LinkedList<>();
	}

	public void reset() {
		heap = new VMHeap();
		interpreter = Interpreter.v();
		retValReg = new Register();
		stack = new LinkedList<>();
		pluginManager.reset();
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
		if (getCurrStackFrame() != null) {
			Log.bb(tag, "New Stack Frame: " + newStackFrame + ", pc " + getCurrStackFrame().pc + " stored. ");
		} else {
			Log.bb(tag, "New Stack Frame: " + newStackFrame);
		}
		pc = 0;
		stack.add(newStackFrame);	
		Log.bb(tag, "Stack: " + stack);
		return newStackFrame;
	}

	/**
	 * @Title: backCallCtx
	 * @Author: Hao Fu
	 * @Description: Restore the context before the call
	 * @param
	 * @return void
	 * @throws
	 */
	public void backCallCtx(Register retReg) {
		Log.bb(tag, "stack " + stack);
		stack.removeLast();
		Log.bb(tag, "stack " + stack);
		if (!stack.isEmpty()) {
			StackFrame currtStack = stack.getLast();
			pc = currtStack.pc;
			Log.msg(tag, "Return to " + pc + "@" + currtStack.method);
			if (pluginManager.getCurrRes() == null
					|| pluginManager.getCurrRes().isEmpty()) {
				return;
			}
			for (Plugin plugin : pluginManager.getCurrRes().keySet()) {
				Map<Object, Instruction> currtRes = pluginManager.getCurrRes()
						.get(plugin);
				Map<Object, Instruction> currtStackRes = currtStack.pluginRes
						.get(plugin);
				Log.bb(tag, "Currt statck res " + currtStackRes);
				for (Object res : currtRes.keySet()) {
					if (res instanceof Register) {
						if (res == retReg) {
							currtStackRes.put(retValReg,
									plugin.currtRes.get(res));
						}
						continue;
					}
					currtStackRes.put(res, plugin.currtRes.get(res));
				}
			}
			pluginManager.setCurrRes(currtStack.pluginRes);		
		} else {
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
	 * @param apk
	 * @param className
	 * @param main
	 * @param plugin
	 * @throws ZipException
	 * @throws IOException
	 * @return void
	 * @throws
	 */
	public void runMethod(String apk, String className, String main,
			PluginManager pluginManager, Object[] params) throws ZipException, IOException {
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
		this.pluginManager = pluginManager;
		if (params == null) {
			runMethod(methods[0]);
		} else {
			runMethod(methods[0], params);
		}
	}

	public void runMethods(String apk, String[] chain,
			PluginManager pluginManager) throws ZipException, IOException {
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

		Log.msg(tag, "apk " + apkFile + " " + apk);

		// find all methods with the name "onCreate", most likely there is
		// only one
		this.pluginManager = pluginManager;

		for (int i = 1; i < chain.length; i++) {
			Settings.suspClass = chain[i].split(":")[0];
			ClassInfo c = ClassInfo.findClass(Settings.suspClass);
			Log.bb(tag, "class " + c);
			MethodInfo[] methods = c.findMethodsHere(chain[i].split(":")[1]);
			Log.warn(tag, "Run chain " + chain[i] + " at " + c);
			// TODO Multiple methods have the same name.
			runMethod(methods[0]);
		}
	}

	public void jump(Instruction inst, boolean then) {
		interpreter.jump(this, inst, then);
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
	
	public void runMethod(MethodInfo method, Object[] params) {
		if (method == null) {
			Log.err(tag, "Null Method");
		}
		StackFrame stackFrame = newStackFrame(null);	
		for (int i = 0; i < params.length; i++) {
			stackFrame.regs[i].data = params[i];
		}
		
		int[] args = new int[params.length];
		for (int i = 0; i < params.length; i++) {
			args[i] = i;
		}
		
		setCallContext(args);
		interpreter.runMethod(this, method);
	}
	
	public int getPC() {
		return pc;
	}
	
	public void setPC(int pc) {
		StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
		StackTraceElement caller = stackTraceElements[2];//maybe this number needs to be corrected
		StackTraceElement caller2 = stackTraceElements[3];
		Log.bb(tag, caller2.getClassName() + " -> " +  caller.getMethodName() + " set pc to " + pc);
		if (getCurrStackFrame() != null) {
			getCurrStackFrame().pc = pc;
		}
		this.pc = pc;
	}
	
	public int getNowPC() {
		return nowPC;
	}
	
	public void setNowPC(int nowPC) {
		this.nowPC = nowPC;
	}

}
