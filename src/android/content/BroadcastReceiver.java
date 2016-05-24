package android.content;

import patdroid.core.ClassInfo;
import fu.hao.trust.dvm.DVMObject;
import fu.hao.trust.dvm.DalvikVM;
import fu.hao.trust.utils.Log;

public class BroadcastReceiver extends DVMObject {
	
	final String TAG = getClass().getSimpleName(); 
	
	private Context context;

	public BroadcastReceiver(DalvikVM vm, ClassInfo type) {
		super(vm, type);
		Log.bb(TAG, "New BroadcastReceiver created.");
	}

	public Context getContext() {
		return context;
	}

	public void setContext(Context context) {
		this.context = context;
	}

}
