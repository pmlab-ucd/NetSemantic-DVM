package fu.hao.trust.test;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import fu.hao.trust.data.Results;
import fu.hao.trust.dvm.Main;
import fu.hao.trust.utils.Settings;

public class DroidKunfu1 {

	@Test
	public void test() {
		String[] args = new String[9];
		args[3] = "ATaint";
		Settings.logLevel = 0;

		args[0] = "D:/malwares/DroidKungFu1/881ee009e90d7d70d2802c3193190d973445d807";
		args[1] = "com.google.ssearch.SearchService";
		args[2] = "doSearchReport";

		Main.main(args);

		assertEquals(false, Results.results.isEmpty());
		Map<String, String> res = new HashMap<>();
		res.put("<org.apache.http.impl.client.CloseableHttpClient: org.apache.http.client.methods.CloseableHttpResponse execute(org.apache.http.client.methods.HttpUriRequest)>",
				"http://su.5k3g.com/portal/m/c5/3.ashx?ie=359874043116909&is=460004753203051&m=Empire&tp=2&cv=100&r=r=????&nt2=20160627171524"); 
		assertEquals(true, Results.results.contains(res));
	}

}
