import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import fu.hao.trust.analysis.Results;
import fu.hao.trust.dvm.Main;
import fu.hao.trust.utils.Settings;

public class Test {
	
	public void testMain() {
		String[] args = new String[3];
		Settings.logLevel = 0;
		
		args[0] = "C:/Users/hao/workspace/DroidBenchProj/Lifecycle_ServiceLifecycle1/app/";//ActivityLifecycle2.apk";
		args[1] = "de.ecspride.MainActivity";
		//args[2] = "onCreate";
		Main.main(args);
		System.out.println("REs: " + Results.results.toString());

		assertEquals(false, Results.results.isEmpty());
		Map<String, String> res = new HashMap<>();
		res.put("<java.net.URL: java.net.URLConnection openConnection()>",
				"http://www.google.de/search?q=359874043116909");
		System.out.print("REs: " + res.toString());
		assertEquals(true, Results.results.contains(res));
	}
	
	class dd {
		int a;
	}
	
	static String TAG = "test";  
	
	dd i;
	
	public static void main(String[] margs) {				
		Test t = new Test();
		t.testMain();
		//t.testFie();
	}
	
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
