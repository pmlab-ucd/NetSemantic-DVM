package fu.hao.trust.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.zip.ZipException;

import org.junit.Test;

import patdroid.core.PrimitiveInfo;
import fu.hao.trust.data.Results;
import fu.hao.trust.dvm.DalvikVM;
import fu.hao.trust.dvm.Main;
import fu.hao.trust.utils.Log;
import fu.hao.trust.utils.Settings;

public class PjappsTest {
	
	static String TAG = "test"; 

	@Test
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
	
	@Test
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
	
	@Test
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

}
