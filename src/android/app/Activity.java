package android.app;

import java.util.HashMap;
import java.util.Map;

import android.content.ContextWrapper;
import android.content.Intent;
import android.view.View;
import patdroid.core.ClassInfo;
import fu.hao.trust.dvm.DalvikVM;
import fu.hao.trust.dvm.DalvikVM.StackFrame;
import fu.hao.trust.utils.Log;
import fu.hao.trust.utils.Pair;
import fu.hao.trust.utils.Settings;

public class Activity extends ContextWrapper {

	Map<Integer, View> views;
	final String TAG = getClass().getSimpleName();
	Intent intent;
	FragmentManager fragmentManager;
	
	public Activity(DalvikVM vm, ClassInfo type, Intent intent) {
		super(vm, type);
		init(vm, type, intent);
	}
	
	public Activity(DalvikVM vm, ClassInfo type) {
		super(vm, type);
		init(vm, type, null);
	}
	
	public void init(DalvikVM vm, ClassInfo type, Intent intent) {
		Log.bb(TAG, "New Activity Created with type " + type);
		this.intent = intent;
		if (Settings.execOnCreate) {
			if (vm.getCurrStackFrame() != null && type.findMethods("onCreate")[0].equals(vm.getCurrStackFrame().getMethod())) {
				return;
			}
			@SuppressWarnings("unchecked")
			Pair<Object, ClassInfo>[] params = (Pair<Object, ClassInfo>[]) new Pair[2]; 
			params[0] = new Pair<Object, ClassInfo>(this, type);
			params[1] = null;
			StackFrame frame = vm.newStackFrame(type, type.findMethods("onCreate")[0], params, false);
			frame.setIntent(intent);
			Log.bb(TAG, "Intent " + intent);
			Log.bb(TAG, "ROOO " + (vm.getGlobalCallCtx() == null));
			if (vm.getCurrtInst().toString().contains("startActivity")) {
				vm.addTmpFrameFront(frame, false);
			} else {
				vm.addTmpFrameFront(frame, true);
			}
		}
	}
	
	public void setContentView(int view) {

	}
	
    public Intent getIntent() {
    	return intent;
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
	
    public FragmentManager getFragmentManager() {
        if (fragmentManager == null) {
        	fragmentManager = new FragmentManager();
        }
        
        return fragmentManager;
    }
	
}
