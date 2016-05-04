package android.content;

import android.content.res.Resources;
import android.telephony.TelephonyManager;
import android.view.WindowManager;
import patdroid.core.ClassInfo;
import fu.hao.trust.dvm.DVMObject;
import fu.hao.trust.dvm.DalvikVM;

public class Context extends DVMObject {
	
	Resources resources;
	
	public Context(DalvikVM vm, ClassInfo type) {
		super(vm, type);
	}

	public void sendBroadcast(Intent paramIntent) {
		
	}
	
	public DalvikVM getVM() {
		return vm;
	}
	
	public Resources getResources() {
		if (resources == null) {
			resources = new Resources();
		}
		
		return resources;
	}	
	
	public Object getSystemService(String name) {
		if (name.equals("phone")) {
			return new TelephonyManager();
		} else if (name.equals("window")) {
			return new WindowManager();
		}

		return null;
	}

}
