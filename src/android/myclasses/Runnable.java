package android.myclasses;

import patdroid.core.ClassInfo;
import fu.hao.trust.dvm.DVMObject;
import fu.hao.trust.dvm.DalvikVM;
import fu.hao.trust.dvm.DalvikVM.StackFrame;
import fu.hao.trust.utils.Pair;
import fu.hao.trust.utils.Settings;

public class Runnable extends DVMObject {

	public Runnable(DalvikVM vm, ClassInfo type) {
		super(vm, type);
	}
	
	public void run() {
		@SuppressWarnings("unchecked")
		Pair<Object, ClassInfo>[] args = (Pair<Object, ClassInfo>[]) new Pair[1];
		args[0] = new Pair<Object, ClassInfo>(this, this.getType());
		StackFrame frame = Settings.getVM().newStackFrame(this.getType(),
				this.getType().findMethods("run")[0], args, false);
		Settings.getVM().runInstrumentedMethods(frame);
	}
	
	@Override
	public String toString() {
		return "[myRunnable " + super.toString() + "]";
	}

}
