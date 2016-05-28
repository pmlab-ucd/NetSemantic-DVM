package android.app;

import java.util.LinkedList;

import android.os.Bundle;
import patdroid.core.ClassInfo;
import patdroid.core.MethodInfo;
import fu.hao.trust.dvm.DVMObject;
import fu.hao.trust.dvm.DalvikVM;
import fu.hao.trust.dvm.DalvikVM.StackFrame;
import fu.hao.trust.utils.Log;
import fu.hao.trust.utils.Pair;
//import fu.hao.trust.utils.Settings;

public class Fragment extends DVMObject {
	Activity activity;

	public Fragment(DalvikVM vm, ClassInfo type) {
		super(vm, type);
		Log.bb(TAG, "Framnet of " + type + " created!");
	}

	/*
	@Override
	public void setType(ClassInfo type) {
		this.type = type;
		myOnAttach(Settings.getVM().getCurrtActivity());
		myOnStart();
	}*/

	public void myOnAttach(Activity activity) {
		this.activity = activity;
		activity.getFragmentManager().addFragment(activity.getFragmentManager().getFragments().size() + 1, this);
		MethodInfo[] onAttaches = type.findMethods("onAttach");
		LinkedList<StackFrame> tmpFrames = new LinkedList<>();
		if (onAttaches != null && onAttaches.length > 0) {
			@SuppressWarnings("unchecked")
			Pair<Object, ClassInfo>[] params = (Pair<Object, ClassInfo>[]) new Pair[2];
			params[0] = new Pair<Object, ClassInfo>(this, type);
			params[1] = new Pair<Object, ClassInfo>(activity,
					activity.getType());
			StackFrame frame = vm.newStackFrame(type, onAttaches[0], params,
					false);
			// vm.addTmpFrameFirstRun(frame);
			tmpFrames.add(frame);
		}
		// vm.addTmpFrameLastRun(onCreate());
		tmpFrames.addFirst(onCreate());
		vm.runInstrumentedMethods(tmpFrames);
		Log.bb(TAG, "What!");
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
		MethodInfo[] onCreateViews = type.findMethods("onCreateView");
		if (onCreateViews != null && onCreateViews.length > 0) {
			@SuppressWarnings("unchecked")
			Pair<Object, ClassInfo>[] params = (Pair<Object, ClassInfo>[]) new Pair[4];
			params[0] = new Pair<Object, ClassInfo>(this, type);
			params[3] = new Pair<Object, ClassInfo>(null, ClassInfo.findClass("android.os.Bundle"));
			StackFrame frame = vm.newStackFrame(type,
					type.findMethods("onCreateView")[0], params, false);
			return frame;
		} else {
			return null;
		}
	}

	public void myOnStart() {
		LinkedList<StackFrame> tmpFrames = new LinkedList<>();
		// vm.addTmpFrameFirstRun(onCreateView());
		tmpFrames.add(onCreateView());
		// vm.addTmpFrameLastRun(onActivityCreated());
		tmpFrames.addFirst(onActivityCreated());

		MethodInfo[] onStarts = type.findMethods("onStart");
		if (onStarts != null && onStarts.length > 0) {
			@SuppressWarnings("unchecked")
			Pair<Object, ClassInfo>[] params = (Pair<Object, ClassInfo>[]) new Pair[1];
			params[0] = new Pair<Object, ClassInfo>(this, type);
			StackFrame frame = vm.newStackFrame(type,
					onStarts[0], params, false);
			// vm.addTmpFrameLastRun(frame);
			tmpFrames.addFirst(frame);
		}
		vm.runInstrumentedMethods(tmpFrames);
	}

	public StackFrame onActivityCreated() {
		MethodInfo[] targets = type.findMethods("onActivityCreated");
		if (targets != null && targets.length > 0) {
			@SuppressWarnings("unchecked")
			Pair<Object, ClassInfo>[] params = (Pair<Object, ClassInfo>[]) new Pair[2];
			params[0] = new Pair<Object, ClassInfo>(this, type);
			params[1] = null;
			StackFrame frame = vm
					.newStackFrame(type, targets[0], params, false);
			return frame;
		} else {
			return null;
		}
	}
	
    public final FragmentManager getFragmentManager() {
    	if (vm.getCurrtActivity() != null) {
    		return vm.getCurrtActivity().getFragmentManager();
    	} 
    	return null;
    }
    
    public final Activity getActivity() {
    	return activity;
    }
    
    public final Bundle getArguments() {
    	return null; 
    }

}
