package fu.hao.netsemantic.test.droidbench;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import android.telephony.TelephonyManager;
import fu.hao.trust.data.Results;
import fu.hao.trust.dvm.Main;
import fu.hao.trust.utils.Settings;

public class BenchReflectionTests {
	String[] args = new String[4];
	
	TelephonyManager tm;

	@Before
	public void prepare() {
		Settings.logLevel = 0;
		args[3] = "ATaint";
		tm = new TelephonyManager();
	}
	
	@Test
	public void Reflection1() {
		args[0] = "C:/Users/hao/workspace/DroidBenchProj/Reflection1/app/";
		args[1] = "srcEventChains";
		Main.main(args);
		assertEquals(false, Results.results.isEmpty());
		Map<String, String> res = new HashMap<>();
		res.put("<android.telephony.SmsManager: void sendTextMessage(java.lang.String,java.lang.String,java.lang.String,android.app.PendingIntent,android.app.PendingIntent)>",
				tm.getDeviceId());
		assertEquals(true, Results.results.contains(res));
	}
	
	@Test
	public void Reflection2() {
		args[0] = "C:/Users/hao/workspace/DroidBenchProj/Reflection2/app/";
		args[1] = "srcEventChains";
		Main.main(args);
		assertEquals(false, Results.results.isEmpty());
		Map<String, String> res = new HashMap<>();
		res.put("<android.telephony.SmsManager: void sendTextMessage(java.lang.String,java.lang.String,java.lang.String,android.app.PendingIntent,android.app.PendingIntent)>",
				tm.getDeviceId());
		assertEquals(true, Results.results.contains(res));
	}

}
