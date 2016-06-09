package fu.hao.netsemantic.test.droidbench;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import android.telephony.TelephonyManager;
import fu.hao.trust.data.Results;
import fu.hao.trust.dvm.Main;
import fu.hao.trust.utils.Settings;

public class BenchArraysAndLists {
	
	String[] args = new String[4];
	
	TelephonyManager tm;

	public BenchArraysAndLists() {
		Settings.logLevel = 0;
		args[3] = "ATaint";
		tm = new TelephonyManager();
	}

	@Test
	public void ArrayAccess1() {
		args[0] = "C:/Users/hao/workspace/DroidBenchProj/ArrayAccess1/app/";
		args[1] = "srcEventChains";
		Main.main(args);
		assertEquals(true, Results.results.isEmpty());
	}
	
	@Test
	public void ArrayAccess2() {
		args[0] = "C:/Users/hao/workspace/DroidBenchProj/ArrayAccess2/app/";
		args[1] = "srcEventChains";
		Main.main(args);
		assertEquals(true, Results.results.isEmpty());
	}
	
	@Test 
	public void HashMapAccess1() {
		args[0] = "C:/Users/hao/workspace/DroidBenchProj/HashMapAccess1/app/";
		args[1] = "srcEventChains";
		Main.main(args);
		assertEquals(true, Results.results.isEmpty());
	}
	
	@Test
	public void ListAccess1() {
		args[0] = "C:/Users/hao/workspace/DroidBenchProj/ListAccess1/app/";
		args[1] = "srcEventChains";
		Main.main(args);
		assertEquals(true, Results.results.isEmpty());
	}
	
	@Test
	public void ArrayCopy() {
		args[0] = "C:/Users/hao/workspace/DroidBenchProj/ArrayCopy1/app/";
		args[1] = "srcEventChains";
		Main.main(args);
		Map<String, String> res = new HashMap<>();
		res.put("<android.telephony.SmsManager: void sendTextMessage(java.lang.String,java.lang.String,java.lang.String,android.app.PendingIntent,android.app.PendingIntent)>",
				tm.getDeviceId());
		assertEquals(true, Results.results.contains(res));
	}
}
