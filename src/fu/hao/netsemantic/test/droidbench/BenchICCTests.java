package fu.hao.netsemantic.test.droidbench;

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
		args[3] = "ATaint";
		tm = new TelephonyManager();
	}
	
	@Test
	public void testActivityCommunication1() {	
		args[0] = "C:/Users/hao/workspace/DroidBenchProj/ActivityCommunication1/app/";
		args[1] = "srcEventChains";
		Main.main(args);
		
		assertEquals(false, Results.results.isEmpty());
		Map<String, String> res = new HashMap<>();
		res.put("<android.telephony.SmsManager: void sendTextMessage(java.lang.String,java.lang.String,java.lang.String,android.app.PendingIntent,android.app.PendingIntent)>",
				tm.getDeviceId());
		assertEquals(true, Results.results.contains(res));
	}

	@Test
	public void testActivityCommunication2() {
		args[0] = "C:/Users/hao/workspace/DroidBenchProj/ActivityCommunication2/app/";
		Settings.addCallBlkListElem("android.content.ContextWrapper/startActivity");
		ResolveIntent.main(args);
	
		// Call parse_xmls.py to get the intent targets
		
		args[0] = "C:/Users/hao/workspace/DroidBenchProj/ActivityCommunication2/app/";
		args[1] = "srcEventChains";
		Main.main(args);
		Map<String, String> res = new HashMap<>();
		res.put("<android.telephony.SmsManager: void sendTextMessage(java.lang.String,java.lang.String,java.lang.String,android.app.PendingIntent,android.app.PendingIntent)>",
				"359874043116909");
		assertEquals(true, Results.results.contains(res));
	}
	
	@Test
	public void testActivityCommunication3() {
		args[0] = "C:/Users/hao/workspace/DroidBenchProj/ActivityCommunication3/app/";
		args[1] = "srcEventChains";
		Main.main(args);
		Map<String, String> res = new HashMap<>();
		res.put("<android.telephony.SmsManager: void sendTextMessage(java.lang.String,java.lang.String,java.lang.String,android.app.PendingIntent,android.app.PendingIntent)>",
				"359874043116909");
		assertEquals(true, Results.results.contains(res));
	}

}
