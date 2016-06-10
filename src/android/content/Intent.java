package android.content;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import android.net.Uri;
import android.os.Bundle;
import fu.hao.trust.utils.Log;
import patdroid.core.ClassInfo;

public class Intent {
	public static final String ACTION_MAIN = "android.intent.action.MAIN";
	public static final String ACTION_VIEW = "android.intent.action.VIEW";
	public static final String ACTION_DEFAULT = "android.intent.action.VIEW";
	public static final String ACTION_ATTACH_DATA = "android.intent.action.ATTACH_DATA";
	public static final String ACTION_EDIT = "android.intent.action.EDIT";
	public static final String ACTION_INSERT_OR_EDIT = "android.intent.action.INSERT_OR_EDIT";
	public static final String ACTION_PICK = "android.intent.action.PICK";
	public static final String ACTION_CREATE_SHORTCUT = "android.intent.action.CREATE_SHORTCUT";
	public static final String EXTRA_SHORTCUT_INTENT = "android.intent.extra.shortcut.INTENT";
	public static final String EXTRA_SHORTCUT_NAME = "android.intent.extra.shortcut.NAME";
	public static final String EXTRA_SHORTCUT_ICON = "android.intent.extra.shortcut.ICON";
	public static final String EXTRA_SHORTCUT_ICON_RESOURCE = "android.intent.extra.shortcut.ICON_RESOURCE";
	public static final String ACTION_CHOOSER = "android.intent.action.CHOOSER";
	public static final String ACTION_GET_CONTENT = "android.intent.action.GET_CONTENT";
	public static final String ACTION_DIAL = "android.intent.action.DIAL";
	public static final String ACTION_CALL = "android.intent.action.CALL";
	public static final String ACTION_SENDTO = "android.intent.action.SENDTO";
	public static final String ACTION_SEND = "android.intent.action.SEND";
	public static final String ACTION_SEND_MULTIPLE = "android.intent.action.SEND_MULTIPLE";
	public static final String ACTION_ANSWER = "android.intent.action.ANSWER";
	public static final String ACTION_INSERT = "android.intent.action.INSERT";
	public static final String ACTION_DELETE = "android.intent.action.DELETE";
	public static final String ACTION_RUN = "android.intent.action.RUN";
	public static final String ACTION_SYNC = "android.intent.action.SYNC";
	public static final String ACTION_PICK_ACTIVITY = "android.intent.action.PICK_ACTIVITY";
	public static final String ACTION_SEARCH = "android.intent.action.SEARCH";
	public static final String ACTION_SYSTEM_TUTORIAL = "android.intent.action.SYSTEM_TUTORIAL";
	public static final String ACTION_WEB_SEARCH = "android.intent.action.WEB_SEARCH";
	public static final String ACTION_ALL_APPS = "android.intent.action.ALL_APPS";
	public static final String ACTION_SET_WALLPAPER = "android.intent.action.SET_WALLPAPER";
	public static final String ACTION_BUG_REPORT = "android.intent.action.BUG_REPORT";
	public static final String ACTION_FACTORY_TEST = "android.intent.action.FACTORY_TEST";
	public static final String ACTION_CALL_BUTTON = "android.intent.action.CALL_BUTTON";
	public static final String ACTION_VOICE_COMMAND = "android.intent.action.VOICE_COMMAND";
	public static final String ACTION_SEARCH_LONG_PRESS = "android.intent.action.SEARCH_LONG_PRESS";
	public static final String ACTION_POWER_USAGE_SUMMARY = "android.intent.action.POWER_USAGE_SUMMARY";
	public static final String ACTION_SCREEN_OFF = "android.intent.action.SCREEN_OFF";
	public static final String ACTION_SCREEN_ON = "android.intent.action.SCREEN_ON";
	public static final String ACTION_USER_PRESENT = "android.intent.action.USER_PRESENT";
	public static final String ACTION_TIME_TICK = "android.intent.action.TIME_TICK";
	public static final String ACTION_TIME_CHANGED = "android.intent.action.TIME_SET";
	public static final String ACTION_DATE_CHANGED = "android.intent.action.DATE_CHANGED";
	public static final String ACTION_TIMEZONE_CHANGED = "android.intent.action.TIMEZONE_CHANGED";
	public static final String ACTION_BOOT_COMPLETED = "android.intent.action.BOOT_COMPLETED";
	public static final String ACTION_CLOSE_SYSTEM_DIALOGS = "android.intent.action.CLOSE_SYSTEM_DIALOGS";
	public static final String ACTION_PACKAGE_INSTALL = "android.intent.action.PACKAGE_INSTALL";
	public static final String ACTION_PACKAGE_ADDED = "android.intent.action.PACKAGE_ADDED";
	public static final String ACTION_PACKAGE_REPLACED = "android.intent.action.PACKAGE_REPLACED";
	public static final String ACTION_PACKAGE_REMOVED = "android.intent.action.PACKAGE_REMOVED";
	public static final String ACTION_PACKAGE_CHANGED = "android.intent.action.PACKAGE_CHANGED";
	public static final String ACTION_PACKAGE_RESTARTED = "android.intent.action.PACKAGE_RESTARTED";
	public static final String ACTION_PACKAGE_DATA_CLEARED = "android.intent.action.PACKAGE_DATA_CLEARED";
	public static final String ACTION_UID_REMOVED = "android.intent.action.UID_REMOVED";
	public static final String ACTION_EXTERNAL_APPLICATIONS_AVAILABLE = "android.intent.action.EXTERNAL_APPLICATIONS_AVAILABLE";
	public static final String ACTION_EXTERNAL_APPLICATIONS_UNAVAILABLE = "android.intent.action.EXTERNAL_APPLICATIONS_UNAVAILABLE";
	public static final String ACTION_WALLPAPER_CHANGED = "android.intent.action.WALLPAPER_CHANGED";
	public static final String ACTION_CONFIGURATION_CHANGED = "android.intent.action.CONFIGURATION_CHANGED";
	public static final String ACTION_LOCALE_CHANGED = "android.intent.action.LOCALE_CHANGED";
	public static final String ACTION_BATTERY_CHANGED = "android.intent.action.BATTERY_CHANGED";
	public static final String ACTION_BATTERY_LOW = "android.intent.action.BATTERY_LOW";
	public static final String ACTION_BATTERY_OKAY = "android.intent.action.BATTERY_OKAY";
	public static final String ACTION_POWER_CONNECTED = "android.intent.action.ACTION_POWER_CONNECTED";
	public static final String ACTION_POWER_DISCONNECTED = "android.intent.action.ACTION_POWER_DISCONNECTED";
	public static final String ACTION_SHUTDOWN = "android.intent.action.ACTION_SHUTDOWN";
	public static final String ACTION_DEVICE_STORAGE_LOW = "android.intent.action.DEVICE_STORAGE_LOW";
	public static final String ACTION_DEVICE_STORAGE_OK = "android.intent.action.DEVICE_STORAGE_OK";
	public static final String ACTION_MANAGE_PACKAGE_STORAGE = "android.intent.action.MANAGE_PACKAGE_STORAGE";
	public static final String ACTION_UMS_CONNECTED = "android.intent.action.UMS_CONNECTED";
	public static final String ACTION_UMS_DISCONNECTED = "android.intent.action.UMS_DISCONNECTED";
	public static final String ACTION_MEDIA_REMOVED = "android.intent.action.MEDIA_REMOVED";
	public static final String ACTION_MEDIA_UNMOUNTED = "android.intent.action.MEDIA_UNMOUNTED";
	public static final String ACTION_MEDIA_CHECKING = "android.intent.action.MEDIA_CHECKING";
	public static final String ACTION_MEDIA_NOFS = "android.intent.action.MEDIA_NOFS";
	public static final String ACTION_MEDIA_MOUNTED = "android.intent.action.MEDIA_MOUNTED";
	public static final String ACTION_MEDIA_SHARED = "android.intent.action.MEDIA_SHARED";
	public static final String ACTION_MEDIA_BAD_REMOVAL = "android.intent.action.MEDIA_BAD_REMOVAL";
	public static final String ACTION_MEDIA_UNMOUNTABLE = "android.intent.action.MEDIA_UNMOUNTABLE";
	public static final String ACTION_MEDIA_EJECT = "android.intent.action.MEDIA_EJECT";
	public static final String ACTION_MEDIA_SCANNER_STARTED = "android.intent.action.MEDIA_SCANNER_STARTED";
	public static final String ACTION_MEDIA_SCANNER_FINISHED = "android.intent.action.MEDIA_SCANNER_FINISHED";
	public static final String ACTION_MEDIA_SCANNER_SCAN_FILE = "android.intent.action.MEDIA_SCANNER_SCAN_FILE";
	public static final String ACTION_MEDIA_BUTTON = "android.intent.action.MEDIA_BUTTON";
	public static final String ACTION_CAMERA_BUTTON = "android.intent.action.CAMERA_BUTTON";
	public static final String ACTION_GTALK_SERVICE_CONNECTED = "android.intent.action.GTALK_CONNECTED";
	public static final String ACTION_GTALK_SERVICE_DISCONNECTED = "android.intent.action.GTALK_DISCONNECTED";
	public static final String ACTION_INPUT_METHOD_CHANGED = "android.intent.action.INPUT_METHOD_CHANGED";
	public static final String ACTION_AIRPLANE_MODE_CHANGED = "android.intent.action.AIRPLANE_MODE";
	public static final String ACTION_PROVIDER_CHANGED = "android.intent.action.PROVIDER_CHANGED";
	public static final String ACTION_HEADSET_PLUG = "android.intent.action.HEADSET_PLUG";
	public static final String ACTION_NEW_OUTGOING_CALL = "android.intent.action.NEW_OUTGOING_CALL";
	public static final String ACTION_REBOOT = "android.intent.action.REBOOT";
	public static final String ACTION_DOCK_EVENT = "android.intent.action.DOCK_EVENT";
	public static final String CATEGORY_DEFAULT = "android.intent.category.DEFAULT";
	public static final String CATEGORY_BROWSABLE = "android.intent.category.BROWSABLE";
	public static final String CATEGORY_ALTERNATIVE = "android.intent.category.ALTERNATIVE";
	public static final String CATEGORY_SELECTED_ALTERNATIVE = "android.intent.category.SELECTED_ALTERNATIVE";
	public static final String CATEGORY_TAB = "android.intent.category.TAB";
	public static final String CATEGORY_LAUNCHER = "android.intent.category.LAUNCHER";
	public static final String CATEGORY_INFO = "android.intent.category.INFO";
	public static final String CATEGORY_HOME = "android.intent.category.HOME";
	public static final String CATEGORY_PREFERENCE = "android.intent.category.PREFERENCE";
	public static final String CATEGORY_DEVELOPMENT_PREFERENCE = "android.intent.category.DEVELOPMENT_PREFERENCE";
	public static final String CATEGORY_EMBED = "android.intent.category.EMBED";
	public static final String CATEGORY_MONKEY = "android.intent.category.MONKEY";
	public static final String CATEGORY_TEST = "android.intent.category.TEST";
	public static final String CATEGORY_UNIT_TEST = "android.intent.category.UNIT_TEST";
	public static final String CATEGORY_SAMPLE_CODE = "android.intent.category.SAMPLE_CODE";
	public static final String CATEGORY_OPENABLE = "android.intent.category.OPENABLE";
	public static final String CATEGORY_FRAMEWORK_INSTRUMENTATION_TEST = "android.intent.category.FRAMEWORK_INSTRUMENTATION_TEST";
	public static final String CATEGORY_CAR_DOCK = "android.intent.category.CAR_DOCK";
	public static final String CATEGORY_DESK_DOCK = "android.intent.category.DESK_DOCK";
	public static final String CATEGORY_CAR_MODE = "android.intent.category.CAR_MODE";
	public static final String EXTRA_TEMPLATE = "android.intent.extra.TEMPLATE";
	public static final String EXTRA_TEXT = "android.intent.extra.TEXT";
	public static final String EXTRA_STREAM = "android.intent.extra.STREAM";
	public static final String EXTRA_EMAIL = "android.intent.extra.EMAIL";
	public static final String EXTRA_CC = "android.intent.extra.CC";
	public static final String EXTRA_BCC = "android.intent.extra.BCC";
	public static final String EXTRA_SUBJECT = "android.intent.extra.SUBJECT";
	public static final String EXTRA_INTENT = "android.intent.extra.INTENT";
	public static final String EXTRA_TITLE = "android.intent.extra.TITLE";
	public static final String EXTRA_INITIAL_INTENTS = "android.intent.extra.INITIAL_INTENTS";
	public static final String EXTRA_KEY_EVENT = "android.intent.extra.KEY_EVENT";
	public static final String EXTRA_DONT_KILL_APP = "android.intent.extra.DONT_KILL_APP";
	public static final String EXTRA_PHONE_NUMBER = "android.intent.extra.PHONE_NUMBER";
	public static final String EXTRA_UID = "android.intent.extra.UID";
	public static final String EXTRA_DATA_REMOVED = "android.intent.extra.DATA_REMOVED";
	public static final String EXTRA_REPLACING = "android.intent.extra.REPLACING";
	public static final String EXTRA_ALARM_COUNT = "android.intent.extra.ALARM_COUNT";
	public static final String EXTRA_DOCK_STATE = "android.intent.extra.DOCK_STATE";
	public static final int EXTRA_DOCK_STATE_UNDOCKED = 0;
	public static final int EXTRA_DOCK_STATE_DESK = 1;
	public static final int EXTRA_DOCK_STATE_CAR = 2;
	public static final String METADATA_DOCK_HOME = "android.dock_home";
	public static final String EXTRA_REMOTE_INTENT_TOKEN = "android.intent.extra.remote_intent_token";
	public static final String EXTRA_CHANGED_COMPONENT_NAME = "android.intent.extra.changed_component_name";
	public static final String EXTRA_CHANGED_COMPONENT_NAME_LIST = "android.intent.extra.changed_component_name_list";
	public static final String EXTRA_CHANGED_PACKAGE_LIST = "android.intent.extra.changed_package_list";
	public static final String EXTRA_CHANGED_UID_LIST = "android.intent.extra.changed_uid_list";
	public static final int FLAG_GRANT_READ_URI_PERMISSION = 1;
	public static final int FLAG_GRANT_WRITE_URI_PERMISSION = 2;
	public static final int FLAG_FROM_BACKGROUND = 4;
	public static final int FLAG_DEBUG_LOG_RESOLUTION = 8;
	public static final int FLAG_ACTIVITY_NO_HISTORY = 1073741824;
	public static final int FLAG_ACTIVITY_SINGLE_TOP = 536870912;
	public static final int FLAG_ACTIVITY_NEW_TASK = 268435456;
	public static final int FLAG_ACTIVITY_MULTIPLE_TASK = 134217728;
	public static final int FLAG_ACTIVITY_CLEAR_TOP = 67108864;
	public static final int FLAG_ACTIVITY_FORWARD_RESULT = 33554432;
	public static final int FLAG_ACTIVITY_PREVIOUS_IS_TOP = 16777216;
	public static final int FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS = 8388608;
	public static final int FLAG_ACTIVITY_BROUGHT_TO_FRONT = 4194304;
	public static final int FLAG_ACTIVITY_RESET_TASK_IF_NEEDED = 2097152;
	public static final int FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY = 1048576;
	public static final int FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET = 524288;
	public static final int FLAG_ACTIVITY_NO_USER_ACTION = 262144;
	public static final int FLAG_ACTIVITY_REORDER_TO_FRONT = 131072;
	public static final int FLAG_ACTIVITY_NO_ANIMATION = 65536;
	public static final int FLAG_RECEIVER_REGISTERED_ONLY = 1073741824;
	public static final int FLAG_RECEIVER_REPLACE_PENDING = 536870912;
	public static final int URI_INTENT_SCHEME = 1;
	public static final int FILL_IN_ACTION = 1;
	public static final int FILL_IN_DATA = 2;
	public static final int FILL_IN_CATEGORIES = 4;
	public static final int FILL_IN_COMPONENT = 8;
	public static final int FILL_IN_PACKAGE = 16;
	public static final int FILL_IN_SOURCE_BOUNDS = 32;

	final String TAG = getClass().getSimpleName();

	String action = "";
	Map<String, Object> values; // The stored data
	private ClassInfo targetClass;
	Bundle bundle;
	Set<String> categories = new HashSet<>();
	Uri uri;

	public Intent(String action) {
		this.action = action;
		values = new HashMap<>();
	}

	public Intent() {
		values = new HashMap<>();
	}
	
    public Intent(String action, Uri uri) {
    	values = new HashMap<>();
    	this.action = action;
    	this.uri = uri;
    }

	public Intent(Context packageContext, Class<?> cls) {
		setTargetClass(ClassInfo.findClass(cls.getName()));
		values = new HashMap<>();
	}

	public Intent(Context packageContext, ClassInfo cls) {
		setTargetClass(cls);
		Log.bb(TAG, "New intent with target cls " + cls);
		values = new HashMap<>();
	}

	public Intent putExtra(String name, String value) {
		values.put(name, value);
		bundle = new Bundle(values);
		return this;
	}

	public String getStringExtra(String name) {
		return (String) values.get(name);
	}

	public String getAction() {
		return action;
	}

	@Override
	public String toString() {
		return "[intent:" + action + ", " + targetClass + ", " + values + "]";
	}

	public boolean hasExtra(String name) {
		return values.containsKey(name);
	}

	public Intent setAction(String action) {
		this.action = action;
		return this;
	}

	public ClassInfo getTargetClass() {
		return targetClass;
	}

	public void setTargetClass(ClassInfo targetClass) {
		this.targetClass = targetClass;
	}

	public Intent setComponent(ComponentName component) {
		targetClass = ClassInfo.findClass(component.getClassName());
		if (targetClass == null) {
			Log.err(TAG, "Cannot identify the targetClass!" + component);
		}

		return this;
	}

	public Bundle getExtras() {
		return bundle;
	}
	
    public Intent addCategory(String category) {
    	categories.add(category);
    	return this;
    }

    public void removeCategory(String category) {
    	categories.remove(category);
    }

}
