package android.app;

import android.os.Bundle;
import patdroid.core.ClassInfo;
import fu.hao.trust.dvm.DVMObject;
import fu.hao.trust.dvm.DalvikVM;

public class Application extends DVMObject {

	public Application(DalvikVM vm, ClassInfo type) {
		super(vm, type);
	}
	
    public void registerActivityLifecycleCallbacks(Application.ActivityLifecycleCallbacks callback) {
        throw new RuntimeException("Stub!");
    }
    
    public void unregisterActivityLifecycleCallbacks(Application.ActivityLifecycleCallbacks callback) {
        throw new RuntimeException("Stub!");
    }
    
    public void registerActivityLifecycleCallbacks(DVMObject callback) {
    	vm.getCallbackPool().put(callback.getClazz().fullName, callback);
    }
    
    public void unregisterActivityLifecycleCallbacks(DVMObject callback) {
    	vm.getCallbackPool().remove(callback.getClazz());
    }
	
    public interface ActivityLifecycleCallbacks {
        void onActivityCreated(Activity var1, Bundle var2);

        void onActivityStarted(Activity var1);

        void onActivityResumed(Activity var1);

        void onActivityPaused(Activity var1);

        void onActivityStopped(Activity var1);

        void onActivitySaveInstanceState(Activity var1, Bundle var2);

        void onActivityDestroyed(Activity var1);
    }
}
