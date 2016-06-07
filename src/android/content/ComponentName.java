package android.content;

import fu.hao.trust.utils.Settings;
import android.os.Parcel;
//import android.os.Parcelable.Creator;

public class ComponentName {
    //public static final Creator<ComponentName> CREATOR = null;
	private String pkg = "";
	private String cls = "";
	private Context appContext;

    public ComponentName(String pkg, String cls) {
		this.pkg = pkg;
		this.cls = cls;
    }

    public ComponentName(Context pkg, String cls) {
    	setAppContext(pkg);
    	this.pkg = Settings.getApkName();
    	this.cls = cls;
    }

    public ComponentName(Context pkg, Class<?> cls) {
        throw new RuntimeException("Stub!");
    }

    public ComponentName(Parcel in) {
        throw new RuntimeException("Stub!");
    }

    public ComponentName clone() {
        throw new RuntimeException("Stub!");
    }

    public String getPackageName() {
    	return pkg;
    }

    public String getClassName() {
    	return cls;
    }

    public String getShortClassName() {
        throw new RuntimeException("Stub!");
    }

    public String flattenToString() {
        throw new RuntimeException("Stub!");
    }

    public String flattenToShortString() {
        throw new RuntimeException("Stub!");
    }

    public static ComponentName unflattenFromString(String str) {
        throw new RuntimeException("Stub!");
    }

    public String toShortString() {
        throw new RuntimeException("Stub!");
    }

    public String toString() {
    	return cls; 
    }

    public boolean equals(Object obj) {
        throw new RuntimeException("Stub!");
    }

    public int hashCode() {
    	return cls.hashCode();
    }

    public int compareTo(ComponentName that) {
        throw new RuntimeException("Stub!");
    }

    public int describeContents() {
        throw new RuntimeException("Stub!");
    }

    public void writeToParcel(Parcel out, int flags) {
        throw new RuntimeException("Stub!");
    }

    public static void writeToParcel(ComponentName c, Parcel out) {
        throw new RuntimeException("Stub!");
    }

    public static ComponentName readFromParcel(Parcel in) {
        throw new RuntimeException("Stub!");
    }

	public Context getAppContext() {
		return appContext;
	}

	public void setAppContext(Context appContext) {
		this.appContext = appContext;
	}

}
