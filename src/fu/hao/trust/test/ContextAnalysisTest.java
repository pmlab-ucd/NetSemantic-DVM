package fu.hao.trust.test;

import static org.junit.Assert.*;

import org.junit.Test;

import fu.hao.trust.data.Results;
import fu.hao.trust.dvm.Main;
import fu.hao.trust.utils.Settings;

public class ContextAnalysisTest {
	
	String[] args = new String[4];

	@Test
	public void testCtx() {
		Settings.logLevel = 0;

		args[0] = "C:/Users/hao/workspace/PJApps/app/PJApps.apk";
		args[1] = "fu.hao.pjapps.MainActivity";
		args[2] = "testCtx";
		args[3] = "Ctx";

		Main.main(args);
		System.out.println("REs: " + Results.targetCallRes);
		assertEquals(false, Results.targetCallRes.isEmpty());
		assertEquals(
				"[<INVOKE,VIRTUAL,extra=[android.telephony.TelephonyManager/getDeviceId[], [4]]>]",
				Results.targetCallRes.values().iterator().next().getDepAPIs()
						.toString());
	}
	
	//@Test
	public void testCondRet() {
		Settings.logLevel = 0;

		args[0] = "C:/Users/hao/workspace/PJApps/app/PJApps.apk";
		args[1] = "fu.hao.pjapps.MainActivity";
		args[2] = "testCondRet";
		args[3] = "Ctx";
		
		Main.main(args);
		System.out.println("REs: " + Results.targetCallRes);
		assertEquals(false, Results.targetCallRes.isEmpty());
		assertEquals(
				"[<INVOKE,VIRTUAL,extra=[android.telephony.TelephonyManager/getDeviceId[], [4]]>]",
				Results.targetCallRes.values().iterator().next().getDepAPIs()
						.toString());
	}
	
	//@Test
	public void testEif2() {
		Settings.logLevel = 0;

		args[0] = "C:/Users/hao/workspace/PJApps/app/PJApps.apk";
		args[1] = "fu.hao.pjapps.MainActivity";
		args[2] = "testEif2";
		args[3] = "Ctx";

		Main.main(args);
		System.out.println("REs: " + Results.targetCallRes);
		assertEquals(false, Results.targetCallRes.isEmpty());
		assertEquals(
				"[<INVOKE,VIRTUAL,extra=[android.telephony.TelephonyManager/getDeviceId[], [4]]>]",
				Results.targetCallRes.values().iterator().next().getDepAPIs()
						.toString());
	}

}
