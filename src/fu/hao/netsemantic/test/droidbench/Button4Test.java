package fu.hao.netsemantic.test.droidbench;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import android.app.Activity;
import fu.hao.trust.data.Results;
import fu.hao.trust.dvm.Main;
import fu.hao.trust.utils.Settings;

public class Button4Test {
	
	@Before
	public void clean() {
		Activity.setWidgetPool(null);
	}
	
	@Test
	public void testCallbacks_Button4() {
		String[] args = new String[4];
		args[0] = "C:/Users/hao/workspace/DroidBenchProj/Button4/app/";
		args[1] = "srcEventChains";
		Settings.logLevel = 0;
		args[3] = "ATaint";
		
		Main.main(args);
		
		assertEquals(false, Results.results.isEmpty());
		Map<String, String> res = new HashMap<>();
		res.put("<android.telephony.SmsManager: void sendTextMessage(java.lang.String,java.lang.String,java.lang.String,android.app.PendingIntent,android.app.PendingIntent)>",
				"359874043116909");
		assertEquals(true, Results.results.contains(res));
	}
	

}
