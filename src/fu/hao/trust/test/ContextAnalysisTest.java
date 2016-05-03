package fu.hao.trust.test;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import fu.hao.trust.data.Results;
import fu.hao.trust.dvm.Main;
import fu.hao.trust.utils.Settings;

public class ContextAnalysisTest {
	
	String[] args = new String[4];
	
	@Before
	public void prepare() {
		args[3] = "Full";
		Object[] initArgs = new Object[1];
		initArgs[0] = "android.app.Activity";
		//initArgs[1] = "NULL";
		Main.initMI(initArgs);
	}

	@Test
	public void testCtx() {
		Settings.logLevel = 0;

		args[0] = "C:/Users/hao/workspace/PJApps/app/app-release.apk";
		args[1] = "fu.hao.pjapps.MainActivity";
		args[2] = "testCtx";
		
		Main.main(args);
		System.out.println("REs: " + Results.targetCallRes);
		assertEquals(false, Results.targetCallRes.isEmpty());
		assertEquals(
				"[<5 INVOKE,VIRTUAL,extra=[android.telephony.TelephonyManager/getDeviceId[], [4]]>]",
				Results.targetCallRes.values().iterator().next().getDepAPIs()
						.toString());
	}
	
	@Test
	public void testLoop() {
		Settings.logLevel = 0;

		args[0] = "C:/Users/hao/workspace/PJApps/app/app-release.apk";
		args[1] = "fu.hao.pjapps.MainActivity";
		args[2] = "testLoop";

		Main.main(args);
		System.out.println("REs: " + Results.targetCallRes);
		assertEquals(false, Results.targetCallRes.isEmpty());
		assertEquals(
				"[<5 INVOKE,VIRTUAL,extra=[android.telephony.TelephonyManager/getDeviceId[], [6]]>]",
				Results.targetCallRes.values().iterator().next().getDepAPIs()
						.toString());
	}
	
	@Test
	public void testCtx2() {
		Settings.logLevel = 0;

		args[0] = "C:/Users/hao/workspace/PJApps/app/app-release.apk";
		args[1] = "fu.hao.pjapps.MainActivity";
		args[2] = "testCtx2";

		Main.main(args);
		System.out.println("REs: " + Results.targetCallRes);
		assertEquals(false, Results.targetCallRes.isEmpty());
		assertEquals(
				"[<24 INVOKE,VIRTUAL,extra=[android.telephony.TelephonyManager/getSubscriberId[], [5]]>]",
				Results.targetCallRes.values().iterator().next().getDepAPIs()
						.toString());
	}
	
	@Test
	public void testCondRet() {
		Settings.logLevel = 0;

		args[0] = "C:/Users/hao/workspace/PJApps/app/app-release.apk";
		args[1] = "fu.hao.pjapps.MainActivity";
		args[2] = "testCondRet";
		
		Main.main(args);
		System.out.println("REs: " + Results.targetCallRes);
		assertEquals(false, Results.targetCallRes.isEmpty());
		assertEquals(
				"[<5 INVOKE,VIRTUAL,extra=[android.telephony.TelephonyManager/getDeviceId[], [4]]>]",
				Results.targetCallRes.values().iterator().next().getDepAPIs()
						.toString());
	}
	
	@Test
	public void testEif2() {
		Settings.logLevel = 0;

		args[0] = "C:/Users/hao/workspace/PJApps/app/app-release.apk";
		args[1] = "fu.hao.pjapps.MainActivity";
		args[2] = "testEif2";

		Main.main(args);
		System.out.println("REs: " + Results.targetCallRes);
		assertEquals(false, Results.targetCallRes.isEmpty());
		assertEquals(
				"[<5 INVOKE,VIRTUAL,extra=[android.telephony.TelephonyManager/getDeviceId[], [3]]>]",
				Results.targetCallRes.values().iterator().next().getDepAPIs()
						.toString());
	}

}
