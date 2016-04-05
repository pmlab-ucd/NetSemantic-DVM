package fu.hao.trust.test;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import fu.hao.trust.data.Results;
import fu.hao.trust.dvm.Main;
import fu.hao.trust.utils.Settings;

public class TestUnknown {

	@Test
	public void testGeneralJava_VirtualDispatch1() {
			String[] args = new String[4];
			Settings.logLevel = 0;
			args[0] = "C:/Users/hao/workspace/DroidBenchProj/GeneralJava_VirtualDispatch1/app/";
			args[1] = "de.ecspride.VirtualDispatch1";
			args[2] = null;
			args[3] = "Taint";
			//args[2] = "clickButton";
			Main.main(args);
			System.out.println("REs: " + Results.results);
			Map<String, String> res = new HashMap<>();
			res.put("<android.telephony.SmsManager: void sendTextMessage(java.lang.String,java.lang.String,java.lang.String,android.app.PendingIntent,android.app.PendingIntent)>",
					"359874043116909");
			assertEquals(Results.results.contains(res), true);
		}

}
