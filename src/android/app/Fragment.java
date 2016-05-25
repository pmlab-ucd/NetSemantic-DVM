package android.app;

import patdroid.core.ClassInfo;
import patdroid.core.MethodInfo;
import fu.hao.trust.dvm.DVMObject;
import fu.hao.trust.dvm.DalvikVM;
import fu.hao.trust.dvm.DalvikVM.StackFrame;
import fu.hao.trust.utils.Log;
import fu.hao.trust.utils.Pair;

public class Fragment extends DVMObject {
	Activity activity;

	public Fragment(DalvikVM vm, ClassInfo type) {
		super(vm, type);
		Log.bb(TAG, "Framnet of " + type + " created!");
	}

	public void myOnAttach(Activity activity) {
		this.activity = activity;
		MethodInfo[] onAttaches = type.findMethods("onAttach");
		if (onAttaches != null && onAttaches.length > 0) {
			@SuppressWarnings("unchecked")
			Pair<Object, ClassInfo>[] params = (Pair<Object, ClassInfo>[]) new Pair[2];
			params[0] = new Pair<Object, ClassInfo>(this, type);
			params[1] = new Pair<Object, ClassInfo>(activity,
					activity.getType());
			StackFrame frame = vm.newStackFrame(type, onAttaches[0], params,
					false);
			vm.addTmpFrameBack(frame, false);
		}
		vm.addTmpFrameBack(onCreate(), false);
	}

	public StackFrame onCreate() {
		MethodInfo[] onCreates = type.findMethods("onCreate");
		if (onCreates != null && onCreates.length > 0) {
			@SuppressWarnings("unchecked")
			Pair<Object, ClassInfo>[] params = (Pair<Object, ClassInfo>[]) new Pair[2];
			params[0] = new Pair<Object, ClassInfo>(this, type);
			params[1] = null;
			StackFrame frame = vm.newStackFrame(type, onCreates[0], params,
					false);
			return frame;
		} else {
			return null;
		}
	}

	public StackFrame onCreateView() {
		@SuppressWarnings("unchecked")
		Pair<Object, ClassInfo>[] params = (Pair<Object, ClassInfo>[]) new Pair[2];
		params[0] = new Pair<Object, ClassInfo>(this, type);
		params[1] = null;
		StackFrame frame = vm.newStackFrame(type,
				type.findMethods("onCreateView")[0], params, false);
		return frame;
	}

	public void myOnStart() {
		vm.addTmpFrameBack(onCreateView(), false);
		vm.addTmpFrameBack(onActivityCreated(), false);

		@SuppressWarnings("unchecked")
		Pair<Object, ClassInfo>[] params = (Pair<Object, ClassInfo>[]) new Pair[2];
		params[0] = new Pair<Object, ClassInfo>(this, type);
		params[1] = null;
		StackFrame frame = vm.newStackFrame(type,
				type.findMethods("onStart")[0], params, false);
		vm.addTmpFrameBack(frame, false);
	}

	public StackFrame onActivityCreated() {
		@SuppressWarnings("unchecked")
		Pair<Object, ClassInfo>[] params = (Pair<Object, ClassInfo>[]) new Pair[2];
		params[0] = new Pair<Object, ClassInfo>(this, type);
		params[1] = null;
		StackFrame frame = vm.newStackFrame(type,
				type.findMethods("onActivityCreated")[0], params, false);
		return frame;
	}

}
