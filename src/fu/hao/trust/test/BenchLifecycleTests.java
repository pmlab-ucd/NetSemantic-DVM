package fu.hao.trust.test;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import android.telephony.TelephonyManager;
import fu.hao.trust.data.Results;
import fu.hao.trust.dvm.Main;
import fu.hao.trust.utils.Settings;

public class BenchLifecycleTests {
	String[] args = new String[4];
	Object[] initArgs = new Object[2];

	
	TelephonyManager tm;

	public BenchLifecycleTests() {
		Settings.logLevel = 0;
		args[3] = "Taint";
		tm = new TelephonyManager();
		initArgs[0] = "NULL";
		initArgs[1] = "NULL";
		Main.initMI(initArgs);
	}
	
	@Test
	public void testLifecycle_ActivityLifecycle2() {
		args[0] = "C:/Users/hao/workspace/DroidBenchProj/Lifecycle_ActivityLifecycle2/app/";
		Main.main(args);
		assertEquals(false, Results.results.isEmpty());
		Map<String, String> res = new HashMap<>();
		res.put("<android.telephony.SmsManager: void sendTextMessage(java.lang.String,java.lang.String,java.lang.String,android.app.PendingIntent,android.app.PendingIntent)>",
				"359874043116909");
		assertEquals(true, Results.results.contains(res));
	}
	
	@Test
	public void testLifecycle_ActivityLifecycle1() {
		args[0] = "C:/Users/hao/workspace/DroidBenchProj/Lifecycle_ActivityLifecycle1/app/";
		Main.main(args);
		assertEquals(false, Results.results.isEmpty());
		Map<String, String> res = new HashMap<>();
		res.put("<java.net.URL: java.net.URLConnection openConnection()>",
				"http://www.google.de/search?q=359874043116909");
		assertEquals(true, Results.results.contains(res));
	}
	
	@Test
	public void testLifecycle_ActivityLifecycle3() {
		args[0] = "C:/Users/hao/workspace/DroidBenchProj/Lifecycle_ActivityLifecycle3/app/";
		Main.main(args);
		assertEquals(false, Results.results.isEmpty());
		Map<String, String> res = new HashMap<>();
		res.put("<android.telephony.SmsManager: void sendTextMessage(java.lang.String,java.lang.String,java.lang.String,android.app.PendingIntent,android.app.PendingIntent)>",
				tm.getSubscriberId());
		assertEquals(true, Results.results.contains(res));
	}
	
	@Test
	public void testLifecycle_ActivityLifecycle4() {
		args[0] = "C:/Users/hao/workspace/DroidBenchProj/Lifecycle_ActivityLifecycle4/app/";
		Main.main(args);
		assertEquals(false, Results.results.isEmpty());
		Map<String, String> res = new HashMap<>();
		res.put("<android.telephony.SmsManager: void sendTextMessage(java.lang.String,java.lang.String,java.lang.String,android.app.PendingIntent,android.app.PendingIntent)>",
				"359874043116909");
		assertEquals(true, Results.results.contains(res));
	}
	
	@Test
	public void testLifecycle_ApplicationLifecycle1() {
		args[0] = "C:/Users/hao/workspace/DroidBenchProj/Lifecycle_ApplicationLifecycle1/app/";
		Main.main(args);
		assertEquals(false, Results.results.isEmpty());
		Map<String, String> res = new HashMap<>();
		res.put("<android.telephony.SmsManager: void sendTextMessage(java.lang.String,java.lang.String,java.lang.String,android.app.PendingIntent,android.app.PendingIntent)>",
				"359874043116909");
		assertEquals(true, Results.results.contains(res));
	}
	
	@Test
	public void testLifecycle_ApplicationLifecycle2() {
		args[0] = "C:/Users/hao/workspace/DroidBenchProj/Lifecycle_ApplicationLifecycle2/app/";
		Main.main(args);
		assertEquals(false, Results.results.isEmpty());
		Map<String, String> res = new HashMap<>();
		res.put("<android.telephony.SmsManager: void sendTextMessage(java.lang.String,java.lang.String,java.lang.String,android.app.PendingIntent,android.app.PendingIntent)>",
				"359874043116909");
		assertEquals(true, Results.results.contains(res));
	}
	
	@Test
	public void testLifecycle_ApplicationLifecycle3() {
		args[0] = "C:/Users/hao/workspace/DroidBenchProj/Lifecycle_ApplicationLifecycle3/app/";
		Main.main(args);
		assertEquals(false, Results.results.isEmpty());
		Map<String, String> res = new HashMap<>();
		res.put("<android.telephony.SmsManager: void sendTextMessage(java.lang.String,java.lang.String,java.lang.String,android.app.PendingIntent,android.app.PendingIntent)>",
				"359874043116909");
		assertEquals(true, Results.results.contains(res));
	}
	
	@Test
	public void testLifecycle_BroadcastReceiverLifecycle1() {
		args[0] = "C:/Users/hao/workspace/DroidBenchProj/Lifecycle_BroadcastReceiverLifecycle1/app/";
		Main.main(args);
		assertEquals(false, Results.results.isEmpty());
		Map<String, String> res = new HashMap<>();
		res.put("<android.telephony.SmsManager: void sendTextMessage(java.lang.String,java.lang.String,java.lang.String,android.app.PendingIntent,android.app.PendingIntent)>",
				"359874043116909");
		assertEquals(true, Results.results.contains(res));
	}
	
	@Test
	public void testLifecycle_ServiceLifecycle1() {
		args[0] = "C:/Users/hao/workspace/DroidBenchProj/Lifecycle_ServiceLifecycle1/app/";
		Main.main(args);
		assertEquals(false, Results.results.isEmpty());
		Map<String, String> res = new HashMap<>();
		res.put("<android.telephony.SmsManager: void sendTextMessage(java.lang.String,java.lang.String,java.lang.String,android.app.PendingIntent,android.app.PendingIntent)>",
				tm.getSimSerialNumber());
		assertEquals(true, Results.results.contains(res));
	}

}
