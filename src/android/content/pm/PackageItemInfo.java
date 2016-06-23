//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package android.content.pm;

import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcel;

import java.util.Comparator;

import fu.hao.trust.utils.Settings;

public class PackageItemInfo {
    public String name;
    public String packageName = Settings.getApkName();
    public int labelRes;
    public CharSequence nonLocalizedLabel;
    public int icon;
    public int logo;
    public Bundle metaData;

    public PackageItemInfo() {
    }

    public PackageItemInfo(PackageItemInfo orig) {
        throw new RuntimeException("Stub!");
    }

    protected PackageItemInfo(Parcel source) {
        throw new RuntimeException("Stub!");
    }

    public CharSequence loadLabel(PackageManager pm) {
    	return "";
    }

    public Drawable loadIcon(PackageManager pm) {
        throw new RuntimeException("Stub!");
    }

    public Drawable loadLogo(PackageManager pm) {
        throw new RuntimeException("Stub!");
    }

    public XmlResourceParser loadXmlMetaData(PackageManager pm, String name) {
        throw new RuntimeException("Stub!");
    }

    public void writeToParcel(Parcel dest, int parcelableFlags) {
        throw new RuntimeException("Stub!");
    }

    public static class DisplayNameComparator implements Comparator<PackageItemInfo> {
        public DisplayNameComparator(PackageManager pm) {
            throw new RuntimeException("Stub!");
        }

        public final int compare(PackageItemInfo aa, PackageItemInfo ab) {
            throw new RuntimeException("Stub!");
        }
    }
}
