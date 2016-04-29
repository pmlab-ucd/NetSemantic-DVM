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
	public void test2c2f4aeaa7861d9f5e707edeb7eb71b77a2ee809_execTask() {
		String[] args = new String[4];
		Settings.logLevel = 0;

		args[0] = "C:/Users/hao/workspace/DroidBenchProj/apks/pjapps/2c2f4aeaa7861d9f5e707edeb7eb71b77a2ee809/2c2f4aeaa7861d9f5e707edeb7eb71b77a2ee809.apk";
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
	
	
	@Test
	public void test29868b672787fda1f49c557597496a585a55f480_execTask() {
		String[] args = new String[4];
		Settings.logLevel = 0;

		args[0] = "C:/Users/hao/workspace/DroidBenchProj/apks/pjapps/29868b672787fda1f49c557597496a585a55f480/29868b672787fda1f49c557597496a585a55f480.apk";
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
	
	@Test
	public void test3609ff6a7b476b73f6cc3e0e33cf845c01e321d2_execTask() {
		String[] args = new String[4];
		Settings.logLevel = 0;

		args[0] = "C:/Users/hao/workspace/DroidBenchProj/apks/pjapps/3609ff6a7b476b73f6cc3e0e33cf845c01e321d2/3609ff6a7b476b73f6cc3e0e33cf845c01e321d2.apk";
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
	
	@Test
	public void test75a79a3ad1ff291ee1d9c614010ba17ae9f09255_execTask() {
		String[] args = new String[4];
		Settings.logLevel = 0;

		args[0] = "C:/Users/hao/workspace/DroidBenchProj/apks/pjapps/75a79a3ad1ff291ee1d9c614010ba17ae9f09255/75a79a3ad1ff291ee1d9c614010ba17ae9f09255.apk";
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
	
	@Test
	public void testa7db6728db3b8b793ca6513275bce2304a58090c_execTask() {
		String[] args = new String[4];
		Settings.logLevel = 0;

		args[0] = "C:/Users/hao/workspace/DroidBenchProj/apks/pjapps/a7db6728db3b8b793ca6513275bce2304a58090c/a7db6728db3b8b793ca6513275bce2304a58090c.apk";
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
	public void testb2e9ec9308af1dc4845d6f768ecb031c899c718b_execTask() {
		String[] args = new String[4];
		Settings.logLevel = 0;

		args[0] = "C:/Users/hao/workspace/DroidBenchProj/apks/pjapps/b2e9ec9308af1dc4845d6f768ecb031c899c718b/b2e9ec9308af1dc4845d6f768ecb031c899c718b.apk";
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
	
	@Test
	public void testf051eeab57e42d569d298ad076c9fb47610e201e_execTask() {
		String[] args = new String[4];
		Settings.logLevel = 0;

		args[0] = "C:/Users/hao/workspace/DroidBenchProj/apks/pjapps/f051eeab57e42d569d298ad076c9fb47610e201e/f051eeab57e42d569d298ad076c9fb47610e201e.apk";
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
	
	@Test
	public void test50edccdca5bdfcfe8b81671306a957bd9867aacd_execTask() {
		String[] args = new String[4];
		Settings.logLevel = 0;

		args[0] = "C:/Users/hao/workspace/DroidBenchProj/apks/pjapps/50edccdca5bdfcfe8b81671306a957bd9867aacd/50edccdca5bdfcfe8b81671306a957bd9867aacd.apk";
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
	
	@Test
	public void test06c38ce66ab027778615b7713a7f250ed30e32bc_execTask() {
		String[] args = new String[4];
		Settings.logLevel = 0;

		args[0] = "C:/Users/hao/workspace/DroidBenchProj/apks/pjapps/06c38ce66ab027778615b7713a7f250ed30e32bc/06c38ce66ab027778615b7713a7f250ed30e32bc.apk";
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
	
	@Test
	public void test030b481d0f1014efa6f730bf4fcaff3d4b4c85ac_execTask() {
		String[] args = new String[4];
		Settings.logLevel = 0;

		args[0] = "C:/Users/hao/workspace/DroidBenchProj/apks/pjapps/030b481d0f1014efa6f730bf4fcaff3d4b4c85ac/030b481d0f1014efa6f730bf4fcaff3d4b4c85ac.apk";
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
	
	@Test
	public void test1c7313408c964b92a76a7e90b1364f52704955ac_execTask() {
		String[] args = new String[4];
		Settings.logLevel = 0;

		args[0] = "C:/Users/hao/workspace/DroidBenchProj/apks/pjapps/1c7313408c964b92a76a7e90b1364f52704955ac/1c7313408c964b92a76a7e90b1364f52704955ac.apk";
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
	
	@Test
	public void teste556761afb96ce447ec964eb945cec733c4413c2_execTask() {
		String[] args = new String[4];
		Settings.logLevel = 0;

		args[0] = "C:/Users/hao/workspace/DroidBenchProj/apks/pjapps/e556761afb96ce447ec964eb945cec733c4413c2/e556761afb96ce447ec964eb945cec733c4413c2.apk";
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
	
	@Test
	public void test87d1ef4a0a967979aa91e699140678d313d1b527_execTask() {
		String[] args = new String[4];
		Settings.logLevel = 0;

		args[0] = "C:/Users/hao/workspace/DroidBenchProj/apks/pjapps/87d1ef4a0a967979aa91e699140678d313d1b527/87d1ef4a0a967979aa91e699140678d313d1b527.apk";
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
	public void test3c0599b86adf69d7fc9a6cd27fee6d2ebdea2115_execTask() {
		String[] args = new String[4];
		Settings.logLevel = 0;

		args[0] = "C:/Users/hao/workspace/DroidBenchProj/apks/pjapps/3c0599b86adf69d7fc9a6cd27fee6d2ebdea2115/3c0599b86adf69d7fc9a6cd27fee6d2ebdea2115.apk";
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
	public void test0399faa155d4eef2135eef91a2573189e99b94b5_execTask() {
		String[] args = new String[4];
		Settings.logLevel = 0;

		args[0] = "C:/Users/hao/workspace/DroidBenchProj/apks/pjapps/0399faa155d4eef2135eef91a2573189e99b94b5/0399faa155d4eef2135eef91a2573189e99b94b5.apk";
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
	public void testc0254995b5fdaf6394771b69af67c89d85183823_execTask() {
		String[] args = new String[4];
		Settings.logLevel = 0;

		args[0] = "C:/Users/hao/workspace/DroidBenchProj/apks/pjapps/c0254995b5fdaf6394771b69af67c89d85183823/c0254995b5fdaf6394771b69af67c89d85183823.apk";
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
	public void test340dd8625a6a5eb0750a402d088141b3ee14225e_a() {
		String[] args = new String[4];
		Settings.logLevel = 0;

		args[0] = "C:/Users/hao/workspace/DroidBenchProj/apks/pjapps/340dd8625a6a5eb0750a402d088141b3ee14225e/340dd8625a6a5eb0750a402d088141b3ee14225e.apk";
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
		assertEquals(true, containsSms);
	}
	
	@Test
	public void testf7ce49349f79cf3bee0af2dbea25521ecb89a3b3_execTask() {
		String[] args = new String[4];
		Settings.logLevel = 0;

		args[0] = "C:/Users/hao/workspace/DroidBenchProj/apks/pjapps/f7ce49349f79cf3bee0af2dbea25521ecb89a3b3/f7ce49349f79cf3bee0af2dbea25521ecb89a3b3.apk";
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
	public void teste84eb4d521c858f088a798004925c6a19f579596_execTask() {
		String[] args = new String[4];
		Settings.logLevel = 0;

		args[0] = "C:/Users/hao/workspace/DroidBenchProj/apks/pjapps/e84eb4d521c858f088a798004925c6a19f579596/e84eb4d521c858f088a798004925c6a19f579596.apk";
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
	public void test97dc3d1e29f12e3b8fecc54f1b244d3a8910877b_execTask() {
		String[] args = new String[4];
		Settings.logLevel = 0;

		args[0] = "C:/Users/hao/workspace/DroidBenchProj/apks/pjapps/97dc3d1e29f12e3b8fecc54f1b244d3a8910877b/97dc3d1e29f12e3b8fecc54f1b244d3a8910877b.apk";
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
	public void test25bf619bbea49002e1b127b64d3baa86c85371d8_execTask() {
		String[] args = new String[4];
		Settings.logLevel = 0;

		args[0] = "C:/Users/hao/workspace/DroidBenchProj/apks/pjapps/25bf619bbea49002e1b127b64d3baa86c85371d8/25bf619bbea49002e1b127b64d3baa86c85371d8.apk";
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
	public void testcbc3e67ad8138febc4e4a6bf6c4c792f8c3db28e_execTask() {
		String[] args = new String[4];
		Settings.logLevel = 0;

		args[0] = "C:/Users/hao/workspace/DroidBenchProj/apks/pjapps/cbc3e67ad8138febc4e4a6bf6c4c792f8c3db28e/cbc3e67ad8138febc4e4a6bf6c4c792f8c3db28e.apk";
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
	public void testbd3abae103a788ef15283df01cd2b2f068113e60_execTask() {
		String[] args = new String[4];
		Settings.logLevel = 0;

		args[0] = "C:/Users/hao/workspace/DroidBenchProj/apks/pjapps/bd3abae103a788ef15283df01cd2b2f068113e60/bd3abae103a788ef15283df01cd2b2f068113e60.apk";
		args[1] = "com.android.ServiceCenterAddressAct";
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
	public void test745d9b1e0b903bee59e350159f6f3a4f3f773a92_execTask() {
		String[] args = new String[4];
		Settings.logLevel = 0;

		args[0] = "C:/Users/hao/workspace/DroidBenchProj/apks/pjapps/745d9b1e0b903bee59e350159f6f3a4f3f773a92/745d9b1e0b903bee59e350159f6f3a4f3f773a92.apk";
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
	public void test002a775c92338612b6e3fbf00ab157a353e48514_execTask() {
		String[] args = new String[4];
		Settings.logLevel = 0;

		args[0] = "C:/Users/hao/workspace/DroidBenchProj/apks/pjapps/002a775c92338612b6e3fbf00ab157a353e48514/002a775c92338612b6e3fbf00ab157a353e48514.apk";
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
	public void teste0399fdd481992bc049b6e9d765da7f007f89875_a() {
		String[] args = new String[4];
		Settings.logLevel = 0;

		args[0] = "C:/Users/hao/workspace/DroidBenchProj/apks/pjapps/e0399fdd481992bc049b6e9d765da7f007f89875/e0399fdd481992bc049b6e9d765da7f007f89875.apk";
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
	public void testda58fdfc0042315ab3393904ec602c6115d240a5_execTask() {
		String[] args = new String[4];
		Settings.logLevel = 0;

		args[0] = "C:/Users/hao/workspace/DroidBenchProj/apks/pjapps/da58fdfc0042315ab3393904ec602c6115d240a5/da58fdfc0042315ab3393904ec602c6115d240a5.apk";
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
	public void testf1c0fe98d3c569ceedf77e9b509b9f8c7ea1969f_execTask() {
		String[] args = new String[4];
		Settings.logLevel = 0;

		args[0] = "C:/Users/hao/workspace/DroidBenchProj/apks/pjapps/f1c0fe98d3c569ceedf77e9b509b9f8c7ea1969f/f1c0fe98d3c569ceedf77e9b509b9f8c7ea1969f.apk";
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
	
	@Test
	public void test9e5297a369c840efab2c1f108ea2850dd38391f4_a() {
		String[] args = new String[4];
		Settings.logLevel = 0;

		args[0] = "C:/Users/hao/workspace/DroidBenchProj/apks/pjapps/9e5297a369c840efab2c1f108ea2850dd38391f4/9e5297a369c840efab2c1f108ea2850dd38391f4.apk";
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
