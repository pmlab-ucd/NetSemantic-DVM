package fu.hao.trust.test;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import android.telephony.TelephonyManager;
import fu.hao.trust.data.Results;
import fu.hao.trust.dvm.Main;
import fu.hao.trust.dvm.ResolveIntent;
import fu.hao.trust.utils.Settings;

public class BenchICCTests {
	
	String[] args = new String[4];
	TelephonyManager tm;
	
	@Before
	public void prepare() {
		Settings.logLevel = 0;
		args[3] = "Taint";
		tm = new TelephonyManager();
	}

	@Test
	public void testActivityCommunication2() {
		args[0] = "C:/Users/hao/workspace/DroidBenchProj/ActivityCommunication2/app/";
		Settings.addCallBlkListElem("android.content.Context/startActivity");
		ResolveIntent.main(args);
		
		args[0] = "C:/Users/hao/workspace/DroidBenchProj/ActivityCommunication2/app/ActivityCommunication2.apk";
		args[1] = "edu.mit.icc_action_string_operations.OutFlowActivity";
		args[2] = "onCreate";
		Settings.reset();
		Main.main(args);
		Map<String, String> res = new HashMap<>();
		res.put("<android.telephony.SmsManager: void sendTextMessage(java.lang.String,java.lang.String,java.lang.String,android.app.PendingIntent,android.app.PendingIntent)>",
				"359874043116909");
		assertEquals(true, Results.results.contains(res));
	}

}
