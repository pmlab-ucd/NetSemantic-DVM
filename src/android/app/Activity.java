package android.app;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.view.View;
import patdroid.core.ClassInfo;
import fu.hao.trust.dvm.DalvikVM;
import fu.hao.trust.dvm.DalvikVM.StackFrame;
import fu.hao.trust.utils.Log;
import fu.hao.trust.utils.Pair;
import fu.hao.trust.utils.Settings;

public class Activity extends Context {

	Map<Integer, View> views;
	final String TAG = getClass().getSimpleName();
	
	public Activity(DalvikVM vm, ClassInfo type) {
		super(vm, type);
		Log.bb(TAG, "New Activity Created with type " + type);
		if (Settings.execOnCreate) {
			@SuppressWarnings("unchecked")
			Pair<Object, ClassInfo>[] params = (Pair<Object, ClassInfo>[]) new Pair[2]; 
			params[0] = new Pair<Object, ClassInfo>(this, type);
			params[1] = null;
			StackFrame frame = vm.newStackFrame(type, type.findMethods("onCreate")[0], params, false);
			Log.bb(TAG, "ROOO " + (vm.getGlobalCallCtx() == null));
			vm.setTmpFrames(frame, true);
		}
	}

	public void setContentView(int view) {

	}

	public View findViewById(int id) {
		if (views == null) {
			views = new HashMap<>();
		}

		if (!views.containsKey(id)) {
			View view = new View(vm, ClassInfo.findClass("android.view.View"));
			view.mID = id;
			views.put(id, view);
		}
		return views.get(id);
	}
	
	@Override
	public String toString() {
		return "Activity:" + super.toString();
	}
	
}
