package android.myclasses;

import patdroid.core.ClassInfo;
import fu.hao.trust.dvm.DVMObject;
import fu.hao.trust.dvm.DalvikVM;
import fu.hao.trust.dvm.DalvikVM.StackFrame;
import fu.hao.trust.utils.Log;
import fu.hao.trust.utils.Pair;
import fu.hao.trust.utils.Settings;

public class Thread extends DVMObject {
	
	private final String TAG = getClass().getSimpleName();

	Runnable runnable;

	public Thread(DalvikVM vm, ClassInfo type) {
		super(vm, type);
	}

	public Thread(Runnable runnable) {
		super(Settings.getVM(), ClassInfo.findClass("java.lang.Thread"));
		this.runnable = runnable;
		Log.bb(TAG, "Add runnable " + runnable);
	}

	public synchronized void start() {
		Log.bb(TAG, "Run replaced exec!");
		if (runnable == null) {
			@SuppressWarnings("unchecked")
			Pair<Object, ClassInfo>[] args = (Pair<Object, ClassInfo>[]) new Pair[1];
			args[0] = new Pair<Object, ClassInfo>(this, this.getType());
			StackFrame frame = Settings.getVM().newStackFrame(this.getType(),
					this.getType().findMethods("run")[0], args, false);
			Settings.getVM().runInstrumentedMethods(frame);
		} else {
			runnable.run();
		}
	}
	
	@Override
	public String toString() {
		return "[myThread: " + super.toString() + "]";
	}

}
