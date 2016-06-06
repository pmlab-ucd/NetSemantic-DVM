package fu.hao.netsemantic.test.droidbench;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import fu.hao.trust.data.Results;
import fu.hao.trust.dvm.Main;
import fu.hao.trust.utils.Settings;

public class Button5Test {
	String[] args = new String[4];

	public Button5Test() {
		Settings.logLevel = 0;
		args[3] = "ATaint";
	}

	@Test
	public void test() {
		args[0] = "C:/Users/hao/workspace/DroidBenchProj/Button5/app/";
		args[1] = "srcEventChains";
		Main.main(args);
		
		assertEquals(false, Results.results.isEmpty());
		Map<String, String> res = new HashMap<>();
		res.put("<android.telephony.SmsManager: void sendTextMessage(java.lang.String,java.lang.String,java.lang.String,android.app.PendingIntent,android.app.PendingIntent)>",
				"[Unknown var:359874043116909, type: java.lang.String]");
		assertEquals(true, Results.results.contains(res));
	}

}
