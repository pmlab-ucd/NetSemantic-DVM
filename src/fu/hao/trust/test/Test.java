package fu.hao.trust.test;

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import android.content.Intent;
import fu.hao.trust.data.Results;
import fu.hao.trust.data.TargetCall;
import fu.hao.trust.dvm.Main;
import fu.hao.trust.utils.Log;
import fu.hao.trust.utils.Settings;

public class Test {

	public static void main(String[] margs) {
		Test t = new Test();
		// t.testAnve();
		// t.test00983aad12700be0a440296c6173b18a829e9369_a();
		t.testMain();
		// t.testMopub_onCreate();
		// t.testMopub_loadAd();
		// t.test7613973();
		// t.testOnReceive_7619303();
		// t.testWo_();
	}
	
	public void testMain() {
		String[] args = new String[4];
		args[3] = "ATaint";
		Settings.logLevel = 0;
		
		args[0] = "C:/Users/hao/workspace/DroidBenchProj/Button2/app/";
		args[1] = "srcEventChains";
		Main.main(args);
		
		args[1] = "sinkEventChains";
		Main.main(args);
		assertEquals(false, Results.results.isEmpty());
		Map<String, String> res = new HashMap<>();
		res.put("<android.telephony.SmsManager: void sendTextMessage(java.lang.String,java.lang.String,java.lang.String,android.app.PendingIntent,android.app.PendingIntent)>",
				"359874043116909");
		assertEquals(true, Results.results.contains(res));
	}
	
	public void testAnve() {
		String[] args = new String[4];
		args[3] = "Full";
		Settings.logLevel = 0;

		args[0] = "C:/Users/hao/workspace/DroidBenchProj/apks/bad/malware/AnserverBot/002f537027830303e2205dd0a6106cb1b79fa704/002f537027830303e2205dd0a6106cb1b79fa704.apk";

		args[1] = "com.sec.android.providers.drm.Onion";
		args[2] = "d";
		Main.main(args);
	}
	
	public void test91() {
		String[] args = new String[4];
		args[3] = "Full";
		Settings.logLevel = 0;

		args[0] = "C:/Users/hao/workspace/DroidBenchProj/apks/bad/ad/appx.91/f13f8cd47549fd94252ac1422fd305a2.apk";

		args[1] = "com.mao.view.MainActivity";
		args[2] = "onCreate";
		Settings.addCallBlkListElem("com.baidu.appx.g.i"); // log 
		Settings.addCallBlkListElem("com.baidu.appx.a.b"); // json
		Settings.addCallBlkListElem("com.baidu.appx.ui.b"); // json

		Main.main(args);
		System.out.println("REs: " + Results.results);
		System.out.println("REs: " + Results.targetCallRes);
		assertEquals(false, Results.targetCallRes.isEmpty());
		assertEquals(true, Results.targetCallRes.values().iterator().next()
				.getInfluAPIs().toString().contains("show"));
		assertEquals(false, Results.targetCallRes.values().iterator().next()
				.getFedViews().isEmpty());
	}

	public void test7613973() {
		String[] args = new String[4];
		args[3] = "Full";
		Settings.logLevel = 0;

		args[0] = "C:/Users/hao/workspace/DroidBenchProj/apks/benign/wowotuan/com.wowotuan.appfactory.dekeshishufudian/7613973.html.apk";

		args[1] = "com.wowotuan.appfactory.gui.activity.CityChoiceActivity";
		args[2] = "onCreate";

		Settings.addCallBlkListElem("com.d.a.j/<init>"); // will lead to at
															// least 369 calls..
		Settings.addCallBlkListElem("com.d.a.b.a.bf/<init>");
		Settings.addCallBlkListElem("com.d.a.d.a");
		Settings.addCallBlkListElem("com.d.a");
		Main.main(args);
		System.out.println("REs: " + Results.results);
		System.out.println("REs: " + Results.targetCallRes);
		assertEquals(false, Results.targetCallRes.isEmpty());
		assertEquals(true, Results.targetCallRes.values().iterator().next()
				.getInfluAPIs().toString().contains("show"));
		assertEquals(false, Results.targetCallRes.values().iterator().next()
				.getFedViews().isEmpty());
	}

	public void testWo_() {
		String[] args = new String[4];
		args[3] = "Full";
		Settings.logLevel = 0;

		args[0] = "C:/Users/hao/workspace/DroidBenchProj/apks/benign/wowotuan/com.wowotuan.appfactory.beiermeiyingtongshishangguan/7625393.html.apk";
		// args[1] = "com.wowotuan.appfactory.gui.activity.bz";
		// args[2] = "onItemClick";

		args[1] = "com.wowotuan.appfactory.gui.activity.ca";
		// args[1] = "com.wowotuan.appfactory.e.g";
		args[2] = "onReceive";
		Object[] initArgs = new Object[2];
		Intent intent = new Intent("com.wowotuan.appfactory.broadcast.location");
		intent.putExtra("location", "121.1, 131.1");
		initArgs[1] = intent;
		initArgs[0] = "NULL";
		Settings.addCallBlkListElem("com.d.a");
		Settings.addCallBlkListElem("com.wowotuan.appfactory.gui.activity.CityChoiceActivity/d");
		Settings.addCallBlkListElem("com.wowotuan.appfactory.gui.activity.CityChoiceActivity/b");
		// Settings.addCallBlkListElem("com.wowotuan.appfactory.dto.RequestCityLocationDto");
		Settings.execOnCreate = true;
		Main.main(args, initArgs);
		assertEquals(true, Results.results.isEmpty());
	}

	public void testOnReceive_7619303() {
		String[] args = new String[4];
		args[3] = "Full";
		Settings.logLevel = 0;

		args[0] = "C:/Users/hao/workspace/DroidBenchProj/apks/benign/wowotuan/com.wowotuan.appfactory.wangpintaisuniupai/7619303.html.apk";

		args[1] = "com.wowotuan.appfactory.gui.activity.ca";
		args[2] = "onReceive";
		Object[] initArgs = new Object[2];
		Intent intent = new Intent("com.wowotuan.appfactory.broadcast.location");
		intent.putExtra("location", "121.1, 131.1");
		initArgs[1] = intent;
		initArgs[0] = "NULL";
		Settings.addCallBlkListElem("com.d.a");
		Settings.addCallBlkListElem("com.wowotuan.appfactory.gui.activity.CityChoiceActivity/d");
		Settings.addCallBlkListElem("com.wowotuan.appfactory.gui.activity.CityChoiceActivity/b");
		Settings.execOnCreate = true;
		Main.main(args, initArgs);
		assertEquals(true, Results.results.isEmpty());
	}

	public void testWo_1() {
		String[] args = new String[4];
		args[3] = "Full";
		Settings.logLevel = 0;

		args[0] = "C:/Users/hao/workspace/DroidBenchProj/apks/benign/wowotuan/com.wowotuan.appfactory.wangpintaisuniupai/7619303.html.apk";
		// args[1] = "com.wowotuan.appfactory.gui.activity.bz";
		// args[2] = "onItemClick";
		// args[1] = "com.wowotuan.appfactory.gui.activity.CityChoiceActivity";
		// args[2] = "k";
		args[1] = "com.wowotuan.appfactory.e.h";
		// args[1] = "com.wowotuan.appfactory.e.g";
		args[2] = "a";

		Main.main(args);
		assertEquals(true, Results.results.isEmpty());
	}

	public void testMopub_loadAd() {
		String[] args = new String[4];
		args[3] = "Full";
		Settings.logLevel = 0;

		args[0] = "C:/Users/hao/workspace/MultiThreading/app/app-release.apk";
		args[1] = "com.mopub.mobileads.WebViewAdUrlGenerator";
		args[2] = "generateUrlString";
		Main.main(args);
		assertEquals(true, Results.results.isEmpty());
	}

	public void testMopub_onCreate() {
		String[] args = new String[4];
		args[3] = "Full";
		Settings.logLevel = 0;

		args[0] = "C:/Users/hao/workspace/MultiThreading/app/app-release.apk";
		args[1] = "fu.hao.multithreading.MainActivity";
		args[2] = "onCreate";

		Main.main(args);
		assertEquals(true, Results.results.isEmpty());
	}

	public void test00983aad12700be0a440296c6173b18a829e9369_a() {
		String[] args = new String[4];
		Settings.logLevel = 0;

		args[0] = "C:/Users/hao/workspace/DroidBenchProj/apks/pjapps/00983aad12700be0a440296c6173b18a829e9369/00983aad12700be0a440296c6173b18a829e9369.apk";
		args[1] = "com.android.main.MainService";
		args[2] = "a";
		args[3] = "Full";

		Main.main(args);
		boolean containsSms = false;
		for (TargetCall targetCall : Results.targetCallRes.values()) {
			Log.msg(TAG, "Result: " + targetCall);
			if (targetCall.getInfluAPIs().toString()
					.contains("SmsManager/sendTextMessage")) {
				containsSms = true;
			}
		}

		assertEquals(false, Results.targetCallRes.isEmpty());
		assertEquals(true, containsSms);
	}

	public void testMain2() {
		String[] args = new String[4];
		Settings.logLevel = 0;
		args[3] = "Full";

		args[0] = "C:/Users/hao/workspace/PJApps/app/app-release.apk";
		args[1] = "fu.hao.pjapps.MainActivity";
		args[2] = "testConnection";
		Main.main(args);

		System.out.println("REs: " + Results.results);
		System.out.println("REs: " + Results.targetCallRes);
		assertEquals(false, Results.targetCallRes.isEmpty());
		assertEquals(
				true,
				Results.targetCallRes
						.values()
						.iterator()
						.next()
						.getInfluAPIs()
						.toString()
						.contains(
								"<40 INVOKE,VIRTUAL,extra=[android.telephony.SmsManager/sendTextMessage"));
	}

	class dd {
		int a;
	}

	static String TAG = "test";

	dd i;

	void testFie() {
		b(i);
		i = new dd();
		i.a = 4;
		System.out.println(i.a);
	}

	void b(dd c) {
		c = new dd();
		c.a = 3;
	}

	public void test() {
		// The test case get from.
		StringBuilder sb = new StringBuilder("hah");
		System.out.println(sb.toString());
		int rand = 8;// (int) (Math.random());
		System.out.println("random number  : " + rand);
		int x = rand * 2 + 1;
		int y = 6345;
		int c = 0;
		int d = 23456;
		int f = 0;
		System.out.println("HelloWorld");
		System.out.println("--------------------");
		System.out.println("initial value ");
		System.out.println("random number x : " + x);
		System.out.println("x = " + x);
		System.out.println("y = " + y);
		System.out.println("c = " + c);
		System.out.println("d = " + d);
		System.out.println("f = " + f);
		System.out.println("--------------------");

		c = x + y;
		d += c;
		System.out.println("c = x + y = " + x + " + " + y + " = " + c);
		System.out.println("d = d + c = " + d);
		x = c / 2;
		System.out.println("x = c/2 = " + x);

		c = x * y;
		d += c;
		System.out.println("c = x * y = " + x + " * " + y + " = " + c);
		System.out.println("d = d + c = " + d);
		x = c / 2;
		System.out.println("x = c/2 = " + x);

		c = x - y;
		d += c;
		System.out.println("c = x - y = " + x + " - " + y + " = " + c);
		System.out.println("d = d + c = " + d);
		x = c / 2;
		System.out.println("x = c/2 = " + x);

		c = x / y;
		d += c;
		System.out.println("c = x / y = " + x + " / " + y + " = " + c);
		System.out.println("d = d + c = " + d);

		f = d + x + y + c;
		System.out.println("f = " + (d) + " + " + (x) + " + " + (y) + " + "
				+ (c) + " = " + f);
		System.out.println("Veri Foo Test By WJY");
	}

}
