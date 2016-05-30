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
	}
	
	@Test
	public void testLifecycle_ActivityLifecycle2() {
		args[0] = "C:/Users/hao/workspace/DroidBenchProj/Lifecycle_ActivityLifecycle2/app/";
		args[1] = "de.ecspride.MainActivity";
		args[2] = "onResume";
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
		args[1] = "de.ecspride.ActivityLifecycle1";
		args[2] = "onStart";
		Main.main(args);
		assertEquals(false, Results.results.isEmpty());
		Map<String, String> res = new HashMap<>();
		//res.put("<java.net.URL: java.net.URLConnection openConnection()>",
		res.put("<java.net.URL: void <init>(java.lang.String)>",
				"http://www.google.de/search?q=359874043116909");
		assertEquals(true, Results.results.contains(res));
	}
	
	@Test
	public void testLifecycle_ActivityLifecycle3() {
		args[0] = "C:/Users/hao/workspace/DroidBenchProj/Lifecycle_ActivityLifecycle3/app/";
		args[1] = "de.ecspride.MainActivity";
		args[2] = "onRestoreInstanceState";
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
		args[1] = "de.ecspride.MainActivity";
		args[2] = "onPause";
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
		args[1] = "de.ecspride.MainActivity";
		args[2] = "onResume";
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
		args[1] = "de.ecspride.ApplicationLifecyle2";
		args[2] = "onLowMemory";
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
		args[1] = "de.ecspride.ApplicationLifecyle3";
		args[2] = "onCreate";
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
		args[1] = "de.ecspride.TestReceiver";
		args[2] = "onReceive";
		Main.main(args);
		assertEquals(false, Results.results.isEmpty());
		Map<String, String> res = new HashMap<>();
		res.put("<android.telephony.SmsManager: void sendTextMessage(java.lang.String,java.lang.String,java.lang.String,android.app.PendingIntent,android.app.PendingIntent)>",
				tm.getDeviceId());
		assertEquals(true, Results.results.contains(res));
	}
	
	@Test
	public void testLifecycle_ServiceLifecycle1() {
		args[0] = "C:/Users/hao/workspace/DroidBenchProj/Lifecycle_ServiceLifecycle1/app/";
		args[1] = "de.ecspride.MainService";
		args[2] = "onLowMemory";
		Main.main(args);
		assertEquals(false, Results.results.isEmpty());
		Map<String, String> res = new HashMap<>();
		res.put("<android.telephony.SmsManager: void sendTextMessage(java.lang.String,java.lang.String,java.lang.String,android.app.PendingIntent,android.app.PendingIntent)>",
				tm.getSimSerialNumber());
		assertEquals(true, Results.results.contains(res));
	}
	
	@Test
	public void testLifecycle_ServiceLifecycle2() {
		args[0] = "C:/Users/hao/workspace/DroidBenchProj/ServiceLifecycle2/app/";
		args[1] = "edu.mit.service_lifecycle.MainActivity";
		args[2] = "onCreate";
		Main.main(args);
		assertEquals(false, Results.results.isEmpty());
		Map<String, String> res = new HashMap<>();
		res.put("<android.telephony.SmsManager: void sendTextMessage(java.lang.String,java.lang.String,java.lang.String,android.app.PendingIntent,android.app.PendingIntent)>",
				tm.getDeviceId());
		assertEquals(true, Results.results.contains(res));
	}
	
	@Test
	public void testLifecycle_BroadcastReceiverLifecycle2() {
		args[0] = "C:/Users/hao/workspace/DroidBenchProj/BroadcastReceiverLifecycle2/app/";
		args[1] = "de.ecspride.MainActivity$MyReceiver";
		args[2] = "onReceive";
		Main.main(args);
		assertEquals(false, Results.results.isEmpty());
		Map<String, String> res = new HashMap<>();
		res.put("<android.telephony.SmsManager: void sendTextMessage(java.lang.String,java.lang.String,java.lang.String,android.app.PendingIntent,android.app.PendingIntent)>",
				tm.getDeviceId());
		assertEquals(true, Results.results.contains(res));
	}
	
	@Test
	public void testLifecycle_FragmentLifecycle1() {
		args[0] = "C:/Users/hao/workspace/DroidBenchProj/FragmentLifecycle1/app/";
		args[1] = "de.ecspride.MainActivity";
		args[2] = "onCreate";
		Main.main(args);
		assertEquals(false, Results.results.isEmpty());
		Map<String, String> res = new HashMap<>();
		res.put("<android.telephony.SmsManager: void sendTextMessage(java.lang.String,java.lang.String,java.lang.String,android.app.PendingIntent,android.app.PendingIntent)>",
				tm.getDeviceId());
		assertEquals(true, Results.results.contains(res));
	}
	
	@Test
	public void testLifecycle_FragmentLifecycle2() {
		args[0] = "C:/Users/hao/workspace/DroidBenchProj/FragmentLifecycle2/app/";
		
		args[1] = "sinkEventChains";
		Main.main(args);
		assertEquals(false, Results.results.isEmpty());
		Map<String, String> res = new HashMap<>();
		res.put("<android.telephony.SmsManager: void sendTextMessage(java.lang.String,java.lang.String,java.lang.String,android.app.PendingIntent,android.app.PendingIntent)>",
				tm.getDeviceId());
		assertEquals(true, Results.results.contains(res));
	}
	
	@Test
	public void testLifecycle_EventOrdering1() {
		args[0] = "C:/Users/hao/workspace/DroidBenchProj/EventOrdering1/app/";
		
		//args[1] = "srcEventChains";
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
