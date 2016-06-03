package fu.hao.trust.test;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import fu.hao.trust.data.Results;
import fu.hao.trust.dvm.Main;
import fu.hao.trust.utils.Settings;

public class BenchCallbackTests {
	String[] args = new String[4];

	public BenchCallbackTests() {
		Settings.logLevel = 0;
		args[3] = "Taint";
	}
	
	@Test
	public void testCallbacks_Button1() {
		args[0] = "C:/Users/hao/workspace/DroidBenchProj/Button1/app/";
		args[1] = "srcEventChains";
		Main.main(args);
		
		assertEquals(false, Results.results.isEmpty());
		Map<String, String> res = new HashMap<>();
		res.put("<android.telephony.SmsManager: void sendTextMessage(java.lang.String,java.lang.String,java.lang.String,android.app.PendingIntent,android.app.PendingIntent)>",
				"359874043116909");
		assertEquals(true, Results.results.contains(res));
	}
	
	@Test
	public void testCallbacks_Button2() {
		args[0] = "C:/Users/hao/workspace/DroidBenchProj/Button2/app/";
		args[1] = "srcEventChains";
		Main.main(args);
		
		assertEquals(false, Results.results.isEmpty());
		Map<String, String> res = new HashMap<>();
		res.put("<android.telephony.SmsManager: void sendTextMessage(java.lang.String,java.lang.String,java.lang.String,android.app.PendingIntent,android.app.PendingIntent)>",
				"359874043116909");
		assertEquals(true, Results.results.contains(res));
	}
	
	@Test
	public void testCallbacks_Button3() {
		args[0] = "C:/Users/hao/workspace/DroidBenchProj/Button3/app/";
		args[1] = "srcEventChains";
		Main.main(args);
		
		assertEquals(true, Results.results.isEmpty());
	}
	
	@Test
	public void testCallbacks_Button4() {
		args[0] = "C:/Users/hao/workspace/DroidBenchProj/Button4/app/";
		args[1] = "srcEventChains";
		Main.main(args);
		
		assertEquals(false, Results.results.isEmpty());
		Map<String, String> res = new HashMap<>();
		res.put("<android.telephony.SmsManager: void sendTextMessage(java.lang.String,java.lang.String,java.lang.String,android.app.PendingIntent,android.app.PendingIntent)>",
				"359874043116909");
		assertEquals(true, Results.results.contains(res));
	}
	
	@Test
	public void testCallbacks_Button5() {
		args[0] = "C:/Users/hao/workspace/DroidBenchProj/Button5/app/";
		args[1] = "srcEventChains";
		Main.main(args);
		
		assertEquals(false, Results.results.isEmpty());
		Map<String, String> res = new HashMap<>();
		res.put("<android.telephony.SmsManager: void sendTextMessage(java.lang.String,java.lang.String,java.lang.String,android.app.PendingIntent,android.app.PendingIntent)>",
				"[Unknown var:359874043116909, type: java.lang.String]");
		assertEquals(true, Results.results.contains(res));
	}
	
	@Test
	public void testCallbacks_Location1() {
		args[0] = "C:/Users/hao/workspace/DroidBenchProj/LocationLeak1/app/";
		args[1] = "srcEventChains";
		Main.main(args);
		
		assertEquals(false, Results.results.isEmpty());
		Map<String, String> res = new HashMap<>();
		res.put("<android.telephony.SmsManager: void sendTextMessage(java.lang.String,java.lang.String,java.lang.String,android.app.PendingIntent,android.app.PendingIntent)>",
				"[Unknown var:Longtitude: -121.759603, type: java.lang.String]");
		assertEquals(true, Results.results.contains(res));
	}
	
	@Test
	public void testCallbacks_Location2() {
		args[0] = "C:/Users/hao/workspace/DroidBenchProj/LocationLeak2/app/";
		args[1] = "srcEventChains";
		Main.main(args);
		
		assertEquals(false, Results.results.isEmpty());
		Map<String, String> res = new HashMap<>();
		res.put("<android.telephony.SmsManager: void sendTextMessage(java.lang.String,java.lang.String,java.lang.String,android.app.PendingIntent,android.app.PendingIntent)>",
				"[Unknown var:Longtitude: -121.759603, type: java.lang.String]");
		assertEquals(true, Results.results.contains(res));
	}
	
}
