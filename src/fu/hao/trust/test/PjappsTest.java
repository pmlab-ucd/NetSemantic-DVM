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
	
	@Test
	public void test5c52f10a83f344c1d68c3c79a4b306f667e62f0a_a() {
		String[] args = new String[4];
		Settings.logLevel = 0;

		args[0] = "C:/Users/hao/workspace/DroidBenchProj/apks/pjapps/5c52f10a83f344c1d68c3c79a4b306f667e62f0a/5c52f10a83f344c1d68c3c79a4b306f667e62f0a.apk";
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
	public void test001717460c5ba78085eee9b7ed58bec94759e4c8_execTask() {
		String[] args = new String[4];
		Settings.logLevel = 0;

		args[0] = "C:/Users/hao/workspace/DroidBenchProj/apks/pjapps/001717460c5ba78085eee9b7ed58bec94759e4c8/001717460c5ba78085eee9b7ed58bec94759e4c8.apk";
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
	public void testdc0955f5d3dac3758c468e97e5a656bb2c9c0467_execTask() {
		String[] args = new String[4];
		Settings.logLevel = 0;

		args[0] = "C:/Users/hao/workspace/DroidBenchProj/apks/pjapps/dc0955f5d3dac3758c468e97e5a656bb2c9c0467/dc0955f5d3dac3758c468e97e5a656bb2c9c0467.apk";
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
	public void test1fad7afd7ccae3c7f70b11a342e52d4bae2a2f7a_execTask() {
		String[] args = new String[4];
		Settings.logLevel = 0;

		args[0] = "C:/Users/hao/workspace/DroidBenchProj/apks/pjapps/1fad7afd7ccae3c7f70b11a342e52d4bae2a2f7a/1fad7afd7ccae3c7f70b11a342e52d4bae2a2f7a.apk";
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
	public void test49f327b5bbddee1206a7ab9be6bef541fc2ba874_execTask() {
		String[] args = new String[4];
		Settings.logLevel = 0;

		args[0] = "C:/Users/hao/workspace/DroidBenchProj/apks/pjapps/49f327b5bbddee1206a7ab9be6bef541fc2ba874/49f327b5bbddee1206a7ab9be6bef541fc2ba874.apk";
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
	public void teste2e24c5767f29dbb1c8daa41e1975a975d2ea4e4_execTask() {
		String[] args = new String[4];
		Settings.logLevel = 0;

		args[0] = "C:/Users/hao/workspace/DroidBenchProj/apks/pjapps/e2e24c5767f29dbb1c8daa41e1975a975d2ea4e4/e2e24c5767f29dbb1c8daa41e1975a975d2ea4e4.apk";
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
	public void test4096c6fef844652fdb8e68ba9455d1b86e4509ce_execTask() {
		String[] args = new String[4];
		Settings.logLevel = 0;

		args[0] = "C:/Users/hao/workspace/DroidBenchProj/apks/pjapps/4096c6fef844652fdb8e68ba9455d1b86e4509ce/4096c6fef844652fdb8e68ba9455d1b86e4509ce.apk";
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
	public void testf78c75dd78accb8afa62109563451c4375172507_execTask() {
		String[] args = new String[4];
		Settings.logLevel = 0;

		args[0] = "C:/Users/hao/workspace/DroidBenchProj/apks/pjapps/f78c75dd78accb8afa62109563451c4375172507/f78c75dd78accb8afa62109563451c4375172507.apk";
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
	public void test5f505727ff12e9ef180ddff4d254bf22884d6316_execTask() {
		String[] args = new String[4];
		Settings.logLevel = 0;

		args[0] = "C:/Users/hao/workspace/DroidBenchProj/apks/pjapps/5f505727ff12e9ef180ddff4d254bf22884d6316/5f505727ff12e9ef180ddff4d254bf22884d6316.apk";
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
	public void testa7e418c5f7ef2996781bcdcf638a0bd9b9ed3175_execTask() {
		String[] args = new String[4];
		Settings.logLevel = 0;

		args[0] = "C:/Users/hao/workspace/DroidBenchProj/apks/pjapps/a7e418c5f7ef2996781bcdcf638a0bd9b9ed3175/a7e418c5f7ef2996781bcdcf638a0bd9b9ed3175.apk";
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
	public void test17cb8f0fa3e1e63303e8230b8e62423fd3abee13_execTask() {
		String[] args = new String[4];
		Settings.logLevel = 0;

		args[0] = "C:/Users/hao/workspace/DroidBenchProj/apks/pjapps/17cb8f0fa3e1e63303e8230b8e62423fd3abee13/17cb8f0fa3e1e63303e8230b8e62423fd3abee13.apk";
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
	public void testdeda50965ca4c389dfad6b709bd4f0d9c5a9c5f7_execTask() {
		String[] args = new String[4];
		Settings.logLevel = 0;

		args[0] = "C:/Users/hao/workspace/DroidBenchProj/apks/pjapps/deda50965ca4c389dfad6b709bd4f0d9c5a9c5f7/deda50965ca4c389dfad6b709bd4f0d9c5a9c5f7.apk";
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
	public void test65567a9b079f513916eb9da07131c07fa103985d_execTask() {
		String[] args = new String[4];
		Settings.logLevel = 0;

		args[0] = "C:/Users/hao/workspace/DroidBenchProj/apks/pjapps/65567a9b079f513916eb9da07131c07fa103985d/65567a9b079f513916eb9da07131c07fa103985d.apk";
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
	public void testf1f8c997d77a6cf521a5f3adb9771d5c625f35cf_execTask() {
		String[] args = new String[4];
		Settings.logLevel = 0;

		args[0] = "C:/Users/hao/workspace/DroidBenchProj/apks/pjapps/f1f8c997d77a6cf521a5f3adb9771d5c625f35cf/f1f8c997d77a6cf521a5f3adb9771d5c625f35cf.apk";
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
	public void testdd830e1a37a73816f138cb7dca076224f39e2ee6_a() {
		String[] args = new String[4];
		Settings.logLevel = 0;

		args[0] = "C:/Users/hao/workspace/DroidBenchProj/apks/pjapps/dd830e1a37a73816f138cb7dca076224f39e2ee6/dd830e1a37a73816f138cb7dca076224f39e2ee6.apk";
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
	public void test95111f6a0c0955d51ad209a0c7465b6ff7a8af84_a() {
		String[] args = new String[4];
		Settings.logLevel = 0;

		args[0] = "C:/Users/hao/workspace/DroidBenchProj/apks/pjapps/95111f6a0c0955d51ad209a0c7465b6ff7a8af84/95111f6a0c0955d51ad209a0c7465b6ff7a8af84.apk";
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
	public void test0177c2775de43572eb37e5de2803ff57eb297a9f_defaultMark() {
		String[] args = new String[4];
		Settings.logLevel = 0;
		
		args[0] = "C:/Users/hao/workspace/DroidBenchProj/apks/pjapps/0177c2775de43572eb37e5de2803ff57eb297a9f/0177c2775de43572eb37e5de2803ff57eb297a9f.apk";
		args[1] = "com.android.main.MainService";
		args[2] = "defaultMark";
		args[3] = "Full";
		
		Main.main(args);
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
	
	@Test
	public void test764bdff985b515ca9207fe8f2fbcf41f7874b5d5_a() {
		String[] args = new String[4];
		Settings.logLevel = 0;

		args[0] = "C:/Users/hao/workspace/DroidBenchProj/apks/pjapps/764bdff985b515ca9207fe8f2fbcf41f7874b5d5/764bdff985b515ca9207fe8f2fbcf41f7874b5d5.apk";
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
	public void test91deec899c6df09ef68f802979c2697d8a8803be_a() {
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
	public void testa7f33bd0441b5151f73fc7f1b30fbf35a9be76e0_execTask() {
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
	
	@Test
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
