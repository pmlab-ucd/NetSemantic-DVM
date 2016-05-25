package android.app;

import patdroid.core.ClassInfo;
import patdroid.core.MethodInfo;
import fu.hao.trust.dvm.DalvikVM;
import fu.hao.trust.dvm.DalvikVM.StackFrame;
import fu.hao.trust.utils.Log;
import fu.hao.trust.utils.Pair;
import fu.hao.trust.utils.Settings;
import android.content.ContextWrapper;
import android.content.Intent;

public class Service extends ContextWrapper {

	final String TAG = getClass().getSimpleName();

	Intent intent;
	
	public Service(DalvikVM vm, ClassInfo type) {
		super(vm, type);
		init(vm, type, null);
	}

	public Service(DalvikVM vm, ClassInfo type, Intent intent) {
		super(vm, type);
		init(vm, type, intent);
	}
	
	public void init(DalvikVM vm, ClassInfo type, Intent intent) {
		Log.bb(TAG, "New Service Created with type " + type);
		vm.getServicePool().addService(this);
		this.intent = intent;
		if (Settings.execOnCreate) {
			MethodInfo[] onCreates = type.findMethods("onCreate");
			if (onCreates != null
					&& onCreates.length > 0) {
				// Avoid duplicate run
				if (!(vm.getCurrStackFrame() != null && onCreates[0].equals(vm
						.getCurrStackFrame().getMethod()))) {

					@SuppressWarnings("unchecked")
					Pair<Object, ClassInfo>[] params = (Pair<Object, ClassInfo>[]) new Pair[2];
					params[0] = new Pair<Object, ClassInfo>(this, type);
					params[1] = null;
					StackFrame frame = vm.newStackFrame(type,
							onCreates[0], params, false);
					frame.setIntent(intent);
					Log.bb(TAG, "Intent " + intent);
					Log.bb(TAG, "ROOO " + (vm.getGlobalCallCtx() == null));
					if (vm.getCurrtInst().toString().contains("startService")) {
						vm.addTmpFrameFront(frame, false);
					} else {
						vm.addTmpFrameFront(frame, true);
					}
				}
			}
		}
	}
	

}
