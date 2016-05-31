package fu.hao.trust.test;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import android.telephony.TelephonyManager;
import fu.hao.trust.data.Results;
import fu.hao.trust.dvm.Main;
import fu.hao.trust.utils.Settings;

public class BenchThreadingTests {
	String[] args = new String[4];
	Object[] initArgs = new Object[2];

	
	TelephonyManager tm;

	public BenchThreadingTests() {
		Settings.logLevel = 0;
		args[3] = "Taint";
		tm = new TelephonyManager();
	}

	@Test
	public void testThreading_Executor1() {
		args[0] = "C:/Users/hao/workspace/DroidBenchProj/Executor1/app/";
		
		args[1] = "srcEventChains";
		Main.main(args);
		
		args[1] = "sinkEventChains";
		Main.main(args);
		assertEquals(false, Results.results.isEmpty());
		Map<String, String> res = new HashMap<>();
		res.put("<android.telephony.SmsManager: void sendTextMessage(java.lang.String,java.lang.String,java.lang.String,android.app.PendingIntent,android.app.PendingIntent)>",
				tm.getDeviceId());
		assertEquals(true, Results.results.contains(res));
	}
	
	@Test
	public void testThreading_AsyncTask1() {
		args[0] = "C:/Users/hao/workspace/DroidBenchProj/AsyncTask1/app/";
		
		args[1] = "srcEventChains";
		Main.main(args);
		
		args[1] = "sinkEventChains";
		Main.main(args);
		assertEquals(false, Results.results.isEmpty());
		Map<String, String> res = new HashMap<>();
		res.put("<android.telephony.SmsManager: void sendTextMessage(java.lang.String,java.lang.String,java.lang.String,android.app.PendingIntent,android.app.PendingIntent)>",
				tm.getDeviceId());
		assertEquals(true, Results.results.contains(res));
	}

}
