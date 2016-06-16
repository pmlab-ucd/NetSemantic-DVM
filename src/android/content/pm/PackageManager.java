//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package android.content.pm;

import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;

import java.util.ArrayList;
import java.util.List;

public class PackageManager {
    public static final int GET_ACTIVITIES = 1;
    public static final int GET_RECEIVERS = 2;
    public static final int GET_SERVICES = 4;
    public static final int GET_PROVIDERS = 8;
    public static final int GET_INSTRUMENTATION = 16;
    public static final int GET_INTENT_FILTERS = 32;
    public static final int GET_SIGNATURES = 64;
    public static final int GET_RESOLVED_FILTER = 64;
    public static final int GET_META_DATA = 128;
    public static final int GET_GIDS = 256;
    public static final int GET_DISABLED_COMPONENTS = 512;
    public static final int GET_SHARED_LIBRARY_FILES = 1024;
    public static final int GET_URI_PERMISSION_PATTERNS = 2048;
    public static final int GET_PERMISSIONS = 4096;
    public static final int GET_UNINSTALLED_PACKAGES = 8192;
    public static final int GET_CONFIGURATIONS = 16384;
    public static final int MATCH_DEFAULT_ONLY = 65536;
    public static final int PERMISSION_GRANTED = 0;
    public static final int PERMISSION_DENIED = -1;
    public static final int SIGNATURE_MATCH = 0;
    public static final int SIGNATURE_NEITHER_SIGNED = 1;
    public static final int SIGNATURE_FIRST_NOT_SIGNED = -1;
    public static final int SIGNATURE_SECOND_NOT_SIGNED = -2;
    public static final int SIGNATURE_NO_MATCH = -3;
    public static final int SIGNATURE_UNKNOWN_PACKAGE = -4;
    public static final int COMPONENT_ENABLED_STATE_DEFAULT = 0;
    public static final int COMPONENT_ENABLED_STATE_ENABLED = 1;
    public static final int COMPONENT_ENABLED_STATE_DISABLED = 2;
    public static final int COMPONENT_ENABLED_STATE_DISABLED_USER = 3;
    public static final int DONT_KILL_APP = 1;
    public static final int VERIFICATION_ALLOW = 1;
    public static final int VERIFICATION_REJECT = -1;
    public static final long MAXIMUM_VERIFICATION_TIMEOUT = 3600000L;
    public static final String FEATURE_AUDIO_LOW_LATENCY = "android.hardware.audio.low_latency";
    public static final String FEATURE_BLUETOOTH = "android.hardware.bluetooth";
    public static final String FEATURE_CAMERA = "android.hardware.camera";
    public static final String FEATURE_CAMERA_AUTOFOCUS = "android.hardware.camera.autofocus";
    public static final String FEATURE_CAMERA_ANY = "android.hardware.camera.any";
    public static final String FEATURE_CAMERA_FLASH = "android.hardware.camera.flash";
    public static final String FEATURE_CAMERA_FRONT = "android.hardware.camera.front";
    public static final String FEATURE_LOCATION = "android.hardware.location";
    public static final String FEATURE_LOCATION_GPS = "android.hardware.location.gps";
    public static final String FEATURE_LOCATION_NETWORK = "android.hardware.location.network";
    public static final String FEATURE_MICROPHONE = "android.hardware.microphone";
    public static final String FEATURE_NFC = "android.hardware.nfc";
    public static final String FEATURE_SENSOR_ACCELEROMETER = "android.hardware.sensor.accelerometer";
    public static final String FEATURE_SENSOR_BAROMETER = "android.hardware.sensor.barometer";
    public static final String FEATURE_SENSOR_COMPASS = "android.hardware.sensor.compass";
    public static final String FEATURE_SENSOR_GYROSCOPE = "android.hardware.sensor.gyroscope";
    public static final String FEATURE_SENSOR_LIGHT = "android.hardware.sensor.light";
    public static final String FEATURE_SENSOR_PROXIMITY = "android.hardware.sensor.proximity";
    public static final String FEATURE_TELEPHONY = "android.hardware.telephony";
    public static final String FEATURE_TELEPHONY_CDMA = "android.hardware.telephony.cdma";
    public static final String FEATURE_TELEPHONY_GSM = "android.hardware.telephony.gsm";
    public static final String FEATURE_USB_HOST = "android.hardware.usb.host";
    public static final String FEATURE_USB_ACCESSORY = "android.hardware.usb.accessory";
    public static final String FEATURE_SIP = "android.software.sip";
    public static final String FEATURE_SIP_VOIP = "android.software.sip.voip";
    public static final String FEATURE_TOUCHSCREEN = "android.hardware.touchscreen";
    public static final String FEATURE_TOUCHSCREEN_MULTITOUCH = "android.hardware.touchscreen.multitouch";
    public static final String FEATURE_TOUCHSCREEN_MULTITOUCH_DISTINCT = "android.hardware.touchscreen.multitouch.distinct";
    public static final String FEATURE_TOUCHSCREEN_MULTITOUCH_JAZZHAND = "android.hardware.touchscreen.multitouch.jazzhand";
    public static final String FEATURE_FAKETOUCH = "android.hardware.faketouch";
    public static final String FEATURE_FAKETOUCH_MULTITOUCH_DISTINCT = "android.hardware.faketouch.multitouch.distinct";
    public static final String FEATURE_FAKETOUCH_MULTITOUCH_JAZZHAND = "android.hardware.faketouch.multitouch.jazzhand";
    public static final String FEATURE_SCREEN_PORTRAIT = "android.hardware.screen.portrait";
    public static final String FEATURE_SCREEN_LANDSCAPE = "android.hardware.screen.landscape";
    public static final String FEATURE_LIVE_WALLPAPER = "android.software.live_wallpaper";
    public static final String FEATURE_WIFI = "android.hardware.wifi";
    public static final String FEATURE_WIFI_DIRECT = "android.hardware.wifi.direct";
    public static final String FEATURE_TELEVISION = "android.hardware.type.television";
    public static final String EXTRA_VERIFICATION_ID = "android.content.pm.extra.VERIFICATION_ID";
    public static final String EXTRA_VERIFICATION_RESULT = "android.content.pm.extra.VERIFICATION_RESULT";
    
    ApplicationInfo appInfo = new ApplicationInfo();

    public PackageManager() {
    	
    }

    public PackageInfo getPackageInfo(String var1, int var2) {
    	throw new RuntimeException("Stub!");
    }

    public String[] currentToCanonicalPackageNames(String[] var1) {
    	throw new RuntimeException("Stub!");
    }
    

    public String[] canonicalToCurrentPackageNames(String[] var1) {
    	throw new RuntimeException("Stub!");
    }

    public Intent getLaunchIntentForPackage(String var1) {
    	throw new RuntimeException("Stub!");
    }

    public ApplicationInfo getApplicationInfo(String var1, int var2) {
    	return appInfo;
    }

    public int checkPermission(String var1, String var2) {
    	throw new RuntimeException("Stub!");
    }

    public boolean addPermission(PermissionInfo var1) {
    	throw new RuntimeException("Stub!");
    }

    public boolean addPermissionAsync(PermissionInfo var1) {
    	throw new RuntimeException("Stub!");
    }

    public void removePermission(String var1) {
    	throw new RuntimeException("Stub!");
    }

    public int checkSignatures(String var1, String var2) {
    	throw new RuntimeException("Stub!");
    }

    public int checkSignatures(int var1, int var2) {
    	throw new RuntimeException("Stub!");
    }

    public String[] getPackagesForUid(int var1) {
    	throw new RuntimeException("Stub!");
    }

    public String getNameForUid(int var1) {
    	throw new RuntimeException("Stub!");
    }

    public List<ApplicationInfo> getInstalledApplications(int var1) {
    	throw new RuntimeException("Stub!");
    }

    public String[] getSystemSharedLibraryNames() {
    	throw new RuntimeException("Stub!");
    }

    public FeatureInfo[] getSystemAvailableFeatures() {
    	throw new RuntimeException("Stub!");
    }

    public boolean hasSystemFeature(String var1) {
    	throw new RuntimeException("Stub!");
    }

    public ResolveInfo resolveActivity(Intent var1, int var2) {
    	throw new RuntimeException("Stub!");
    }

    public List<ResolveInfo> queryIntentActivities(Intent var1, int var2) {
    	List<ResolveInfo> list = new ArrayList<>();
    	if (var1.getAction().equals(Intent.ACTION_MAIN)) {
    		ResolveInfo ri = new ResolveInfo();
    		ri.activityInfo = new ActivityInfo();
    		ri.activityInfo.applicationInfo = new ApplicationInfo();
    		ri.activityInfo.applicationInfo.packageName = "com.android.vending";
    		list.add(ri); 
    	}
    	
    	return list;
    }

    public List<ResolveInfo> queryIntentActivityOptions(ComponentName var1, Intent[] var2, Intent var3, int var4) {
    	throw new RuntimeException("Stub!");
    }

    public List<ResolveInfo> queryBroadcastReceivers(Intent var1, int var2) {
    	throw new RuntimeException("Stub!");
    }

    public ResolveInfo resolveService(Intent var1, int var2) {
    	throw new RuntimeException("Stub!");
    }

    public List<ResolveInfo> queryIntentServices(Intent var1, int var2) {
    	throw new RuntimeException("Stub!");
    }

    public ProviderInfo resolveContentProvider(String var1, int var2) {
    	throw new RuntimeException("Stub!");
    }

    public List<ProviderInfo> queryContentProviders(String var1, int var2, int var3) {
    	throw new RuntimeException("Stub!");
    }

    public InstrumentationInfo getInstrumentationInfo(ComponentName var1, int var2) {
    	throw new RuntimeException("Stub!");
    }

    public List<InstrumentationInfo> queryInstrumentation(String var1, int var2) {
    	throw new RuntimeException("Stub!");
    }

    public Drawable getDrawable(String var1, int var2, ApplicationInfo var3) {
    	throw new RuntimeException("Stub!");
    }

    public Drawable getActivityIcon(ComponentName var1) {
    	throw new RuntimeException("Stub!");
    }

    public Drawable getActivityIcon(Intent var1) {
    	throw new RuntimeException("Stub!");
    }

    public Drawable getDefaultActivityIcon() {
    	throw new RuntimeException("Stub!");
    }

    public Drawable getApplicationIcon(ApplicationInfo var1) {
    	throw new RuntimeException("Stub!");
    }

    public Drawable getApplicationIcon(String var1) {
    	throw new RuntimeException("Stub!");
    }

    public Drawable getActivityLogo(ComponentName var1) {
    	throw new RuntimeException("Stub!");
    }

    public Drawable getActivityLogo(Intent var1) {
    	throw new RuntimeException("Stub!");
    }

    public Drawable getApplicationLogo(ApplicationInfo var1) {
    	throw new RuntimeException("Stub!");
    }

    public Drawable getApplicationLogo(String var1) {
    	throw new RuntimeException("Stub!");
    }

    public CharSequence getText(String var1, int var2, ApplicationInfo var3) {
    	throw new RuntimeException("Stub!");
    }

    public XmlResourceParser getXml(String var1, int var2, ApplicationInfo var3) {
    	throw new RuntimeException("Stub!");
    }

    public CharSequence getApplicationLabel(ApplicationInfo var1) {
    	throw new RuntimeException("Stub!");
    }

    public Resources getResourcesForActivity(ComponentName var1) {
    	throw new RuntimeException("Stub!");
    }

    public Resources getResourcesForApplication(ApplicationInfo var1) {
    	throw new RuntimeException("Stub!");
    }

    public Resources getResourcesForApplication(String var1) {
    	throw new RuntimeException("Stub!");
    }

    public PackageInfo getPackageArchiveInfo(String archiveFilePath, int flags) {
        throw new RuntimeException("Stub!");
    }

    public void verifyPendingInstall(int var1, int var2) {
    	throw new RuntimeException("Stub!");
    }

    public void extendVerificationTimeout(int var1, int var2, long var3) {
    	throw new RuntimeException("Stub!");
    }

    public void setInstallerPackageName(String var1, String var2) {
    	throw new RuntimeException("Stub!");
    }

    public String getInstallerPackageName(String var1) {
    	throw new RuntimeException("Stub!");
    }

    /** @deprecated */
    @Deprecated
    public void addPackageToPreferred(String var1) {
    	throw new RuntimeException("Stub!");
    }

    /** @deprecated */
    @Deprecated
    public void removePackageFromPreferred(String var1) {
    	throw new RuntimeException("Stub!");
    }

    public List<PackageInfo> getPreferredPackages(int var1) {
    	throw new RuntimeException("Stub!");
    }

    /** @deprecated */
    @Deprecated
    public void addPreferredActivity(IntentFilter var1, int var2, ComponentName[] var3, ComponentName var4) {
    	throw new RuntimeException("Stub!");
    }

    public void clearPackagePreferredActivities(String var1) {
    	throw new RuntimeException("Stub!");
    }

    public int getPreferredActivities(List<IntentFilter> var1, List<ComponentName> var2, String var3) {
    	throw new RuntimeException("Stub!");
    }

    public void setComponentEnabledSetting(ComponentName var1, int var2, int var3) {
    	throw new RuntimeException("Stub!");
    }

    public int getComponentEnabledSetting(ComponentName var1) {
    	throw new RuntimeException("Stub!");
    }

    public void setApplicationEnabledSetting(String var1, int var2, int var3) {
    	throw new RuntimeException("Stub!");
    }

    public int getApplicationEnabledSetting(String var1) {
    	throw new RuntimeException("Stub!");
    }

    public boolean isSafeMode() {
    	throw new RuntimeException("Stub!");
    }

    public static class NameNotFoundException {
        public NameNotFoundException() {
            throw new RuntimeException("Stub!");
        }

        public NameNotFoundException(String name) {
            throw new RuntimeException("Stub!");
        }
    }
}
