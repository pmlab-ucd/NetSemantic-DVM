package fu.hao.trust.test;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import fu.hao.trust.data.Results;
import fu.hao.trust.dvm.Main;
import fu.hao.trust.utils.Settings;

public class Geinimi {

	@Test
	public void test() {
		String[] args = new String[9];
		args[3] = "ATaint";
		Settings.logLevel = 0;

		args[0] = "D:/malwares/Geinimi/1353bd14e91a53fa1ee54cd51c1db6918eb9f851/";
		args[1] = "com.geinimi.AdService";
		args[2] = "onCreate";

		Main.main(args);

		assertEquals(false, Results.results.isEmpty());
		Map<String, String> res = new HashMap<>();
		res.put("<java.net.URLConnection: void connect()>",
				"Connect url:http://www.winpowersoft.com:8080/adserver/getAdXml.do, params:PTID=000004&IMEI=359874043116909&IMSI=460004753203051&CPID=0000"); 
		assertEquals(true, Results.results.contains(res));
	}

}
