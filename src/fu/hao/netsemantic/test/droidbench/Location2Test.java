package fu.hao.netsemantic.test.droidbench;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import fu.hao.trust.data.Results;
import fu.hao.trust.dvm.Main;
import fu.hao.trust.utils.Settings;

public class Location2Test {

	@Test
	public void testCallbacks_Location2() {
		Settings.logLevel = 0;
		String[] args = new String[4];
		args[3] = "ATaint";
		
		args[0] = "C:/Users/hao/workspace/DroidBenchProj/LocationLeak2/app/";
		args[1] = "srcEventChains";
		Main.main(args);
		
		assertEquals(false, Results.results.isEmpty());
		Map<String, String> res = new HashMap<>();
		res.put("<android.telephony.SmsManager: void sendTextMessage(java.lang.String,java.lang.String,java.lang.String,android.app.PendingIntent,android.app.PendingIntent)>",
				"Longtitude: -121.759603");
		assertEquals(true, Results.results.contains(res));
	}

}
