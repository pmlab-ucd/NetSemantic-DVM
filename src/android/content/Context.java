package android.content;

import android.app.Application;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.telephony.TelephonyManager;
import android.view.WindowManager;
import patdroid.core.ClassInfo;
import fu.hao.trust.dvm.DVMObject;
import fu.hao.trust.dvm.DalvikVM;

public class Context extends DVMObject {

	public static final int MODE_APPEND = 32768;
	public static final int MODE_MULTI_PROCESS = 4;
	public static final int MODE_ENABLE_WRITE_AHEAD_LOGGING = 8;
	public static final int BIND_AUTO_CREATE = 1;
	public static final int BIND_DEBUG_UNBIND = 2;
	public static final int BIND_NOT_FOREGROUND = 4;
	public static final int BIND_ABOVE_CLIENT = 8;
	public static final int BIND_ALLOW_OOM_MANAGEMENT = 16;
	public static final int BIND_WAIVE_PRIORITY = 32;
	public static final int BIND_IMPORTANT = 64;
	public static final int BIND_ADJUST_WITH_ACTIVITY = 128;
	public static final String POWER_SERVICE = "power";
	public static final String WINDOW_SERVICE = "window";
	public static final String LAYOUT_INFLATER_SERVICE = "layout_inflater";
	public static final String ACCOUNT_SERVICE = "account";
	public static final String ACTIVITY_SERVICE = "activity";
	public static final String ALARM_SERVICE = "alarm";
	public static final String NOTIFICATION_SERVICE = "notification";
	public static final String ACCESSIBILITY_SERVICE = "accessibility";
	public static final String KEYGUARD_SERVICE = "keyguard";
	public static final String LOCATION_SERVICE = "location";
	public static final String SEARCH_SERVICE = "search";
	public static final String SENSOR_SERVICE = "sensor";
	public static final String STORAGE_SERVICE = "storage";
	public static final String WALLPAPER_SERVICE = "wallpaper";
	public static final String VIBRATOR_SERVICE = "vibrator";
	public static final String CONNECTIVITY_SERVICE = "connectivity";
	public static final String WIFI_SERVICE = "wifi";
	public static final String WIFI_P2P_SERVICE = "wifip2p";
	public static final String NSD_SERVICE = "servicediscovery";
	public static final String AUDIO_SERVICE = "audio";
	public static final String MEDIA_ROUTER_SERVICE = "media_router";
	public static final String TELEPHONY_SERVICE = "phone";
	public static final String CLIPBOARD_SERVICE = "clipboard";
	public static final String INPUT_METHOD_SERVICE = "input_method";
	public static final String TEXT_SERVICES_MANAGER_SERVICE = "textservices";
	public static final String DROPBOX_SERVICE = "dropbox";
	public static final String DEVICE_POLICY_SERVICE = "device_policy";
	public static final String UI_MODE_SERVICE = "uimode";
	public static final String DOWNLOAD_SERVICE = "download";
	public static final String NFC_SERVICE = "nfc";
	public static final String USB_SERVICE = "usb";
	public static final String INPUT_SERVICE = "input";
	public static final String DISPLAY_SERVICE = "display";
	public static final String USER_SERVICE = "user";
	public static final int CONTEXT_INCLUDE_CODE = 1;
	public static final int CONTEXT_IGNORE_SECURITY = 2;
	public static final int CONTEXT_RESTRICTED = 4;

	Resources resources = new Resources();;
	PackageManager packageManager = new PackageManager();

	public Context(DalvikVM vm, ClassInfo type) {
		super(vm, type);
	}

	public void sendBroadcast(Intent paramIntent) {

	}

	public DalvikVM getVM() {
		return vm;
	}

	public Resources getResources() {
		return resources;
	}

	public Object getSystemService(String name) {
		switch (name) {
		case "location":
			return new LocationManager();
		case "phone":
			return new TelephonyManager();
		case "windown":
			return new WindowManager();
		case "connectivity":
			return new ConnectivityManager();
		}

		return null;
	}

	public Context getApplicationContext() {
		return this;
	}

	public PackageManager getPackageManager() {
		return packageManager;
	}

	public Application getApplication() {
		return vm.getApplication();
	}

}
