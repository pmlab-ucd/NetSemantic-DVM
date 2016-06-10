package android.content.pm;

import android.os.Parcel;

public class ComponentInfo extends PackageItemInfo {
	 public ApplicationInfo applicationInfo;
	    public String processName;
	    public int descriptionRes;
	    public boolean enabled;
	    public boolean exported;

	    public ComponentInfo() {
	    }

	    public ComponentInfo(ComponentInfo orig) {
	        throw new RuntimeException("Stub!");
	    }

	    protected ComponentInfo(Parcel source) {
	        throw new RuntimeException("Stub!");
	    }

	    public CharSequence loadLabel(PackageManager pm) {
	        throw new RuntimeException("Stub!");
	    }

	    public boolean isEnabled() {
	        throw new RuntimeException("Stub!");
	    }

	    public final int getIconResource() {
	        throw new RuntimeException("Stub!");
	    }


}
