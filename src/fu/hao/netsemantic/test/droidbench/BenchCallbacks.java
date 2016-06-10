package fu.hao.netsemantic.test.droidbench;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import android.telephony.TelephonyManager;
import fu.hao.trust.data.Results;
import fu.hao.trust.dvm.Main;
import fu.hao.trust.utils.Settings;

public class BenchCallbacks {
	
	String[] args = new String[4];
	
	TelephonyManager tm;

	public BenchCallbacks() {
		Settings.logLevel = 0;
		args[3] = "ATaint";
	}

	@Test
	public void AnonymousClass1() {
		args[0] = "C:/Users/hao/workspace/DroidBenchProj/AnonymousClass1/app/";
		args[1] = "srcEventChains";
		Main.main(args);
		
		assertEquals(false, Results.results.isEmpty());
		Map<String, String> res = new HashMap<>();
		res.put("<android.telephony.SmsManager: void sendTextMessage(java.lang.String,java.lang.String,java.lang.String,android.app.PendingIntent,android.app.PendingIntent)>",
				"Latitude: 38.53203Longtitude: -121.759603");
		assertEquals(true, Results.results.contains(res));
	}
	
	@Test
	public void RegisterGlobal1() {
		args[0] = "C:/Users/hao/workspace/DroidBenchProj/RegisterGlobal1/app/";
		args[1] = "srcEventChains";
		Main.main(args);
		
		assertEquals(false, Results.results.isEmpty());
		Map<String, String> res = new HashMap<>();
		res.put("<android.telephony.SmsManager: void sendTextMessage(java.lang.String,java.lang.String,java.lang.String,android.app.PendingIntent,android.app.PendingIntent)>",
				"359874043116909");
		assertEquals(true, Results.results.contains(res));
	}
	
	@Test
	public void RegisterGlobal2() {
		args[0] = "C:/Users/hao/workspace/DroidBenchProj/RegisterGlobal2/app/";
		args[1] = "srcEventChains";
		Main.main(args);
		
		assertEquals(false, Results.results.isEmpty());
		Map<String, String> res = new HashMap<>();
		res.put("<android.telephony.SmsManager: void sendTextMessage(java.lang.String,java.lang.String,java.lang.String,android.app.PendingIntent,android.app.PendingIntent)>",
				"359874043116909");
		assertEquals(true, Results.results.contains(res));
	}

}
