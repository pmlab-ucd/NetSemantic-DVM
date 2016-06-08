package android.myclasses;

import patdroid.core.ClassInfo;
import fu.hao.trust.dvm.DVMObject;
import fu.hao.trust.dvm.DalvikVM.StackFrame;
import fu.hao.trust.utils.Log;
import fu.hao.trust.utils.Pair;
import fu.hao.trust.utils.Settings;

public class Executor {

	private final String TAG = getClass().getSimpleName();

	public void execute(Runnable var1) {
		Log.bb(TAG, "Run replaced exec!");
		@SuppressWarnings("unchecked")
		Pair<Object, ClassInfo>[] args = (Pair<Object, ClassInfo>[]) new Pair[1];
		args[0] = new Pair<Object, ClassInfo>(var1, var1.getType());
		StackFrame frame = Settings.getVM().newStackFrame(var1.getType(),
				var1.getType().findMethods("run")[0], args, false);
		Settings.getVM().runInstrumentedMethods(frame);
	}
	
	public void execute(DVMObject var1) {
		Log.bb(TAG, "Run replaced exec!");
		@SuppressWarnings("unchecked")
		Pair<Object, ClassInfo>[] args = (Pair<Object, ClassInfo>[]) new Pair[1];
		args[0] = new Pair<Object, ClassInfo>(var1, var1.getType());
		StackFrame frame = Settings.getVM().newStackFrame(var1.getType(),
				var1.getType().findMethods("run")[0], args, false);
		Settings.getVM().runInstrumentedMethods(frame);
	}

}
