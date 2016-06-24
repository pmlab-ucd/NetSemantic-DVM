package android.location;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.app.PendingIntent;
import android.location.GpsStatus.Listener;
import android.location.GpsStatus.NmeaListener;
import android.os.Bundle;
import android.os.Looper;
import fu.hao.trust.dvm.DVMObject;
import fu.hao.trust.dvm.DalvikVM.StackFrame;
import fu.hao.trust.utils.Log;
import fu.hao.trust.utils.Pair;
import fu.hao.trust.utils.Settings;
import patdroid.core.ClassInfo;
import patdroid.core.MethodInfo;

public class LocationManager {
	private static final String TAG = LocationManager.class.getSimpleName();
	
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

	public void requestLocationUpdates(String provider, long minTime,
			float minDistance, LocationListener alistener) {
		listeners.add(alistener);
		if (alistener instanceof DVMObject) {
			DVMObject listener = (DVMObject) alistener;
			MethodInfo[] onLocationChangeds = listener.getType().findMethods(
					"onLocationChanged");
			if (onLocationChangeds != null && onLocationChangeds.length > 0) {
				@SuppressWarnings("unchecked")
				Pair<Object, ClassInfo>[] args = (Pair<Object, ClassInfo>[]) new Pair[2];
				args[0] = new Pair<Object, ClassInfo>(listener,
						listener.getType());
				args[1] = new Pair<Object, ClassInfo>(location,
						ClassInfo.findClass("android.location.Location"));
				StackFrame frame = Settings.getVM().newStackFrame(
						listener.getType(), onLocationChangeds[0], args, false);
				Settings.getVM().runInstrumentedMethods(frame);
			}
		} else {
			Log.err(TAG, "Not a DVMObject!");
		}
	}
	
	//Providers(boolean enabledOnly) {
       // throw new RuntimeException("Stub!");
    //}

    public LocationProvider getProvider(String name) {
        throw new RuntimeException("Stub!");
    }

    public List<String> getProviders(Criteria criteria, boolean enabledOnly) {
        throw new RuntimeException("Stub!");
    }

    public String getBestProvider(Criteria criteria, boolean enabledOnly) {
    	return "GPS";
    }


    public void requestLocationUpdates(String provider, long minTime, float minDistance, LocationListener listener, Looper looper) {
        throw new RuntimeException("Stub!");
    }

    public void requestLocationUpdates(long minTime, float minDistance, Criteria criteria, LocationListener listener, Looper looper) {
        throw new RuntimeException("Stub!");
    }

    public void requestLocationUpdates(String provider, long minTime, float minDistance, PendingIntent intent) {
        throw new RuntimeException("Stub!");
    }

    public void requestLocationUpdates(long minTime, float minDistance, Criteria criteria, PendingIntent intent) {
        throw new RuntimeException("Stub!");
    }

    public void requestSingleUpdate(String provider, LocationListener listener, Looper looper) {
        throw new RuntimeException("Stub!");
    }

    public void requestSingleUpdate(Criteria criteria, LocationListener listener, Looper looper) {
        throw new RuntimeException("Stub!");
    }

    public void requestSingleUpdate(String provider, PendingIntent intent) {
        throw new RuntimeException("Stub!");
    }

    public void requestSingleUpdate(Criteria criteria, PendingIntent intent) {
        throw new RuntimeException("Stub!");
    }

    public void removeUpdates(LocationListener listener) {
        throw new RuntimeException("Stub!");
    }

    public void removeUpdates(PendingIntent intent) {
        throw new RuntimeException("Stub!");
    }

    public void addProximityAlert(double latitude, double longitude, float radius, long expiration, PendingIntent intent) {
        throw new RuntimeException("Stub!");
    }

    public void removeProximityAlert(PendingIntent intent) {
        throw new RuntimeException("Stub!");
    }

    public boolean isProviderEnabled(String provider) {
        throw new RuntimeException("Stub!");
    }

    public Location getLastKnownLocation(String provider) {
    	return location;
    }

    public void addTestProvider(String name, boolean requiresNetwork, boolean requiresSatellite, boolean requiresCell, boolean hasMonetaryCost, boolean supportsAltitude, boolean supportsSpeed, boolean supportsBearing, int powerRequirement, int accuracy) {
        throw new RuntimeException("Stub!");
    }

    public void removeTestProvider(String provider) {
        throw new RuntimeException("Stub!");
    }

    public void setTestProviderLocation(String provider, Location loc) {
        throw new RuntimeException("Stub!");
    }

    public void clearTestProviderLocation(String provider) {
        throw new RuntimeException("Stub!");
    }

    public void setTestProviderEnabled(String provider, boolean enabled) {
        throw new RuntimeException("Stub!");
    }

    public void clearTestProviderEnabled(String provider) {
        throw new RuntimeException("Stub!");
    }

    public void setTestProviderStatus(String provider, int status, Bundle extras, long updateTime) {
        throw new RuntimeException("Stub!");
    }

    public void clearTestProviderStatus(String provider) {
        throw new RuntimeException("Stub!");
    }

    public boolean addGpsStatusListener(Listener listener) {
        throw new RuntimeException("Stub!");
    }

    public void removeGpsStatusListener(Listener listener) {
        throw new RuntimeException("Stub!");
    }

    public boolean addNmeaListener(NmeaListener listener) {
        throw new RuntimeException("Stub!");
    }

    public void removeNmeaListener(NmeaListener listener) {
        throw new RuntimeException("Stub!");
    }

    public GpsStatus getGpsStatus(GpsStatus status) {
        throw new RuntimeException("Stub!");
    }

    public boolean sendExtraCommand(String provider, String command, Bundle extras) {
        throw new RuntimeException("Stub!");
    }
}
