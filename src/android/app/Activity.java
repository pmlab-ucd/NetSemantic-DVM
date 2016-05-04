package android.app;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.view.View;
import patdroid.core.ClassInfo;
import fu.hao.trust.dvm.DalvikVM;
import fu.hao.trust.utils.Log;

public class Activity extends Context {

	Map<Integer, View> views;
	final String TAG = getClass().getSimpleName();
	
	
	public Activity(DalvikVM vm, ClassInfo type) {
		super(vm, type);
		Log.bb(TAG, "New Activity Created!");
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
