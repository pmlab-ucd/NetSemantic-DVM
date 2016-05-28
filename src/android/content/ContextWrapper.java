package android.content;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import android.app.Activity;
import android.app.Service;
import patdroid.core.ClassInfo;
import patdroid.core.MethodInfo;
import patdroid.core.PrimitiveInfo;
import fu.hao.trust.dvm.DalvikVM;
import fu.hao.trust.dvm.DalvikVM.StackFrame;
import fu.hao.trust.utils.Log;
import fu.hao.trust.utils.Pair;
import fu.hao.trust.utils.Settings;

public class ContextWrapper extends Context {

	Map<IntentFilter, BroadcastReceiver> filters = new HashMap<>();
	final String TAG = getClass().getName();

	public ContextWrapper(DalvikVM vm, ClassInfo type) {
		super(vm, type);
	}

	public Intent registerReceiver(BroadcastReceiver receiver,
			IntentFilter filter) {
		receiver.setContext(this);
		filters.put(filter, receiver);
		Log.debug(TAG, "Filter: " + filter + ", Receiver: " + receiver);
		return null;
	}

	public void sendBroadcast(Intent intent) {
		addOnReceives(intent);
	}

	private void addOnReceives(Intent intent) {
		String action = intent.getAction();
		@SuppressWarnings("unchecked")
		Pair<Object, ClassInfo>[] params = (Pair<Object, ClassInfo>[]) new Pair[3];
		params[1] = new Pair<Object, ClassInfo>(this, type);
		params[2] = new Pair<Object, ClassInfo>(intent,
				ClassInfo.findClass(intent.getClass().getName()));
		LinkedList<StackFrame> tmpFrames = new LinkedList<>();
		for (IntentFilter filter : filters.keySet()) {
			if (filter.matchAction(action)) {
				BroadcastReceiver receiver = filters.get(filter);
				MethodInfo[] onReceives = receiver.getType().findMethods(
						"onReceive");
				if (onReceives != null && onReceives.length > 0) {
					MethodInfo mi = onReceives[0];
					params[0] = new Pair<Object, ClassInfo>(receiver,
							receiver.getType());
					tmpFrames.add(vm.newStackFrame(mi.myClass, mi, params, false));
				}
			}
		}

		vm.runInstrumentedMethods(tmpFrames);
	}

	public ComponentName startService(Intent intent) {
		ClassInfo type = findReceiver(intent);
		if (type == null) {
			Log.err(TAG, "Cannot resolve the receiver!");
		}

		Service service = vm.getServicePool().getService(type);
		if (service == null) {
			service = new Service(vm, type, intent);
		}

		MethodInfo[] onStartCmds = type.findMethods("onStartCommand");
		@SuppressWarnings("unchecked")
		Pair<Object, ClassInfo>[] params = (Pair<Object, ClassInfo>[]) new Pair[4];
		params[0] = new Pair<Object, ClassInfo>(service, type);
		params[1] = new Pair<Object, ClassInfo>(intent,
				ClassInfo.findClass(intent.getClass().getName()));
		params[2] = new Pair<Object, ClassInfo>(new PrimitiveInfo(0),
				ClassInfo.findClass(intent.getClass().getName()));
		params[3] = new Pair<Object, ClassInfo>(new PrimitiveInfo(0),
				ClassInfo.primitiveInt);
		StackFrame frame = vm
				.newStackFrame(type, onStartCmds[0], params);
		frame.setIntent(intent);
		Log.bb(TAG, "Intent " + intent);

		return null;
	}

	public void startActivity(Intent intent) {
		ClassInfo type = findReceiver(intent);
		if (type == null) {
			Log.err(TAG, "Cannot resolve the receiver!");
		}
		new Activity(vm, type, intent);
	}

	private ClassInfo findReceiver(Intent intent) {
		ClassInfo type = null;
		if (intent.getTargetClass() != null) {
			type = intent.getTargetClass();
		} else if (intent.getAction() != null) {
			String action = intent.getAction();
			type = ClassInfo.findClass(Settings.getIntentTarget(action));
		}

		return type;
	}

}
