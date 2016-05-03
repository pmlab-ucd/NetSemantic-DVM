package fu.hao.trust.test;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import fu.hao.trust.data.Results;
import fu.hao.trust.dvm.Main;
import fu.hao.trust.utils.Settings;

public class MainTest2 {
	String[] args = new String[4];
	Object[] initArgs = new Object[2];

	public MainTest2() {
		Settings.logLevel = 0;
		args[3] = "Taint";
		
		initArgs[0] = "android.app.Activity";
		initArgs[1] = "NULL";
		Main.initMI(initArgs);
	}

	@Test
	public void testCallbacks_Button1() {
		args[0] = "C:/Users/hao/workspace/DroidBenchProj/Callbacks_Button1/app/";
		Main.main(args);
		assertEquals(false, Results.results.isEmpty());
		Map<String, String> res = new HashMap<>();
		res.put("<android.telephony.SmsManager: void sendTextMessage(java.lang.String,java.lang.String,java.lang.String,android.app.PendingIntent,android.app.PendingIntent)>",
				"359874043116909");
		assertEquals(true, Results.results.contains(res));
	}
	
	@Test
	public void atestCallbacks_Button2() {
		args[0] = "C:/Users/hao/workspace/DroidBenchProj/Callbacks_Button2/app/";
		Main.main(args);
		assertEquals(false, Results.results.isEmpty());
		Map<String, String> res = new HashMap<>();
		res.put("<android.telephony.SmsManager: void sendTextMessage(java.lang.String,java.lang.String,java.lang.String,android.app.PendingIntent,android.app.PendingIntent)>",
				"359874043116909");
		assertEquals(true, Results.results.contains(res));
	}
}
