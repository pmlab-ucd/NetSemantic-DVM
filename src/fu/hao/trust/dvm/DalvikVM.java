package fu.hao.trust.dvm;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import android.app.Activity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.location.MyLocationListener;
import android.myclasses.GenInstance;
import android.os.AsyncTask;
import android.view.View;
import android.widget.BaseAdapter;
import fu.hao.trust.analysis.Plugin;
import fu.hao.trust.analysis.PluginManager;
import fu.hao.trust.data.VMFullState;
import fu.hao.trust.solver.BiDirBranch;
import fu.hao.trust.utils.Log;
import fu.hao.trust.utils.Settings;
import patdroid.core.ClassInfo;
import patdroid.core.FieldInfo;
import patdroid.core.MethodInfo;
import patdroid.core.PrimitiveInfo;
import patdroid.core.ReflectionClassDetailLoader;
import patdroid.dalvik.Instruction;
import patdroid.smali.SmaliClassDetailLoader;
import patdroid.util.Pair;

public class DalvikVM {
	private final String TAG = getClass().getSimpleName();

	// Dalvik VM Register Bank
	public class Register {
		// data can be instance of: PrimitiveInfo, DVMObject and any class
		// reflection supports
		private Pair<Object, ClassInfo> value;

		private int count = -1;
		private StackFrame stackFrame;

		public Register(StackFrame stackFrame, int count) {
			this.stackFrame = stackFrame;
			this.count = count;
		}

		public void copy(Register y) {
			if (y.isUsed()) {
				setValue(y.getData(), y.getType());
			}
		}

		public void copy(Register y, Map<DVMObject, DVMObject> objMap,
				Map<DVMClass, DVMClass> classMap) {
			value.setFirst(y.getData());
			value.setSecond(y.getType());

			if (value.getFirst() instanceof DVMObject) {
				Log.bb(TAG,
						"backobj for reg " + y.getData() + " as "
								+ objMap.get(y.getData()));
				value.setFirst(objMap.get(y.getData()));
			} else if (value.getFirst() instanceof DVMClass) {
				value.setFirst(classMap.get(y.getData()));
			}
		}

		public Object getData() {
			return value.getFirst();
		}

		public StackFrame getCallerFrame() {
			if (stack.size() > 1) {
				return stack.get(stack.size() - 2);
			}
			return null;
		}

		public void setValue(Pair<Object, ClassInfo> value) {
			if (value == null) {
				value = null;
				return;
			}
			setValue(value.getFirst(), value.getSecond());
		}

		public void setValue(Object data, ClassInfo type) {
			if (value == null) {
				assigned[0] = -1;
				this.value = new Pair<>(data, type);
				Log.warn(TAG, "ar " + count + ", " + value);
			} else {
				assigned[0] = this;
				Pair<Object, ClassInfo> oldVal = new Pair<>(value.getFirst(),
						value.getSecond());
				getAssigned()[1] = oldVal;
				Pair<Object, ClassInfo> newVal = new Pair<>(data, type);
				getAssigned()[2] = newVal;
				Log.warn(TAG, "ar " + count + " " + oldVal + ", " + newVal);
			}

			value.setFirst(data);
			value.setSecond(type);
		}

		public ClassInfo getType() {
			return value.getSecond();
		}

		public String toString() {
			if (count == -1) {
				return "[Global RetReg]";
			}
			return "[reg " + count + "@"
					+ stackFrame.method.myClass.getShortName() + "/"
					+ stackFrame.method.name + "]";
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

		public void reset() {
			value = null;
		}

		public boolean isUsed() {
			return value == null ? false : true;
		}

	}

	public class StackFrame {
		MethodInfo method;
		// in theory, pc should not be here
		// but it's easier to implement in our case
		// since we know we do not need pc to cross procedure
		// Point to the next instruction should be executed
		int[] pc = new int[1];
		Map<Plugin, Map<String, Map<Object, Instruction>>> pluginRes;
		private DVMObject thisObj;
		private Register[] regs = new Register[65536]; // locals
		Register exceptReg; // Register to store exceptional obj.
		private Register thisReg;
		private Register[] callCtx;
		private ClassInfo myClass;

		private Intent intent;

		public void setThisObj(DVMObject thisObj) {
			this.thisObj = thisObj;
		}

		public DVMObject getThisObj() {
			return thisObj;
		}

		public Register getReg(int index) {
			return regs[index];
		}

		StackFrame(ClassInfo myClass, MethodInfo method) {
			this.method = method;
			pc[0] = -1;
			pluginRes = new HashMap<>();

			for (int i = 0; i < regs.length; i++) {
				regs[i] = new Register(this, i);
			}

			if (method == null || method.insns == null) {
				Log.warn(TAG, "EMPTY BODY!");
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
							if (callingCtx == null) {
								continue;
							}
							for (int i = 0; i < callingCtx.length; i++) {
								if (callingCtx[i] == obj) {
									cres.put(obj, ores.get(obj));
								}
							}
							continue;
						}
						cres.put(obj, ores.get(obj));
					}
					clonedRes.put(tag, cres);
				}

				pluginRes.put(plugin, clonedRes);
			}

			if (callingCtx != null) {
				callCtx = new Register[callingCtx.length];
				for (int i = 0; i < callingCtx.length; i++) {
					callCtx[i] = callingCtx[i];
				}
			}

			this.myClass = myClass;
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
			StackFrame frame = new StackFrame(myClass, method);
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
				if (regs[i].getData() != null) {
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

		public Register[] getRegs() {
			return regs;
		}

		public Register getThisReg() {
			return thisReg;
		}

		public void setThisReg(Register thisReg) {
			this.thisReg = thisReg;
		}

		public Map<Plugin, Map<String, Map<Object, Instruction>>> clonePluginRes() {
			Map<Plugin, Map<String, Map<Object, Instruction>>> pluginRes = new HashMap<>();
			Map<Plugin, Map<String, Map<Object, Instruction>>> currtRes = getCurrStackFrame()
					.getPluginRes();
			for (Plugin plugin : currtRes.keySet()) {
				Map<String, Map<Object, Instruction>> res = new HashMap<>();
				for (String tag : currtRes.get(plugin).keySet()) {
					Map<Object, Instruction> out = new HashMap<>();
					for (Object obj : currtRes.get(plugin).get(tag).keySet()) {
						out.put(obj, currtRes.get(plugin).get(tag).get(obj));
					}
					res.put(tag, out);
				}
				pluginRes.put(plugin, res);
			}

			return pluginRes;
		}

		public Register[] getCallCtx() {
			return callCtx;
		}

		public void setCallCtx(Register[] callCtx) {
			this.callCtx = callCtx;
		}

		public ClassInfo getMyClass() {
			return myClass;
		}

		public void setMyClass(ClassInfo myClass) {
			this.myClass = myClass;
		}

		public Intent getIntent() {
			return intent;
		}

		public void setIntent(Intent intent) {
			this.intent = intent;
		}

		public void printRegs() {
			if (retValReg.isUsed()) {
				Log.bb(TAG, retValReg + ", " + retValReg.getData());
			}
			for (int i = 0; i < regs.length; i++) {
				if (regs[i].isUsed()) {
					Log.bb(TAG, regs[i] + ", " + regs[i].getData());
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

	@Deprecated
	public VMFullState storeState() {
		Log.warn(
				TAG,
				"++++++++++++++++++++++++++++++++++++++Store state! +++++++++++++++++++++++++++++++++++++++++++");
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
		Method pluginMethod = getReflectMethod();// pluginManager.getMethod();

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
				newField = newVMObject(((DVMObject) field).getType());
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
		// pluginManager.setMethod(state.getPluginMethod());

		executor.jump(this, focusBranch.getInstructions().iterator().next(),
				false);
		// getCurrStackFrame().pc[0]--;

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
	// private int[] pc; // Point to the position of next instruction
	private int nowPC; // Point to the current instruction.

	// Help to identify the loop.
	@Deprecated
	Instruction lastBranch;

	Executor executor;

	// which reg store the return value of callee called by this method
	private Register retValReg;

	private Register[] callingCtx;
	private boolean cleanCallingCtx = true;

	// The "this" instance of a component.
	// DVMObject callbackOwner;

	private PluginManager pluginManager;

	// reg, oldVal, newVal
	private Object[] assigned = new Object[3];

	private Method reflectMethod;

	/**
	 * @fieldName: chainThisObj
	 * @fieldType: DVMObject
	 * @Description: To help run chain methods.
	 */
	private DVMObject chainThisObj = null;

	// Store the tmp method info that should be pushed into the stack soon
	@Deprecated
	LinkedList<StackFrame> tmpFrames;

	@Deprecated
	private boolean repeatInst = false;

	private Intent globalIntent;

	public void setGlobalIntent(Intent intent) {
		globalIntent = intent;
	}

	public Intent getGlobalIntent() {
		return globalIntent;
	}

	public Register getReg(int i) {
		return stack.getLast().regs[i];
	}

	public Object[] getAssigned() {
		return assigned;
	}

	private ServicePool servicePool;
	private Activity currtActivity;
	
	private LinkedList<Activity> activityStack;

	/**
	 * @Title: getClass
	 * @Description: Get the class in the heap.
	 * @return
	 * @see java.lang.Object#getClass()
	 */
	public DVMClass getClass(ClassInfo type) {
		if (type == null || type.toString().contains("java.lang")) {
			return null;
		}
		if (heap.getClass(this, type) == null) {
			setClass(type, new DVMClass(this, type));
		}
		Log.bb(Settings.getRuntimeCaller(), "Get DVMClass for type " + type);
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

	public Register[] getCallContext() {
		return getCurrStackFrame().getCallCtx();
	}

	public Register[] getGlobalCallCtx() {
		return callingCtx;
	}

	public void setGlobalCallContext(int[] is) {
		if (is == null) {
			callingCtx = null;
			return;
		}
		callingCtx = new Register[is.length];
		for (int i = 0; i < is.length; i++) {
			callingCtx[i] = getReg(is[i]);
		}
	}

	public void resetCallCtx() {
		if (cleanCallingCtx) {
			callingCtx = null;
		} else {
			cleanCallingCtx = true;
		}
	}

	public void setGlobalCallContext(Register[] regs, boolean clean) {
		callingCtx = regs;
		cleanCallingCtx = clean;
	}

	public DalvikVM(String APK) {
		heap = new VMHeap();
		executor = Executor.v();
		retValReg = new Register(null, -1);
		stack = new LinkedList<>();
		callingCtx = null;
		activityStack = new LinkedList<>();

		try {
			loadAPK(APK);
		} catch (ZipException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Settings.setVM(this);
	}
	
	// TODO
	public Activity getActivity(String activityName) {
		return null;
	}

	public void reset() {
		heap = new VMHeap();
		executor = Executor.v();
		stack = new LinkedList<>();
		getPluginManager().reset();
		retValReg.reset();
		callingCtx = null;
		setChainThisObj(null);
	}

	public Register getReturnReg() {
		return retValReg;
	}

	public StackFrame getCurrStackFrame() {
		if (stack.isEmpty()) {
			return null;
		}
		stack.remove(null);
		return stack.getLast();
	}

	public StackFrame getCallerFrame() {
		if (stack.size() > 1) {
			return stack.get(stack.size() - 2);
		}

		return null;
	}

	public void addStackFrames(LinkedList<StackFrame> frames) {
		if (frames == null) {
			return;
		}
		frames.remove(null);
		Log.bb(TAG, "Add frames " + frames);
		stack.addAll(frames);
	}

	public StackFrame newStackFrame(ClassInfo sitClass, MethodInfo mi) {
		return newStackFrame(sitClass, mi, null, true);
	}

	public StackFrame newStackFrame(ClassInfo sitClass, MethodInfo mi,
			boolean nowAddToStack) {
		return newStackFrame(sitClass, mi, null, nowAddToStack);
	}

	public StackFrame newStackFrame(ClassInfo sitClass, MethodInfo mi,
			Pair<Object, ClassInfo>[] callCtxObjs) {
		return newStackFrame(sitClass, mi, callCtxObjs, true);
	}

	public StackFrame newStackFrame(ClassInfo sitClass, MethodInfo mi,
			Pair<Object, ClassInfo>[] callCtxObjs, boolean nowAddToStack) {
		String TAG = "newStackFrame";
		if (mi == null || mi.insns == null) {
			return null;
		}

		StackFrame newStackFrame = new StackFrame(sitClass, mi);

		if (callCtxObjs != null) {

			Register[] regs = new Register[callCtxObjs.length];

			for (int i = 0; i < regs.length; i++) {
				regs[i] = newTmpRegister();
				regs[i].setValue(callCtxObjs[i]);
			}
			newStackFrame.setCallCtx(regs);
		}

		/*
		 * if (mi.isConstructor()) { for (Instruction inst : mi.insns) { if
		 * (inst.opcode == Instruction.OP_INVOKE_OP) { Object[] extra =
		 * (Object[]) inst.extra; MethodInfo mee = (MethodInfo) extra[0]; if
		 * (mee.isConstructor() && mee.insns != null) { for (Instruction ins :
		 * mee.insns) { if (ins.opcode == Instruction.OP_INVOKE_OP) { Object[]
		 * extra2 = (Object[]) ins.extra; MethodInfo me = (MethodInfo)
		 * extra2[0]; if ((me.paramTypes.length == mi.paramTypes.length &&
		 * me.name.equals(mi.name)) || me.equals(mi)) { boolean detected = true;
		 * for (int i = 0; i < me.paramTypes.length; i++) { if
		 * (!me.paramTypes[i].equals(mi.paramTypes[i])) { detected = false; } }
		 * if (detected) { inst.opcode = Instruction.OP_HALT; Log.warn(TAG,
		 * "Found potentian infinity loop in the constructor!"); } } } } } } } }
		 */

		if (getCurrStackFrame() != null) {
			Log.bb(TAG, "New Stack Frame: " + newStackFrame + ", pc " + getPC()
					+ " stored. ");
		} else {
			Log.bb(TAG, "New Stack Frame: " + newStackFrame);
		}
		if (pluginManager != null) {
			pluginManager.setCurrRes(newStackFrame == null ? null
					: newStackFrame.pluginRes);
		}
		if (nowAddToStack) {
			Log.bb(TAG, "Add frame " + newStackFrame);
			stack.add(newStackFrame);
		}

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
		stack.removeLast();
		callingCtx = null;
		Log.bb(TAG, "stack " + stack);
		Log.bb(TAG, "Back to the last stack frame." + getCurrStackFrame());
	}

	public void loadAPK(String apk) throws ZipException, IOException {
		// for normal java run-time classes
		// when a class is not loaded, load it with reflection
		ClassInfo.rootDetailLoader = new ReflectionClassDetailLoader();
		// pick an apk
		ZipFile apkFile;
		File file = new File(apk);

		apkFile = new ZipFile(file);
		// load all classes, methods, fields and instructions from an apk
		// we are using smali as the underlying engine
		new SmaliClassDetailLoader(apkFile, true).loadAll();
		Activity.xmlViewDefs();
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
		// get the class representation for the MainActivity class in the
		// apk
		ClassInfo c = ClassInfo.findClass(className);
		// find all methods with the name "onCreate", most likely there is
		// only one
		MethodInfo[] methods = c.findMethodsHere(main);
		Log.msg(TAG, "Begin run " + main + " at " + apk);
		this.setPluginManager(pluginManager);

		if (methods.length == 0) {
			MethodInfo[] targets = null;
			ClassInfo clazz = c;
			while (clazz.getSuperClass() != null) {
				clazz = clazz.getSuperClass();
				targets = clazz.findMethodsHere(main);
				if (targets.length != 0) {
					break;
				}
			}
			if (targets == null || targets.length == 0) {
				Log.err(TAG, "Cannot find the method " + main + " at " + c);
			}

			// Instrument inherited non-override call-backs
			MethodInfo onCreate = c.findMethodsHere("onCreate")[0];
			if (onCreate != null) {
				Instruction[] insts = new Instruction[onCreate.insns.length + 1];
				for (int i = 0; i < onCreate.insns.length; i++) {
					Instruction inst = onCreate.insns[i];
					if (inst.opcode == Instruction.OP_RETURN) {
						Instruction instrumented = new Instruction();
						instrumented.opcode = Instruction.OP_INVOKE_OP;
						instrumented.opcode_aux = Instruction.OP_INVOKE_DIRECT;
						int[] args = new int[1];
						int[] oargs = (int[]) onCreate.insns[0].getExtra();
						args[0] = oargs[0];
						instrumented
								.setExtra(new Object[] { targets[0], args });
						insts[i] = instrumented;
						insts[i + 1] = inst;
					} else {
						insts[i] = inst;
					}
				}

				onCreate.insns = insts;
				methods = new MethodInfo[1];
				methods[0] = onCreate;
			}
		}

		for (int i = 0; i < methods.length; i++) {
			if (params == null) {
				runMethod(c, methods[i]);
			} else {
				runMethod(c, methods[i], params, false);
			}
			reset();
			Log.msg(TAG, "FINISHED!\n");
		}
	}

	public void runMethods(String apk, String[] chain,
			PluginManager pluginManager) throws ZipException, IOException {
		Log.msg(TAG, "apk " + apk);

		// find all methods with the name "onCreate", most likely there is
		// only one
		this.setPluginManager(pluginManager);

		for (int i = 1; i < chain.length; i++) {
			Settings.setEntryClass(chain[i].split(":")[0]);
			Settings.logTag = Settings.getApkName() + "_"
					+ Settings.getEntryClass() + "_" + chain[i];
			ClassInfo c = ClassInfo.findClass(Settings.getEntryClass());
			Log.bb(TAG, "class " + c);
			MethodInfo[] methods = c.findMethodsHere(chain[i].split(":")[1]);
			Log.warn(TAG, "Run chain " + chain[i] + " at " + c);
			// TODO Multiple methods have the same name.
			runMethod(c, methods[0]);
		}
	}

	public void runInstrumentedMethods(StackFrame instrumentedFrame) {
		LinkedList<StackFrame> tmpFrames = new LinkedList<>();
		tmpFrames.add(instrumentedFrame);
		runInstrumentedMethods(tmpFrames);
	}

	public void runInstrumentedMethods(LinkedList<StackFrame> instrumentedFrames) {
		Log.msg(TAG, "Begin run instrumented methods!");
		if (instrumentedFrames != null && instrumentedFrames.size() > 0) {
			instrumentedFrames.remove(null);
			StackFrame stopSign = getCurrStackFrame();
			Log.msg(TAG, getCurrtInst());
			// Object backObj = getReturnReg().getData();
			// ClassInfo backType = getReturnReg().getType();
			resetCallCtx();
			addStackFrames(instrumentedFrames);
			executor.runInstrumentedMethods(this, stopSign);
			// getReturnReg().setValue(backObj, backType);
		}
		Log.msg(TAG, "Finish running instrumented methods! Back to "
				+ getCurrStackFrame());
	}

	public void jump(Instruction inst, boolean then) {
		executor.jump(this, inst, then);
	}

	/**
	 * @Title: runMethod
	 * @Author: hao
	 * @Description: For internal use
	 * @param @param method
	 * @return void
	 * @throws
	 */
	public void runMethod(ClassInfo sitClass, MethodInfo method) {
		Log.msg(TAG, "RUN Method " + method);

		if (method.insns == null) {
			Log.warn(TAG, "Empty body of " + method);
			return;
		}

		executor.runMethod(sitClass, this, method);
	}

	public void runMethod(ClassInfo sitClass, MethodInfo method,
			Object[] params, boolean force) {
		Log.msg(TAG, "Instrumented Method: " + method);
		if (params == null) {
			resetCallCtx();
			runMethod(sitClass, method);
			return;
		}
		if (method == null) {
			Log.err(TAG, "Null Method");
		}

		if (params.length == method.paramTypes.length) {
			Object[] oldParams = params;
			params = new Object[method.paramTypes.length + 1];
			params[0] = null;
			for (int i = 0; i < oldParams.length; i++) {
				params[i + 1] = oldParams[i];
			}

		}

		Log.bb(TAG, "paramLen" + params.length + ", "
				+ method.paramTypes.length);

		// Put an Null stack frame to simulate the calling ctx.
		Register reg = new Register(null, 0);
		if (params[0] == null || params[0].equals("NULL")) {
			params[0] = newVMObject(sitClass);
		}

		reg.setValue(params[0], sitClass);
		callingCtx = new Register[params.length];
		callingCtx[0] = reg;
		for (int i = 1; i < params.length; i++) {
			Log.bb(TAG, "param: " + params[i]);
			if (params[i] instanceof Register) {
				callingCtx[i] = (Register) params[i];
				continue;
			}
			if (params[i].equals("NULL")) {
				try {
					Class<?> clazz = Class
							.forName(method.paramTypes[i - 1].fullName);
					params[i] = clazz.newInstance();
				} catch (ClassNotFoundException e) {
					params[i] = newVMObject(method.paramTypes[i - 1]);
				} catch (InstantiationException e) {
					params[i] = null;
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					params[i] = null;
					e.printStackTrace();
				}
			}

			reg = new Register(null, i);
			reg.setValue(params[i], method.paramTypes[i - 1]);
			callingCtx[i] = reg;
		}

		int[] args = new int[params.length];
		for (int i = 0; i < params.length; i++) {
			args[i] = i;
		}

		executor.runMethod(sitClass, this, method);
	}

	public boolean addEventFrame(DVMObject obj, String eventMethod) {
		return addEventFrame(obj, null, eventMethod);
	}
	
	@SuppressWarnings("unchecked")
	public boolean addEventFrame(String eventClass, String eventMethod, Pair<Object, ClassInfo>[] params) {
		ClassInfo clazz = ClassInfo.findClass(eventClass);
		MethodInfo[] mis = clazz.findMethods(eventMethod);
		
		if (mis != null && mis.length > 0) {
			MethodInfo mi = mis[0];
			if (params == null) {
				params = (Pair<Object, ClassInfo>[]) new Pair[mi.paramTypes.length + 1];
				params[0] = new Pair<Object, ClassInfo>(newVMObject(clazz), clazz);
			}
			StackFrame frame = newStackFrame(clazz, mi, params, true);
			runInstrumentedMethods(frame);
			return true;
		} else {
			Log.err(TAG, "Inconsistent event: cannot find the method "
					+ eventMethod);
			return false;
		}
	}

	@SuppressWarnings("unchecked")
	public boolean addEventFrame(DVMObject obj,
			Pair<Object, ClassInfo>[] params, String eventMethod) {
		MethodInfo[] mis = obj.getType().findMethods(eventMethod);
		if (mis != null && mis.length > 0) {
			MethodInfo mi = mis[0];
			if (params == null) {
				params = (Pair<Object, ClassInfo>[]) new Pair[mi.paramTypes.length + 1];
				params[0] = new Pair<Object, ClassInfo>(obj, obj.type);
				ClassInfo viewType = ClassInfo.findClass("android.view.View");
				for (int i = 0; i < mi.paramTypes.length; i++) {
					if (mi.paramTypes[i].equals(ClassInfo.primitiveInt)) {
						params[i + 1] = new Pair<Object, ClassInfo>(
								new PrimitiveInfo(0), ClassInfo.primitiveInt);
					} else if (mi.paramTypes[i].isConvertibleTo(viewType)) {
						if (getCurrtActivity().getTmpView() == null) {
							getCurrtActivity().setTmpView(new View(this, viewType, -1));
						}
						params[i + 1] = new Pair<Object, ClassInfo>(
								getCurrtActivity().getTmpView(), viewType);
					}
				}
			}

			StackFrame frame = newStackFrame(obj.getType(), mi, params, true);

			// Init tainted fields
			if (Settings.isInitTaintedFields()) {
				// FIXME can be other types
				Map<String, Pair<Object, Instruction>> taintedFields = Settings
						.getTaintedFields(obj.getType());
				ClassInfo type = obj.getType();
				if (taintedFields != null) {
					for (String fieldName : taintedFields.keySet()) {
						Pair<Object, Instruction> infos = taintedFields
								.get(fieldName);
						Object value = infos.getFirst();
						if (type.getStaticFieldType(fieldName) != null) {
							getClass(type).setStatField(fieldName, value);
						} else {
							obj.setField(fieldName, value);
						}
						// TODO
						for (Plugin plugin : frame.getPluginRes().keySet()) {
							for (String tag : plugin.getCurrtRes().keySet()) {
								if (tag.contains("Taint")
										|| tag.contains("Ctx")) {
									plugin.getCurrtRes().get(tag)
											.put(value, infos.getSecond());
								}
							}
						}
					}
				}
			}
			return true;
		} else {
			Log.err(TAG, "Inconsistent event: cannot find the method "
					+ eventMethod);
			return false;
		}
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

	public Instruction getCurrtInst() {
		if (getCurrStackFrame() != null) {
			return getCurrStackFrame().getInst(nowPC);
		}

		return null;
	}

	public void setNowPC(int nowPC) {
		this.nowPC = nowPC;
	}

	public boolean isPass() {
		return executor.pass;
	}

	public void setPass(boolean pass) {
		this.executor.pass = pass;
	}

	public Method getReflectMethod() {
		return reflectMethod;
	}

	public void setReflectMethod(Method reflectMethod) {
		if (reflectMethod != null) {
			Log.bb(TAG, "Set reflective call: " + reflectMethod);
		}
		this.reflectMethod = reflectMethod;
	}

	public PluginManager getPluginManager() {
		return pluginManager;
	}

	public void setPluginManager(PluginManager pluginManager) {
		this.pluginManager = pluginManager;
	}

	public DVMObject getChainThisObj() {
		return chainThisObj;
	}

	public void setChainThisObj(DVMObject chainThisObj) {
		this.chainThisObj = chainThisObj;
	}

	public void initThisObj(String thisObjTypeName, String[] argTypeNames,
			Object[] args) {
		ClassInfo thisObjType = ClassInfo.findClass(thisObjTypeName);
		ClassInfo[] argTypes = null;

		Log.bb(TAG, thisObjType);
		MethodInfo constructor;
		if (argTypeNames != null) {
			argTypes = new ClassInfo[argTypeNames.length];
			for (int i = 0; i < argTypeNames.length; i++) {
				argTypes[i] = ClassInfo.findClass(argTypeNames[i]);
			}
			for (ClassInfo type : argTypes) {
				Log.bb(TAG, "init argType " + type);
			}
			constructor = thisObjType.getConstructor(argTypes);
		} else {
			constructor = thisObjType.getDefaultConstructor();
		}

		if (constructor != null) {
			Object[] callCtx = new Object[args.length + 1];

			int i = 0;
			callCtx[0] = newVMObject(thisObjType);
			for (ClassInfo type : argTypes) {
				if (args[i].equals("NULL")) {
					try {
						Class<?> clazz = Class.forName(type.fullName);
						callCtx[i + 1] = clazz.newInstance();
					} catch (ClassNotFoundException e) {
						callCtx[i + 1] = newVMObject(type);
					} catch (InstantiationException e) {
						callCtx[i + 1] = null;
						e.printStackTrace();
					} catch (IllegalAccessException e) {
						callCtx[i + 1] = null;
						e.printStackTrace();
					}
				} else {
					callCtx[i + 1] = args[i];
				}

				i++;
			}
			runMethod(thisObjType, constructor, callCtx, false);
			chainThisObj = (DVMObject) callCtx[0];
			stack.clear();
		} else {
			if (argTypeNames != null) {
				Log.err(TAG, "Cannot find the constructor!");
			}
		}

	}

	public DVMObject newVMObject(ClassInfo type) {
		ClassInfo oType = type;
		
		while (type != null) {	
			if (type.isConvertibleTo(ClassInfo.findClass("android.app.Activity"))) {
				return new Activity(this, oType);
			}
			if (type.isConvertibleTo(ClassInfo.findClass("android.location.LocationListener"))) {
				return new MyLocationListener(this, oType);
			}
			String typeName = type.toString();
			
			if (!typeName.contains("$")) {
				if (typeName.contains("android.app.Activity")) {
					return new Activity(this, oType);
				} else if (typeName.contains("View")) {
					return GenInstance.getView(this, oType, -1);
				} else if (typeName.contains("AsyncTask")) {
					return new AsyncTask(this, oType);
				} else if (typeName.contains("Adapter")) {
					return new BaseAdapter(this, oType);
				} else if (typeName.contains("BroadcastReceiver")) {
					return new BroadcastReceiver(this, oType);
				} else if (typeName.contains("android.app.Service")) {
					return new Service(this, oType);
				} else if (typeName.contains("android.app")
						&& typeName.endsWith("Fragment")) {
					return GenInstance.getFragment(this, oType);
				} else if (typeName.equals("java.lang.Thread")) {
					return new android.myclasses.Thread(this, oType);
				}
			}

			type = type.getSuperClass();
			
			Log.bb(TAG, "Superclass: " + typeName);
		}
		
		if (oType.isConvertibleTo(ClassInfo.findClass("java.lang.Runnable"))) {
			return new android.myclasses.Runnable(this, oType);
		}

		return new DVMObject(this, oType);
	}

	@Deprecated
	public LinkedList<StackFrame> getTmpFrames() {
		return tmpFrames;
	}

	/**
	 * @Title: setTmpMI
	 * @Author: Hao Fu
	 * @Description: Set the method that should be pushed into the stack soon
	 * @param @param tmpMI
	 * @return void
	 * @throws
	 */
	@Deprecated
	public void addTmpFrameLastRun(StackFrame tmpFrame) {
		if (tmpFrame == null) {
			return;
		}

		if (tmpFrames == null) {
			tmpFrames = new LinkedList<>();
		}
		tmpFrames.addFirst(tmpFrame);
	}

	@Deprecated
	public void addTmpFrameFirstRun(StackFrame tmpFrame) {
		if (tmpFrame == null) {
			return;
		}

		if (tmpFrames == null) {
			tmpFrames = new LinkedList<>();
		}
		tmpFrames.add(tmpFrame);
	}

	@Deprecated
	public void setTmpFrames(List<MethodInfo> mis) {
		if (tmpFrames == null) {
			tmpFrames = new LinkedList<>();
		}
		for (MethodInfo mi : mis) {
			tmpFrames.add(newStackFrame(mi.myClass, mi, false));
		}
	}

	public ClassInfo getCurrtClass() {
		return getCurrStackFrame().getMyClass();
	}

	public Register newTmpRegister() {
		return new Register(null, -2);
	}

	public boolean isCleanCallingCtx() {
		return cleanCallingCtx;
	}

	public void setCleanCallingCtx(boolean cleanCallingCtx) {
		this.cleanCallingCtx = cleanCallingCtx;
	}

	@Deprecated
	public void setRepeatInst(boolean repeatInst) {
		this.repeatInst = repeatInst;
	}

	@Deprecated
	public boolean getRepeatInst() {
		return repeatInst;
	}

	public ServicePool getServicePool() {
		if (servicePool == null) {
			servicePool = new ServicePool();
		}
		return servicePool;
	}

	public void setServicePool(ServicePool servicePool) {
		this.servicePool = servicePool;
	}

	public Activity getCurrtActivity() {
		return currtActivity;
	}

	public void setCurrtActivity(Activity currtActivity) {
		this.currtActivity = currtActivity;
	}

	public LinkedList<Activity> getActivityStack() {
		return activityStack;
	}

	public void setActivityStack(LinkedList<Activity> activityStack) {
		this.activityStack = activityStack;
	}

}
