package android.app;

import java.util.HashMap;
import java.util.Map;

import android.telephony.TelephonyManager;
import android.view.View;
import patdroid.core.ClassInfo;
import fu.hao.trust.dvm.DVMObject;
import fu.hao.trust.dvm.DalvikVM;

public class Activity extends DVMObject {

	Map<Integer, View> views;

	public Activity(DalvikVM vm, ClassInfo type) {
		super(vm, type);
	}

	public void setContentView(int view) {

	}

	public Object getSystemService(String name) {
		if (name.equals("phone")) {
			return new TelephonyManager();
		}

		return null;
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

}
