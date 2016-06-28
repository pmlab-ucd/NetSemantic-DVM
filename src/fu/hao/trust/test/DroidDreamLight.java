package fu.hao.trust.test;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import fu.hao.trust.data.Results;
import fu.hao.trust.dvm.Main;
import fu.hao.trust.utils.Settings;

public class DroidDreamLight {

	@Test
	public void test() {
		String[] args = new String[9];
		args[3] = "ATaint";
		Settings.logLevel = 0;
		
		args[0] = "D:/malwares/DroidDreamLight/1a613334f30005925d95bbfd845caa065a15682b";
		args[1] = "com.passion.lightdd.CoreService";
		args[2] = "onCreate";

		Main.main(args);

		assertEquals(false, Results.results.isEmpty());
		assertEquals(false, Results.results.isEmpty());
		Map<String, String> res = new HashMap<>();
		res.put("<java.net.URL: void <init>(java.lang.String)>",
				"http://ya3k.com/bksy.jsp"); 
		assertEquals(true, Results.results.contains(res));
	}

}
