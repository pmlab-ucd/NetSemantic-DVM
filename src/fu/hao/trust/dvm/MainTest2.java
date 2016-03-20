package fu.hao.trust.dvm;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import fu.hao.trust.analysis.Results;
import fu.hao.trust.utils.Settings;

public class MainTest2 {
	String[] args = new String[4];

	public MainTest2() {
		Settings.logLevel = 0;
		args[3] = "Taint";
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
