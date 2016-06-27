package fu.hao.trust.test;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import fu.hao.trust.data.Results;
import fu.hao.trust.dvm.Main;
import fu.hao.trust.utils.Settings;

public class GoldDream {
	
	@Test
	public void malware() {
		String[] args = new String[9];
		args[3] = "ATaint";
		Settings.logLevel = 0;

		args[0] = "D:/malwares/GoldDream/1034b746a26e236b9a623a5cebfb67dde52a1d8a/";
		args[1] = "com.GoldDream.zj.zjService";
		args[2] = "onStart";

		Main.main(args);

		assertEquals(false, Results.results.isEmpty());
		Map<String, String> res = new HashMap<>();
		res.put("<java.net.URL: java.net.URLConnection openConnection()>",
				"http://lebar.gicp.net/zj/RegistUid.aspx?pid=9958&cid=1000&imei=359874043116909&sim=460004753203051&imsi=89014103211501404960&ua=unknown"); 
		assertEquals(true, Results.results.contains(res));
	}

}
