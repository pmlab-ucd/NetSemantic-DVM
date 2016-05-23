package android.os;

import patdroid.core.MethodInfo;
import patdroid.dalvik.Instruction;
import fu.hao.trust.dvm.DVMObject;
import fu.hao.trust.dvm.DalvikVM;
import fu.hao.trust.dvm.DalvikVM.StackFrame;
import fu.hao.trust.utils.Pair;
import fu.hao.trust.utils.Settings;

public class Handler {
	DalvikVM vm;
	 public Handler(Looper looper) {
		 
	 }
	 
	 @SuppressWarnings({ "rawtypes", "unchecked" })
	public final boolean post(Runnable r) {
		 vm = Settings.getVM();
		 Instruction inst = vm.getCurrtInst();
			Object[] extra = (Object[]) inst.getExtra();
			int[] args = (int[]) extra[1];

			DVMObject runner = (DVMObject) vm.getReg(args[1]).getData();
			MethodInfo run = runner.getType().findMethods("run")[0];
			Pair[] params = new Pair[1];
			params[0] = new Pair(runner, runner.getType());
			StackFrame frame = vm.newStackFrame(runner.getType(), run, params, false);
			vm.setTmpFrames(frame, false);
		return true;		 
	 }

}
