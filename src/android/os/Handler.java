package android.os;

import patdroid.core.ClassInfo;
import patdroid.core.MethodInfo;
import patdroid.dalvik.Instruction;
import fu.hao.trust.dvm.DVMObject;
import fu.hao.trust.dvm.DalvikVM;
import fu.hao.trust.dvm.DalvikVM.StackFrame;
import fu.hao.trust.utils.Log;
import fu.hao.trust.utils.Pair;
import fu.hao.trust.utils.Settings;

public class Handler extends DVMObject {
	
	public Handler() {
		super(Settings.getVM(), ClassInfo.findClass("android.os.Handler"));
	}

	public Handler(Looper looper) {
		super(Settings.getVM(), ClassInfo.findClass("android.os.Handler"));
	}
	
	public Handler(DalvikVM vm, ClassInfo type) {
		super(vm, type);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public final boolean post(Runnable r) {
		Instruction inst = vm.getCurrtInst();
		Object[] extra = (Object[]) inst.getExtra();
		int[] args = (int[]) extra[1];

		DVMObject runner = (DVMObject) vm.getReg(args[1]).getData();
		MethodInfo run = runner.getType().findMethods("run")[0];
		Pair[] params = new Pair[1];
		params[0] = new Pair(runner, runner.getType());
		StackFrame frame = vm.newStackFrame(runner.getType(), run, params,
				false);
		vm.runInstrumentedMethods(frame);
		return true;
	}
	
	@SuppressWarnings("unchecked")
	public final boolean post(DVMObject runner) {
		MethodInfo run = runner.getType().findMethods("run")[0];
		@SuppressWarnings("rawtypes")
		Pair[] params = new Pair[1];
		params[0] = new Pair<DVMObject, ClassInfo>(runner, runner.getType());
		StackFrame frame = vm.newStackFrame(runner.getType(), run, params,
				false);
		vm.runInstrumentedMethods(frame);
		return true;
	}
	
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public void dispatchMessage(Message msg) {
    	// Look for handle message 
		MethodInfo[] handleMsgs = type.findMethods("handleMessage");
		if (handleMsgs != null && handleMsgs.length > 0) {
			Pair[] params = new Pair[2];
			params[0] = new Pair(this, type);
			params[1] = new Pair(msg, msg.getType());
			StackFrame frame = vm.newStackFrame(type, handleMsgs[0], params,
					false);
			vm.runInstrumentedMethods(frame);
		} else {
			Log.warn(TAG, "Cannot locate the handleMsg method!");
		}
    }

}
