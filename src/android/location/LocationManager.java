package android.location;

import java.util.HashSet;
import java.util.Set;

import fu.hao.trust.dvm.DalvikVM.StackFrame;
import fu.hao.trust.utils.Pair;
import fu.hao.trust.utils.Settings;
import patdroid.core.ClassInfo;
import patdroid.core.MethodInfo;

public class LocationManager {
    public static final String NETWORK_PROVIDER = "network";
    public static final String GPS_PROVIDER = "gps";
    public static final String PASSIVE_PROVIDER = "passive";
    public static final String KEY_PROXIMITY_ENTERING = "entering";
    public static final String KEY_STATUS_CHANGED = "status";
    public static final String KEY_PROVIDER_ENABLED = "providerEnabled";
    public static final String KEY_LOCATION_CHANGED = "location";
    public static final String PROVIDERS_CHANGED_ACTION = "android.location.PROVIDERS_CHANGED";
    
    Location location;
    
    Set<LocationListener> listeners;
    
    public LocationManager() {
    	listeners = new HashSet<>();
    	location = new Location();
    }
	
    public void requestLocationUpdates(String provider, long minTime, float minDistance, LocationListener listener) {
       listeners.add(listener); 
       MethodInfo[] onLocationChangeds = listener.getType().findMethods("onLocationChanged");
       if (onLocationChangeds != null && onLocationChangeds.length > 0) {
    	   @SuppressWarnings("unchecked")
			Pair<Object, ClassInfo>[] args = (Pair<Object, ClassInfo>[]) new Pair[2]; 
    	   	args[0] = new Pair<Object, ClassInfo>(listener, listener.getType());
    	   	args[1] = new Pair<Object, ClassInfo>(location, ClassInfo.findClass("android.location.Location"));
			StackFrame frame = Settings.getVM().newStackFrame(listener.getType(), onLocationChangeds[0], args, false);
			Settings.getVM().runInstrumentedMethods(frame);
       }
    }
}
