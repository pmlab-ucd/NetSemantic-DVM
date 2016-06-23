//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package android.text.format;

import android.content.Context;

public final class Formatter {
    public Formatter() {
    }

    public static String formatFileSize(Context context, long number) {
    	return Long.toString(number);
    }

    public static String formatShortFileSize(Context context, long number) {
        return Long.toString(number);
    }

    /** @deprecated */
    @Deprecated
    public static String formatIpAddress(int ipv4Address) {
        throw new RuntimeException("Stub!");
    }
}
