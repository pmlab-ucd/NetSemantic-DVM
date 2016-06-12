package fu.hao.netsemantic.test.droidbench;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import android.telephony.TelephonyManager;
import fu.hao.trust.data.Results;
import fu.hao.trust.dvm.Main;
import fu.hao.trust.utils.Settings;

public class BenchAndroidSpecific {

	String[] args = new String[4];
	
	TelephonyManager tm;

	public BenchAndroidSpecific() {
		Settings.logLevel = 0;
		args[3] = "ATaint";
		tm = new TelephonyManager();
	}
	
	@Test 
	public void DirectLeak1() {
		args[0] = "C:/Users/hao/workspace/DroidBenchProj/DirectLeak1/app/";
		args[1] = "srcEventChains";
		Main.main(args);
		Map<String, String> res = new HashMap<>();
		res.put("<android.telephony.SmsManager: void sendTextMessage(java.lang.String,java.lang.String,java.lang.String,android.app.PendingIntent,android.app.PendingIntent)>",
				tm.getDeviceId());
		assertEquals(true, Results.results.contains(res));
	}
	
	@Test
	public void InactiveActivity() {
		args[0] = "C:/Users/hao/workspace/DroidBenchProj/InactiveActivity/app/";
		args[1] = "srcEventChains";
		Main.main(args);
		assertEquals(true, Results.results.isEmpty());
	}
	
	@Test
	public void LogNoLeak() {
		args[0] = "C:/Users/hao/workspace/DroidBenchProj/LogNoLeak/app/";
		args[1] = "srcEventChains";
		Main.main(args);
		assertEquals(true, Results.results.isEmpty());
	}
	
	@Test
	public void Obfuscation1() {
		args[0] = "C:/Users/hao/workspace/DroidBenchProj/Obfuscation1/app/";
		args[1] = "srcEventChains";
		Main.main(args);
		Map<String, String> res = new HashMap<>();
		res.put("<android.telephony.SmsManager: void sendTextMessage(java.lang.String,java.lang.String,java.lang.String,android.app.PendingIntent,android.app.PendingIntent)>",
				tm.getDeviceId());
		assertEquals(true, Results.results.contains(res));
	}
	
	@Test
	public void ApplicationModeling1() {
		args[0] = "C:/Users/hao/workspace/DroidBenchProj/ApplicationModeling1/app/";
		args[1] = "srcEventChains";
		Main.main(args);
		assertEquals(false, Results.results.isEmpty());
		Map<String, String> res = new HashMap<>();
		res.put("<android.telephony.SmsManager: void sendTextMessage(java.lang.String,java.lang.String,java.lang.String,android.app.PendingIntent,android.app.PendingIntent)>",
				tm.getDeviceId());
		assertEquals(true, Results.results.contains(res));
	}
	
	@Test
	public void Parcel1() {
		args[0] = "C:/Users/hao/workspace/DroidBenchProj/Parcel1/app/";
		args[1] = "srcEventChains";
		Main.main(args);
		assertEquals(false, Results.results.isEmpty());
		Map<String, String> res = new HashMap<>();
		res.put("<android.telephony.SmsManager: void sendTextMessage(java.lang.String,java.lang.String,java.lang.String,android.app.PendingIntent,android.app.PendingIntent)>",
				tm.getDeviceId());
		assertEquals(true, Results.results.contains(res));
	}
	
	@Test
	public void PublicAPIField1() {
		args[0] = "C:/Users/hao/workspace/DroidBenchProj/PublicAPIField1/app/";
		args[1] = "srcEventChains";
		Main.main(args);
		assertEquals(false, Results.results.isEmpty());
		Map<String, String> res = new HashMap<>();
		res.put("<android.telephony.SmsManager: void sendTextMessage(java.lang.String,java.lang.String,java.lang.String,android.app.PendingIntent,android.app.PendingIntent)>",
				"3.5987404E73116909.0");
		assertEquals(true, Results.results.contains(res));
	}
	
	@Test
	public void PublicAPIField2() {
		args[0] = "C:/Users/hao/workspace/DroidBenchProj/PublicAPIField2/app/";
		args[1] = "srcEventChains";
		Main.main(args);
		assertEquals(false, Results.results.isEmpty());
		Map<String, String> res = new HashMap<>();
		res.put("<android.telephony.SmsManager: void sendTextMessage(java.lang.String,java.lang.String,java.lang.String,android.app.PendingIntent,android.app.PendingIntent)>",
				tm.getDeviceId());
		assertEquals(true, Results.results.contains(res));
	}
	
	
}
