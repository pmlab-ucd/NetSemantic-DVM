package fu.hao.trust.test;

import static org.junit.Assert.*;

import org.junit.Test;

import fu.hao.trust.analysis.Results;
import fu.hao.trust.dvm.Main;
import fu.hao.trust.utils.Settings;

public class InfluenceAnalysisTest {

	@Test
	public void testInfluence() {
		String[] args = new String[4];
		Settings.logLevel = 0;

		args[0] = "C:/Users/hao/workspace/PJApps/app/PJApps.apk";
		args[1] = "fu.hao.pjapps.MainActivity";
		args[2] = "testInfluence";
		args[3] = "Influ";

		Main.main(args);
	}

	@Test
	public void testConnection() {
		String[] args = new String[4];
		Settings.logLevel = 2;

		args[0] = "C:/Users/hao/workspace/PJApps/app/PJApps.apk";
		args[1] = "fu.hao.pjapps.MainActivity";
		args[2] = "testConnection";
		args[3] = "Full";
		Main.main(args);

		System.out.println("REs: " + Results.results);
		System.out.println("REs: " + Results.targetCallRes);
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
								"<INVOKE,VIRTUAL,extra=[android.telephony.SmsManager/sendTextMessage"));
	}

}
