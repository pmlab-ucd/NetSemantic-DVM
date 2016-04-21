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
import fu.hao.trust.data.VMFullState;
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

	private final String TAG = getClass().getSimpleName();

	// Dalvik VM Register Bank
	public class Register {
		ClassInfo type = null;
		// data can be instance of: PrimitiveInfo, DVMObject and any class
		// reflection supports
		private Object data = null;
		private int count = -1;
		private StackFrame stackFrame;

		Register(StackFrame stackFrame, int count) {
			this.stackFrame = stackFrame;
			this.count = count;
		}

		public void copy(Register y) {
			this.setType(y.type);
			this.setData(y.getData());
		}

		public void copy(Register y, Map<DVMObject, DVMObject> objMap,
				Map<DVMClass, DVMClass> classMap) {
			this.type = y.type;
			this.data = y.data;

			if (this.data instanceof DVMObject) {
				Log.bb(TAG,
						"backobj for reg " + y.data + " as "
								+ objMap.get(y.data));
				this.data = objMap.get(y.data);
			} else if (this.data instanceof DVMClass) {
				this.data = classMap.get(y.data);
			}
		}

		public Object getData() {
			return data;
		}

		public void setData(Object data) {
			if (this.data != null || this.type != null) {
				setAssigned(new Object[3]);
				getAssigned()[0] = this;
				getAssigned()[1] = this.data;
				getAssigned()[2] = data;
				Log.bb(TAG, "A" + this + " " + data);
			}
			this.data = data;
		}

		public ClassInfo getType() {
			return type;
		}

		public String toString() {
			if (count == -1) {
				return "[Global RetReg]";
			}
			return "[reg " + count + "@" + stackFrame.method.name + "]";
		}

		public void setType(ClassInfo type) {
			this.type = type;
		}

		public StackFrame getStackFrame() {
			return stackFrame;
		}

		public void setStackFrame(StackFrame stackFrame) {
			this.stackFrame = stackFrame;
		}

		public int getIndex() {
			return count;
		}

	}

	public class StackFrame {
		MethodInfo method;
		// in theory, pc should not be here
		// but it's easier to implement in our case
		// since we know we do not need pc to cross procedure
		int[] pc = new int[1];
		Map<Plugin, Map<String, Map<Object, Instruction>>> pluginRes;
		private DVMObject thisObj;
		private Register[] regs = new Register[65536]; // locals
		Register exceptReg; // Register to store exceptional obj.
		private Register thisReg;
		
		public void setThisObj(DVMObject thisObj) {
			this.thisObj = thisObj;
		}
		
		public DVMObject getThisObj() {
			return thisObj;
		}

		StackFrame(MethodInfo method) {
			this.method = method;
			pc[0] = -1;
			pluginRes = new HashMap<>();

			for (int i = 0; i < regs.length; i++) {
				regs[i] = new Register(this, i);
			}

			if (method == null) {
				return;
			}

			int counter = 0;

			Log.bb(TAG, "New method call: " + method);
			for (Instruction ins : method.insns) {
				Log.bb(TAG, "[" + counter + "]" + ins.toString());
				counter++;
			}

			if (getPluginManager() == null) {
				setPluginManager(new PluginManager());
			}

			for (Plugin plugin : getPluginManager().getPlugins()) {
				Map<String, Map<Object, Instruction>> clonedRes = new HashMap<>();
				Map<String, Map<Object, Instruction>> originRes = plugin
						.getCurrtRes();
				for (String tag : originRes.keySet()) {
					Map<Object, Instruction> ores = originRes.get(tag);
					Map<Object, Instruction> cres = new HashMap<>();
					for (Object obj : ores.keySet()) {
						if (obj instanceof Register) {
							continue;
						}
						cres.put(obj, ores.get(obj));
					}
					clonedRes.put(tag, cres);
				}

				pluginRes.put(plugin, clonedRes);
			}

		}

		public Map<Plugin, Map<String, Map<Object, Instruction>>> getPluginRes() {
			return pluginRes;
		}

		public void setPluginRes(
				Map<Plugin, Map<String, Map<Object, Instruction>>> pluginRes) {
			this.pluginRes = new HashMap<>(pluginRes);
		}

		public StackFrame clone(Map<DVMObject, DVMObject> objMap,
				Map<DVMClass, DVMClass> classMap) {
			StackFrame frame = new StackFrame(method);
			frame.thisObj = thisObj;
			frame.pc[0] = pc[0];
			frame.pluginRes = new HashMap<>(pluginRes);

			for (Plugin plugin : pluginRes.keySet()) {
				Map<String, Map<Object, Instruction>> clonedRes = new HashMap<>(
						pluginRes.get(plugin));
				frame.pluginRes.put(plugin, clonedRes);
			}

			// Backup register
			for (int i = 0; i < regs.length; i++) {
				frame.regs[i].copy(regs[i], objMap, classMap);
				if (regs[i].data != null) {
					Log.bb(TAG,
							"BackupReg " + i + " " + frame.regs[i].getData());
				}
				for (Plugin plugin : pluginRes.keySet()) {
					Map<String, Map<Object, Instruction>> clonedRes = frame.pluginRes
							.get(plugin);
					Map<String, Map<Object, Instruction>> oRes = pluginRes
							.get(plugin);
					for (String tag : oRes.keySet()) {
						Map<Object, Instruction> ores = oRes.get(tag);
						Map<Object, Instruction> cres = clonedRes.get(tag);
						if (ores.containsKey(regs[i])) {
							cres.remove(regs[1]);
							cres.put(frame.regs[i], ores.get(regs[i]));
						}
					}
				}
			}

			// Backup heap.
			for (Plugin plugin : pluginRes.keySet()) {
				Map<String, Map<Object, Instruction>> oriRes = pluginRes
						.get(plugin);
				Map<String, Map<Object, Instruction>> clonedRes = frame.pluginRes
						.get(plugin);
				for (String tag : oriRes.keySet()) {
					Map<Object, Instruction> ores = oriRes.get(tag);
					Map<Object, Instruction> cres = clonedRes.get(tag);
					for (Object obj : ores.keySet()) {
						if (obj instanceof DVMClass) {
							cres.remove(obj);
							cres.put(classMap.get(obj), ores.get(obj));
						} else if (obj instanceof DVMObject) {
							cres.remove(obj);
							cres.put(classMap.get(obj), ores.get(obj));
						}
					}
				}
			}
			Log.bb(TAG, "BB " + pluginRes);
			Log.bb(TAG, "BB " + frame.pluginRes);

			return frame;
		}

		public Register getExceptReg() {
			return exceptReg;
		}

		public Instruction getInst(int index) {
			return method.insns[index];
		}

		public int getPC() {
			return pc[0];
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
					Log.bb(TAG, "StackFrame " + method + ", reg " + i + ": "
							+ regs[i].data);
				}
			}
		}

		public Register[] getRegs() {
			return regs;
		}

		public Register getThisReg() {
			return thisReg;
		}

		public void setThisReg(Register thisReg) {
			this.thisReg = thisReg;
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

	@Deprecated
	public VMFullState storeState() {
		Log.warn(
				TAG,
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

		// FIXME Backup plugin res
		Method pluginMethod = getReflectMethod();//pluginManager.getMethod();

		VMFullState state = new VMFullState(backHeap, newStack, getPC(),
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

	@Deprecated
	public LinkedList<BiDirBranch> getBiDirBranches() {
		return bidirBranches;
	}

	@Deprecated
	public void restoreFullState() {
		if (bidirBranches.isEmpty()) {
			return;
		}

		Log.warn(
				TAG,
				"++++++++++++++++++++++++++++++++++++++BackTrace++++++++++++++++++++++++++++++++++++++++++++++");
		Log.msg(TAG, "bidibranches: " + bidirBranches);
		BiDirBranch focusBranch = bidirBranches.removeLast();
		Log.msg(TAG, " bidirBrach: " + focusBranch);
		VMFullState state = focusBranch.getFullState();
		heap = state.getHeap();
		stack = state.getStack();
		getPluginManager().setCurrRes(getCurrStackFrame().pluginRes);
		//pluginManager.setMethod(state.getPluginMethod());

		interpreter.jump(this, focusBranch.getInstructions().iterator().next(),
				false);
		// getCurrStackFrame().pc[0]--;
		getCurrStackFrame().printLocals();

		lastBranch = focusBranch.getInstructions().iterator().next();

		if (focusBranch.getMethod() != getCurrStackFrame().getMethod()) {
			Log.err(TAG, "BackTracing Error! Not the same method! "
					+ focusBranch.getMethod() + " expected, but now is "
					+ getCurrStackFrame().getMethod());
		}

	}

	// We directly use underlying jvm who runs this interpreter to manage memory
	VMHeap heap;

	private LinkedList<StackFrame> stack;
	//private int[] pc; // Point to the position of next instruction
	private int nowPC; // Point to the current instruction.

	// Help to identify the loop.
	@Deprecated
	Instruction lastBranch;

	Interpreter interpreter;

	// which reg store the return value of callee called by this method
	Register retValReg;

	private Register[] callingCtx;

	// The "this" instance of a component.
	DVMObject callbackOwner;

	private PluginManager pluginManager;

	private Object[] assigned;
	
	private Method reflectMethod;

	public Register getReg(int i) {
		return stack.getLast().regs[i];
	}

	public Object[] getAssigned() {
		return assigned;
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
		retValReg = new Register(null, -1);
		stack = new LinkedList<>();
	}

	public void reset() {
		heap = new VMHeap();
		interpreter = Interpreter.v();
		retValReg = new Register(null, -1);
		stack = new LinkedList<>();
		getPluginManager().reset();
		retValReg.data = null;
		retValReg.type = null;
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
			Log.bb(TAG, "New Stack Frame: " + newStackFrame + ", pc "
					+ getCurrStackFrame().pc + " stored. ");
		} else {
			Log.bb(TAG, "New Stack Frame: " + newStackFrame);
		}
		if (pluginManager != null) {
			pluginManager.setCurrRes(newStackFrame == null ? null :newStackFrame.pluginRes);
		}
		stack.add(newStackFrame);
		Log.bb(TAG, "Stack: " + stack);
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
		Log.bb(TAG, "stack " + stack);
		stack.removeLast();
		Log.bb(TAG, "stack " + stack);
	}

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
			PluginManager pluginManager, Object[] params) throws ZipException,
			IOException {
		// for normal java run-time classes
		// when a class is not loaded, load it with reflection
		ClassInfo.rootDetailLoader = new ReflectionClassDetailLoader();
		// pick an apk
		ZipFile apkFile;
		File file = new File(apk);
		Settings.apkName = file.getName() + "_" + className + "_" + main;
		apkFile = new ZipFile(file);		
		Log.msg(TAG, "Begin run " + main + " at " + apk);
		// load all classes, methods, fields and instructions from an apk
		// we are using smali as the underlying engine
		new SmaliClassDetailLoader(apkFile, true).loadAll();
		// get the class representation for the MainActivity class in the
		// apk
		ClassInfo c = ClassInfo.findClass(className);
		// find all methods with the name "onCreate", most likely there is
		// only one
		MethodInfo[] methods = c.findMethodsHere(main);
		this.setPluginManager(pluginManager);
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

		Log.msg(TAG, "apk " + apkFile + " " + apk);

		// find all methods with the name "onCreate", most likely there is
		// only one
		this.setPluginManager(pluginManager);

		for (int i = 1; i < chain.length; i++) {
			Settings.suspClass = chain[i].split(":")[0];
			ClassInfo c = ClassInfo.findClass(Settings.suspClass);
			Log.bb(TAG, "class " + c);
			MethodInfo[] methods = c.findMethodsHere(chain[i].split(":")[1]);
			Log.warn(TAG, "Run chain " + chain[i] + " at " + c);
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
		Log.msg(TAG, "RUN Method " + method);

		if (method.insns == null) {
			Log.warn(TAG, "Empty body of " + method);
			return;
		}

		interpreter.runMethod(this, method);
	}

	public void runMethod(MethodInfo method, Object[] params) {
		if (method == null) {
			Log.err(TAG, "Null Method");
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
		return stack.isEmpty() ? Integer.MAX_VALUE : stack.getLast().getPC();
	}

	public void setPC(int pc) {
		StackTraceElement[] stackTraceElements = Thread.currentThread()
				.getStackTrace();
		StackTraceElement caller = stackTraceElements[2];// maybe this number
															// needs to be
															// corrected
		StackTraceElement caller2 = stackTraceElements[3];
		Log.bb(TAG, caller2.getClassName() + " -> " + caller.getMethodName()
				+ " set pc to " + pc);
		if (getCurrStackFrame() != null) {
			getCurrStackFrame().pc[0] = pc;
		}
	}

	public int getNowPC() {
		return nowPC;
	}

	public void setNowPC(int nowPC) {
		this.nowPC = nowPC;
	}

	public boolean isPass() {
		return interpreter.pass;
	}

	public void setPass(boolean pass) {
		this.interpreter.pass = pass;
	}

	public Method getReflectMethod() {
		return reflectMethod;
	}

	public void setReflectMethod(Method reflectMethod) {
		this.reflectMethod = reflectMethod;
	}

	public void setAssigned(Object[] assigned) {
		this.assigned = assigned;
	}

	public PluginManager getPluginManager() {
		return pluginManager;
	}

	public void setPluginManager(PluginManager pluginManager) {
		this.pluginManager = pluginManager;
	}

}