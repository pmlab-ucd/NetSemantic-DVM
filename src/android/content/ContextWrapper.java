package android.content;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import android.app.Activity;
import android.app.Service;
import android.content.pm.ApplicationInfo;
import android.os.IBinder;
import patdroid.core.ClassInfo;
import patdroid.core.MethodInfo;
import patdroid.core.PrimitiveInfo;
import fu.hao.trust.dvm.DVMObject;
import fu.hao.trust.dvm.DalvikVM;
import fu.hao.trust.dvm.DalvikVM.StackFrame;
import fu.hao.trust.utils.Log;
import fu.hao.trust.utils.Pair;
import fu.hao.trust.utils.Settings;

public class ContextWrapper extends Context {

	Map<IntentFilter, BroadcastReceiver> filters = new HashMap<>();
	final String TAG = getClass().getName();
	Map<String, SharedPreferences> preferences = new HashMap<>(); 
	private ApplicationInfo appInfo = new ApplicationInfo();
	
	Intent service;
	Set<DVMObject> conns;

	public ContextWrapper(DalvikVM vm, ClassInfo type) {
		super(vm, type);
		conns = new HashSet<>();
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
				.newStackFrame(type, onStartCmds[0], params, false);
		frame.setIntent(intent);
		Log.bb(TAG, "Intent " + intent);
		vm.runInstrumentedMethods(frame);

		return null;
	}

	public void startActivity(Intent intent) {
		ClassInfo type = findReceiver(intent);
		if (type == null) {
			Log.warn(TAG, "Cannot resolve the receiver!");
		} else {
			new Activity(vm, type, intent);
		}
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
	
	public SharedPreferences getSharedPreferences(String name, int mode) {
		if (!preferences.containsKey(name)) {
			preferences.put(name, new SharedPreferences());
		}
		return preferences.get(name);
    }
	
    public boolean bindService(Intent service, ServiceConnection conn, int flags) {
        throw new RuntimeException("Stub!");
    }
    
    @SuppressWarnings("unchecked")
	public boolean bindService(Intent service, DVMObject conn, int flags) {
    	// Locate the service
		ClassInfo serviceClass = service.getTargetClass();
		MethodInfo[] onBinds = serviceClass.findMethods("onBind");
    	if (onBinds == null || onBinds.length == 0) {
    		Log.err(TAG, "Not a correct ServiceClass!");
    		return false;
    	}
    	
    	Pair<Object, ClassInfo>[] params = (Pair<Object, ClassInfo>[]) new Pair[2]; 
		params[0] = new Pair<Object, ClassInfo>(vm.getServicePool().getService(serviceClass), serviceClass);
		params[1] = new Pair<Object, ClassInfo>(service, ClassInfo.findClass("android.content.Intent"));
		StackFrame frame = vm.newStackFrame(serviceClass, onBinds[0], params, false);	
		vm.runInstrumentedMethods(frame);
    	
    	// Bind with the client 
    	MethodInfo[] onServices = conn.getType().findMethods("onServiceConnected");
    	if (onServices == null || onServices.length == 0) {
    		Log.err(TAG, "Not a correct ServiceConnection!");
    		return false;
    	}
    	
    	params = (Pair<Object, ClassInfo>[]) new Pair[3]; 
		params[0] = new Pair<Object, ClassInfo>(conn, conn.getType());
		ComponentName compName = new ComponentName(Settings.getApkName(), this.getClazz().toString());
		params[1] = new Pair<Object, ClassInfo>(compName, ClassInfo.findClass("android.content.ComponentName"));
		IBinder binder = (IBinder) vm.getReturnReg().getData();
		params[2] = new Pair<Object, ClassInfo>(binder, ClassInfo.findClass("android.os.IBinder"));
		frame = vm.newStackFrame(conn.getClazz(), onServices[0], params, false);	
		vm.runInstrumentedMethods(frame);
		

    	
    	this.service = service;
    	conns.add(conn);
    	return true;
    }

    public void unbindService(ServiceConnection conn) {
        throw new RuntimeException("Stub!");
    }
    
    public void unbindService(DVMObject conn) {
    	conns.remove(conn);
    }
    
    public ApplicationInfo getApplicationInfo() {
    	return appInfo;
    }
}
