package android.content.pm;

import java.util.Comparator;

import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Parcel;

public class ResolveInfo {
	 public ActivityInfo activityInfo;
	    public ServiceInfo serviceInfo;
	    public IntentFilter filter;
	    public int priority;
	    public int preferredOrder;
	    public int match;
	    public int specificIndex;
	    public boolean isDefault;
	    public int labelRes;
	    public CharSequence nonLocalizedLabel;
	    public int icon;
	    public String resolvePackageName;

	    public ResolveInfo() {
	    }

	    public ResolveInfo(ResolveInfo orig) {
	        throw new RuntimeException("Stub!");
	    }

	    public CharSequence loadLabel(PackageManager pm) {
	        throw new RuntimeException("Stub!");
	    }

	    public Drawable loadIcon(PackageManager pm) {
	        throw new RuntimeException("Stub!");
	    }

	    public final int getIconResource() {
	        throw new RuntimeException("Stub!");
	    }

	    public int describeContents() {
	        throw new RuntimeException("Stub!");
	    }

	    public void writeToParcel(Parcel dest, int parcelableFlags) {
	        throw new RuntimeException("Stub!");
	    }

	    public static class DisplayNameComparator implements Comparator<ResolveInfo> {
	        public DisplayNameComparator(PackageManager pm) {
	            throw new RuntimeException("Stub!");
	        }

	        public final int compare(ResolveInfo a, ResolveInfo b) {
	            throw new RuntimeException("Stub!");
	        }
	    }
}
