package android.os;

import patdroid.util.Log;

public class Looper {
	
	private static String TAG = "Looper"; 
	
	public static Looper getMainLooper() {
		return new Looper();
	}

	public static void prepare() {
	}

	public static void loop() {
		Log.msg(TAG, "There is an Looper.loop() here.");
	}

}
