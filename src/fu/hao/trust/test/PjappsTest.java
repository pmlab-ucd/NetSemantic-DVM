package fu.hao.trust.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.zip.ZipException;

import org.junit.Test;

import patdroid.core.PrimitiveInfo;
import fu.hao.trust.data.Results;
import fu.hao.trust.data.TargetCall;
import fu.hao.trust.dvm.DalvikVM;
import fu.hao.trust.dvm.Main;
import fu.hao.trust.utils.Log;
import fu.hao.trust.utils.Settings;

public class PjappsTest {
	
	static String TAG = "test";
	
	public void test91deec899c6df09ef68f802979c2697d8a8803be() {
		String[] args = new String[4];
		Settings.logLevel = 0;

		args[0] = "C:/Users/hao/workspace/DroidBenchProj/apks/pjapps/91deec899c6df09ef68f802979c2697d8a8803be/91deec899c6df09ef68f802979c2697d8a8803be.apk";
		args[1] = "com.android.main.MainService";
		args[2] = "a";
		args[3] = "Full";

		Main.main(args);
		boolean containsSms = false;
		for (TargetCall targetCall : Results.targetCallRes.values()) {
			Log.msg(TAG, "Result: " + targetCall);
			if (targetCall.getInfluAPIs().toString().contains("SmsManager/sendTextMessage")) {
				containsSms = true;
			}
		}

		assertEquals(false, Results.targetCallRes.isEmpty());
		assertEquals(false, containsSms);
	}
	
	@Test
	public void testa7f33bd0441b5151f73fc7f1b30fbf35a9be76e0() {
		String[] args = new String[4];
		Settings.logLevel = 0;

		args[0] = "C:/Users/hao/workspace/DroidBenchProj/apks/pjapps/a7f33bd0441b5151f73fc7f1b30fbf35a9be76e0/a7f33bd0441b5151f73fc7f1b30fbf35a9be76e0.apk";
		args[1] = "com.android.main.MainService";
		args[2] = "execTask";
		args[3] = "Full";

		Main.main(args);
		boolean containsSms = false;
		for (TargetCall targetCall : Results.targetCallRes.values()) {
			Log.msg(TAG, "Result: " + targetCall);
			if (targetCall.getInfluAPIs().toString().contains("SmsManager/sendTextMessage")) {
				containsSms = true;
			}
		}

		assertEquals(false, Results.targetCallRes.isEmpty());
		assertEquals(true, containsSms);
	}
	
	
	@Test
	public void test7b4a2e82c0b61e2f260e60bbb2e5c16ea864ad46() {
		String[] args = new String[4];
		Settings.logLevel = 0;

		args[0] = "C:/Users/hao/workspace/DroidBenchProj/apks/pjapps/7b4a2e82c0b61e2f260e60bbb2e5c16ea864ad46/7b4a2e82c0b61e2f260e60bbb2e5c16ea864ad46.apk";
		args[1] = "com.android.main.MainService";
		args[2] = "execTask";
		args[3] = "Full";

		Main.main(args);
		boolean containsSms = false;
		for (TargetCall targetCall : Results.targetCallRes.values()) {
			Log.msg(TAG, "Result: " + targetCall);
			if (targetCall.getInfluAPIs().toString().contains("SmsManager/sendTextMessage")) {
				containsSms = true;
			}
		}

		assertEquals(false, Results.targetCallRes.isEmpty());
		assertEquals(true, containsSms);
	}
	
	@Test
	public void test2c47ec0a2eb6e88e910f1c16889f8cdedce09507() {
		String[] args = new String[4];
		Settings.logLevel = 0;

		args[0] = "C:/Users/hao/workspace/DroidBenchProj/apks/pjapps/2c47ec0a2eb6e88e910f1c16889f8cdedce09507/2c47ec0a2eb6e88e910f1c16889f8cdedce09507.apk";
		args[1] = "com.android.main.MainService";
		args[2] = "execTask";
		args[3] = "Full";

		Main.main(args);
		boolean containsSms = false;
		for (TargetCall targetCall : Results.targetCallRes.values()) {
			Log.msg(TAG, "Result: " + targetCall);
			if (targetCall.getInfluAPIs().toString().contains("SmsManager/sendTextMessage")) {
				containsSms = true;
			}
		}

		assertEquals(false, Results.targetCallRes.isEmpty());
		assertEquals(true, containsSms);
	}
	
	@Test
	public void test71d2f241f2cb8f4208dd3574df3c3ce0dacdd1c0() {
		String[] args = new String[4];
		Settings.logLevel = 0;

		args[0] = "C:/Users/hao/workspace/DroidBenchProj/apks/pjapps/71d2f241f2cb8f4208dd3574df3c3ce0dacdd1c0/71d2f241f2cb8f4208dd3574df3c3ce0dacdd1c0.apk";
		args[1] = "com.android.MainService";
		args[2] = "execTask";
		args[3] = "Full";

		Main.main(args);
		boolean containsSms = false;
		for (TargetCall targetCall : Results.targetCallRes.values()) {
			Log.msg(TAG, "Result: " + targetCall);
			if (targetCall.getInfluAPIs().toString().contains("SmsManager/sendTextMessage")) {
				containsSms = true;
			}
		}

		assertEquals(false, Results.targetCallRes.isEmpty());
		assertEquals(true, containsSms);
	}

	//@Test
	public void testcc41c23be6baa51a2d555f397b9ca9144939885f() {
		String[] args = new String[4];
		Settings.logLevel = 0;
		
		args[0] = "C:/Users/hao/workspace/DroidBenchProj/apks/pjapps/cc41c23be6baa51a2d555f397b9ca9144939885f/cc41c23be6baa51a2d555f397b9ca9144939885f.apk";
		args[1] = "com.android.main.MainService";
		args[2] = "execTask";
		args[3] = "Full";
		
		Main.main(args);
		Log.msg(TAG, "REs: " + Results.results);
		Log.msg(TAG, "REs: " + Results.targetCallRes);
		
		assertEquals(false, Results.targetCallRes.isEmpty());
		assertEquals(
				true,
				Results.targetCallRes
						.values()
						.iterator()
						.next()
						.getInfluAPIs()
						.toString()
						.contains(
								"android.telephony.gsm.SmsManager/sendTextMessage"));
	}
	
	//@Test
	public void test0177c2775de43572eb37e5de2803ff57eb297a9f() {
		String[] args = new String[4];
		Settings.logLevel = 0;
		
		args[0] = "C:/Users/hao/workspace/DroidBenchProj/apks/pjapps/0177c2775de43572eb37e5de2803ff57eb297a9f/0177c2775de43572eb37e5de2803ff57eb297a9f.apk";
		args[1] = "com.android.main.MainService";
		args[2] = "execTask";
		args[3] = "Full";
		
		Main.main(args);
		Log.msg(TAG, "REs: " + Results.results);
		Log.msg(TAG, "REs: " + Results.targetCallRes);
		
		assertEquals(false, Results.targetCallRes.isEmpty());
		assertEquals(
				true,
				Results.targetCallRes
						.values()
						.iterator()
						.next()
						.getInfluAPIs()
						.toString()
						.contains(
								"android.telephony.gsm.SmsManager/sendTextMessage"));
	}
	
	//@Test
	public void test() {
		String[] args = new String[4];
		Settings.logLevel = 0;
		
		args[0] = "C:/Users/hao/workspace/PJApps/app/663e8eb52c7b4a14e2873b1551748587018661b3.apk";
		args[1] = "com.android.main.MainService";
		args[2] = "execTask";
		args[3] = "Ctx";
		
		//Main.main(args);
		System.out.println("REs: " + Results.results);
		System.out.println("REs: " + Results.targetCallRes);
		
		DalvikVM vm = new DalvikVM();
		Object[] params = new Object[3];
		params[0] = "3lgoagdmfejekgfos9t15chojm";
		params[1] = new PrimitiveInfo(3); 
		params[2] = new PrimitiveInfo(3);
		try {
			vm.runMethod("C:/Users/hao/workspace/PJApps/app/663e8eb52c7b4a14e2873b1551748587018661b3.apk",
					"com.android.main.Base64", "encodebook", null, params);
		} catch (ZipException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	//@Test
	public void testEncryption() {
		DalvikVM vm = new DalvikVM();
		Object[] params = new Object[3];
		params[0] = "kl4ofgsmgeje5gko99s1fc2ofm";
		params[1] = new PrimitiveInfo(3); 
		params[2] = new PrimitiveInfo(3);
		try {
			vm.runMethod("C:/Users/hao/workspace/PJApps/app/663e8eb52c7b4a14e2873b1551748587018661b3.apk",
					"com.android.main.Base64", "encodebook", null, params);
		} catch (ZipException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testEncryption71d2f24() {
		DalvikVM vm = new DalvikVM();
		Object[] params = new Object[2];
		params[0] = "alfo3gsa3nfdsrfo3isd21d8a8fccosm";
		params[1] = new PrimitiveInfo(1); 
		try {
			vm.runMethod("C:/Users/hao/workspace/DroidBenchProj/apks/pjapps/71d2f241f2cb8f4208dd3574df3c3ce0dacdd1c0/71d2f241f2cb8f4208dd3574df3c3ce0dacdd1c0.apk",
					"com.android.Base64", "encode", null, params);
		} catch (ZipException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
