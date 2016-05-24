package android.content;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import patdroid.core.ClassInfo;
import patdroid.core.MethodInfo;
import fu.hao.trust.dvm.DalvikVM;
import fu.hao.trust.dvm.DalvikVM.StackFrame;
import fu.hao.trust.utils.Log;
import fu.hao.trust.utils.Pair;

public class ContextWrapper extends Context {

	Map<IntentFilter, BroadcastReceiver> filters = new HashMap<>();
	final String TAG = getClass().getName();
	
	public ContextWrapper(DalvikVM vm, ClassInfo type) {
		super(vm, type);
	}
	
    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
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
		params[2] = new Pair<Object, ClassInfo>(intent, ClassInfo.findClass(intent.getClass().getName()));
		LinkedList<StackFrame> frames = new LinkedList<>();
    	for (IntentFilter filter : filters.keySet()) {
    		if (filter.matchAction(action)) {
    			BroadcastReceiver receiver = filters.get(filter);
    			MethodInfo[] onReceives = receiver.getType().findMethods("onReceive");
    			if (onReceives != null && onReceives.length > 0) {
    				MethodInfo mi = onReceives[0];
    				params[0] = new Pair<Object, ClassInfo>(receiver, receiver.getType());
    	    		frames.add(vm.newStackFrame(mi.myClass, mi, params, false));
    			}
    		}
    	}
    	
    	vm.setTmpFrames(frames, false);
    }

}
