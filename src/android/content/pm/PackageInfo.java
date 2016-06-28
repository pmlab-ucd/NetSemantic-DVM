package android.content.pm;

import fu.hao.trust.utils.Settings;

public class PackageInfo {
    public String packageName;
    public int versionCode;
    public String versionName;
    public String sharedUserId;
    public int sharedUserLabel;
    public ApplicationInfo applicationInfo;
    public long firstInstallTime;
    public long lastUpdateTime;
    public int[] gids = null;
    public ActivityInfo[] activities = null;
    public ActivityInfo[] receivers = null;
    public ServiceInfo[] services = null;
    public ProviderInfo[] providers = null;
    public InstrumentationInfo[] instrumentation = null;
    public PermissionInfo[] permissions = null;
    public String[] requestedPermissions = null;
    public int[] requestedPermissionsFlags = null;
    public static final int REQUESTED_PERMISSION_REQUIRED = 1;
    public static final int REQUESTED_PERMISSION_GRANTED = 2;
    public Signature[] signatures = null;
    public ConfigurationInfo[] configPreferences = null;
    public FeatureInfo[] reqFeatures = null;
    //public static final Creator<PackageInfo> CREATOR = null;

    public PackageInfo() {
    	packageName = Settings.getApkName();
    	applicationInfo = new ApplicationInfo();
    }


    public int describeContents() {
        throw new RuntimeException("Stub!");
    }

}
