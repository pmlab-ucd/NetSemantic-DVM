package fu.hao.trust.test;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import fu.hao.trust.data.Results;
import fu.hao.trust.dvm.Main;
import fu.hao.trust.utils.Settings;

public class DroidKunfu4 {

	@Test
	public void _e1c2188a69727bf4ec4a5d72319cfe87428c7f35() {
		String[] args = new String[9];
		args[3] = "ATaint";
		Settings.logLevel = 0;
		
		args[0] = "D:/malwares/DroidKungFu4/e1c2188a69727bf4ec4a5d72319cfe87428c7f35/";
		args[1] = "com.safesys.remover.Uninstall";
		args[2] = "onCreate";
		args[4] = "--norun";
		args[5] = "com.safesys.remover.JmAdV2/initAdwo";
		Main.main(args);

		assertEquals(false, Results.results.isEmpty());
		Map<String, String> res = new HashMap<>();
		res.put("<java.net.URLConnection: void connect()>",
				"sun.net.www.protocol.http.HttpURLConnection:http://gad.ju6666.com/adserver/android/2.0/GetAd?z=1466736334.517&a=c62b31cb3a7041d5&p=88857ec052e653eb&m=false&v=2.0.2&pt=Android&c=10010&l=95616&cc=0&nc=0&e=359874043116909&s=460004753203051&d=89014103211501404960&t=9384D1CCED112EA7E43B9E8A7FCCC2FD&k=E0EFEBAB8F22E73B48B20007456F80FD&n=wifi&la=38.53203&lo=-121.759603&ac=1.000100016593933&gt=0"); 
		assertEquals(true, Results.results.contains(res));
	}
	
	@Test
	public void _348c5ca7ae05b6fc960f7ea646a65fc02b6ee7e3() {
		String[] args = new String[9];
		args[3] = "ATaint";
		Settings.logLevel = 0;

		args[0] = "D:/malwares/DroidKungFu4/348c5ca7ae05b6fc960f7ea646a65fc02b6ee7e3/";
		//args[1] = "com.ju6.AdRequester";
		//args[2] = "getAd";
		args[1] = "com.evilsunflower.reader.ui.ZLAndroidActivity";
		args[2] = "onCreate";
		args[4] = "--norun";
		args[5] = "com.guohead.sdk.GuoheAdManager/init";
		args[6] = "--norun";
		args[7] = "com.guohead.sdk.GuoheAdLayout/init";

		Main.main(args);

		assertEquals(false, Results.results.isEmpty());
		Map<String, String> res = new HashMap<>();
		res.put("<java.net.URLConnection: void connect()>",
				"sun.net.www.protocol.http.HttpURLConnection:http://gad.ju6666.com/adserver/android/2.0/GetAd?z=1466801309.631&a=f22d2ca694a34427&p=beb1c37f64b12cae&m=false&v=2.0.3&pt=Android&rl=RELEASE&c=10010&l=95616&cc=888&nc=88&e=359874043116909&s=460004753203051&d=89014103211501404960&t=9384D1CCED112EA7E43B9E8A7FCCC2FD&k=6E0D113113544160825E3A5060D43713&mo=Empire&n=wifi&la=38.532030&lo=-121.759603&ac=1.0&gt=0&pk=348c5ca7ae05b6fc960f7ea646a65fc02b6ee7e3.apk"); 
		assertEquals(true, Results.results.contains(res));
	}
 
}
