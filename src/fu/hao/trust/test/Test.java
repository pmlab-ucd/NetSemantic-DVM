package fu.hao.trust.test;

import static org.junit.Assert.assertEquals;
import fu.hao.trust.data.Results;
import fu.hao.trust.data.TargetCall;
import fu.hao.trust.dvm.Main;
import fu.hao.trust.utils.Log;
import fu.hao.trust.utils.Settings;

public class Test {

	public static void main(String[] margs) {
		Test t = new Test();
		t.test00983aad12700be0a440296c6173b18a829e9369_a();
		//t.testMain2();
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
			if (targetCall.getInfluAPIs().toString().contains("SmsManager/sendTextMessage")) {
				containsSms = true;
			}
		}

		assertEquals(false, Results.targetCallRes.isEmpty());
		assertEquals(true, containsSms);
	}
	
	public void testMain() {
		String[] args = new String[4];
		args[3] = "Taint";
		Settings.logLevel = 0;
		
		args[0] = "C:/Users/hao/workspace/DroidBenchProj/FieldAndObjectSensitivity_FieldSensitivity4/app/app-release.apk";
		args[1] = "de.ecspride.FieldSensitivity4";
		args[2] = "onCreate";
		Main.main(args);
		assertEquals(true, Results.results.isEmpty());
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

	void b (dd c) {
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
