package android.myclasses.java.util;

import java.util.Date;

import patdroid.core.ClassInfo;
import fu.hao.trust.dvm.DVMObject;
import fu.hao.trust.dvm.DalvikVM.StackFrame;
import fu.hao.trust.utils.Pair;
import fu.hao.trust.utils.Settings;

public class Timer {
	
	public Timer(boolean bool) {
		
	}
	
	public Timer() {
		
	}
	
	public void schedule(DVMObject timerTask, long start, long end) {
		schedule(timerTask, 0);
	}
	
    public void schedule(DVMObject timerTask, Date when) {
    	schedule(timerTask, 0);
    }
	
	public void schedule(DVMObject timerTask, long delay) {
		if (timerTask != null) {
			@SuppressWarnings("unchecked")
			Pair<Object, ClassInfo>[] args = (Pair<Object, ClassInfo>[]) new Pair[1];
			args[0] = new Pair<Object, ClassInfo>(timerTask, timerTask.getType());
			StackFrame frame = Settings.getVM().newStackFrame(timerTask.getType(),
					timerTask.getType().findMethods("run")[0], args, false);
			Settings.getVM().runInstrumentedMethods(frame);
		}
	}
	
	

}
