package fu.hao.trust.test;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import fu.hao.trust.data.Results;
import fu.hao.trust.dvm.Main;
import fu.hao.trust.utils.Settings;

public class DroidKunfu3 {
	
	public void myDroidKunfu3() {
		String[] args = new String[9];
		args[3] = "ATaint";
		Settings.logLevel = 0;
		
		args[0] = "C:/Users/hao/workspace/DroidKungFu3/app/";
		args[1] = "com.google.update.UpdateService";
		args[2] = "onCreate";
		//args[8] = "--runEntryException";
		Main.main(args);
		
		assertEquals(false, Results.results.isEmpty());
		Map<String, String> res = new HashMap<>();
		res.put("<android.telephony.SmsManager: void sendTextMessage(java.lang.String,java.lang.String,java.lang.String,android.app.PendingIntent,android.app.PendingIntent)>",
				"POST http://search.zs169.com:8511/search/newhi.php HTTP/1.1"); // devID
		assertEquals(true, Results.results.contains(res));
	}

	@Test
	public void _3d79d12d1abcddf9b53ca04469488a84a91aabdc() {
		String[] args = new String[9];
		args[3] = "ATaint";
		Settings.logLevel = 0;
		
		args[0] = "D:/malwares/DroidKungFu3/3d79d12d1abcddf9b53ca04469488a84a91aabdc";
		args[1] = "com.google.update.UpdateService";
		args[2] = "onCreate";
		args[4] = "--norun";
		args[5] = "com.google.update.Utils$TCP/startListen";
		args[8] = "--runEntryException";
		Main.main(args);
		
		assertEquals(false, Results.results.isEmpty());
		Map<String, String> res = new HashMap<>();
		res.put("<org.apache.http.impl.client.CloseableHttpClient: org.apache.http.client.methods.CloseableHttpResponse execute(org.apache.http.client.methods.HttpUriRequest)>",
				"POST http://search.zs169.com:8511/search/newhi.php HTTP/1.1"); // devID
		assertEquals(true, Results.results.contains(res));
	}
	
	@Test 
	public void _255a1b74428b5615d65f39775ec7234e27bd9e74() {
		String[] args = new String[9];
		args[3] = "ATaint";
		Settings.logLevel = 0;
		
		args[0] = "D:/malwares/DroidKungFu3/255a1b74428b5615d65f39775ec7234e27bd9e74";
		args[1] = "com.google.update.UpdateService";
		args[2] = "onCreate";
		args[4] = "--norun";
		args[5] = "com.google.update.RU$U12/U1";
		args[8] = "--runEntryException";
		Main.main(args);
		
		assertEquals(false, Results.results.isEmpty());
		Map<String, String> res = new HashMap<>();
		res.put("<org.apache.http.impl.client.CloseableHttpClient: org.apache.http.client.methods.CloseableHttpResponse execute(org.apache.http.client.methods.HttpUriRequest)>",
				"POST http://search.zs169.com:8511/search/newhi.php HTTP/1.1"); // devID
		assertEquals(true, Results.results.contains(res));
	}
	
	@Test
	public void _e585b2e268af2f68f969da956637344ea54c40de() {
		String[] args = new String[9];
		args[3] = "ATaint";
		Settings.logLevel = 0;
		
		args[0] = "D:/malwares/DroidKungFu3/e585b2e268af2f68f969da956637344ea54c40de";
		args[1] = "com.google.update.UpdateService";
		args[2] = "onCreate";
		args[4] = "--norun";
		args[5] = "com.google.update.RU$U12/U1";
		args[8] = "--runEntryException";
		Main.main(args);
		
		assertEquals(false, Results.results.isEmpty());
		Map<String, String> res = new HashMap<>();
		res.put("<org.apache.http.impl.client.CloseableHttpClient: org.apache.http.client.methods.CloseableHttpResponse execute(org.apache.http.client.methods.HttpUriRequest)>",
				"POST http://search.best188.net:8511/search/newhi.php HTTP/1.1"); // devID
		assertEquals(true, Results.results.contains(res));
	}
	
	@Test
	public void _8c841b25102569be4a1a5f407108482473fad43e() {
		String[] args = new String[9];
		args[3] = "ATaint";
		Settings.logLevel = 0;
		
		args[0] = "D:/malwares/DroidKungFu3/8c841b25102569be4a1a5f407108482473fad43e";
		args[1] = "com.google.update.UpdateService";
		args[2] = "onCreate";
		args[4] = "--norun";
		args[5] = "com.google.update.RU$U12/U1";
		args[8] = "--runEntryException";
		Main.main(args);
		
		assertEquals(false, Results.results.isEmpty());
		Map<String, String> res = new HashMap<>();
		res.put("<org.apache.http.impl.client.CloseableHttpClient: org.apache.http.client.methods.CloseableHttpResponse execute(org.apache.http.client.methods.HttpUriRequest)>",
				"POST http://search.best188.net:8511/search/newhi.php HTTP/1.1"); // devID
		assertEquals(true, Results.results.contains(res));
	}
	
	@Test 
	public void _d19be71348f28529b64d366e04c076b43314b26e() {
		String[] args = new String[9];
		args[3] = "ATaint";
		Settings.logLevel = 0;
		
		args[0] = "D:/malwares/DroidKungFu3/d19be71348f28529b64d366e04c076b43314b26e";
		args[1] = "com.google.update.UpdateService";
		args[2] = "onCreate";
		args[4] = "--norun";
		args[5] = "com.google.update.RU$U12/U1";
		args[8] = "--runEntryException";
		Main.main(args);
		
		assertEquals(false, Results.results.isEmpty());
		Map<String, String> res = new HashMap<>();
		res.put("<org.apache.http.impl.client.CloseableHttpClient: org.apache.http.client.methods.CloseableHttpResponse execute(org.apache.http.client.methods.HttpUriRequest)>",
				"POST http://search.best188.net:8511/search/newhi.php HTTP/1.1"); // devID
		assertEquals(true, Results.results.contains(res));
	}
	
	@Test 
	public void _e7185137929a0cca9dab752564ab47a3c99bb371() {
		String[] args = new String[9];
		args[3] = "ATaint";
		Settings.logLevel = 0;
		
		args[0] = "D:/malwares/DroidKungFu3/e7185137929a0cca9dab752564ab47a3c99bb371";
		args[1] = "com.google.update.UpdateService";
		args[2] = "onCreate";
		args[4] = "--norun";
		args[5] = "com.google.update.RU$U12/U1";
		args[8] = "--runEntryException";
		Main.main(args);
		
		assertEquals(false, Results.results.isEmpty());
		Map<String, String> res = new HashMap<>();
		res.put("<org.apache.http.impl.client.CloseableHttpClient: org.apache.http.client.methods.CloseableHttpResponse execute(org.apache.http.client.methods.HttpUriRequest)>",
				"POST http://search.best188.net:8511/search/newhi.php HTTP/1.1"); // devID
		assertEquals(true, Results.results.contains(res));
	}
}
