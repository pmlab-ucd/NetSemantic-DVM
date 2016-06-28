package fu.hao.trust.test;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import fu.hao.trust.data.Results;
import fu.hao.trust.dvm.Main;
import fu.hao.trust.utils.Settings;

public class KMin_2 {

	@Test
	public void test() {
		String[] args = new String[9];
		args[3] = "ATaint";
		Settings.logLevel = 0;

		args[0] = "D:/malwares/KMin/3726cfddc691cb72e7ed6b059bf943ad9b4d1774";
		args[1] = "com.km.ad.AdService";
		args[2] = "onStart";

		Main.main(args);

		assertEquals(false, Results.results.isEmpty());
		Map<String, String> res = new HashMap<>();
		res.put("<java.net.URL: void <init>(java.lang.String)>",
				"http://su.5k3g.com/portal/m/c5/3.ashx?ie=359874043116909&is=460004753203051&m=Empire&tp=2&cv=100&r=r=????&nt2=20160627171524"); 
		assertEquals(true, Results.results.contains(res));
	}

}
