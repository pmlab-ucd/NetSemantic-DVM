package fu.hao.trust.test;

import org.junit.Before;
import org.junit.Test;

import fu.hao.trust.dvm.ResolveIntent;
import fu.hao.trust.utils.Settings;

public class ResolveIntentTest {
	String[] args = new String[1];
	
	ResolveIntentTest() {
		
	}
	
	@Before
	public void prepare() {
		Settings.logLevel = 0;
	}
	
	public static void main(String[] argss) {
		Settings.logLevel = 0;
		String[] args = new String[1];
		args[0] = "C:/Users/hao/workspace/DroidBenchProj/ServiceLifecycle2/app/";
		Settings.addCallBlkListElem("android.content.Context/startActivity");
		Settings.addCallBlkListElem("android.content.Context/startService");
		ResolveIntent.main(args);
	}
	
	@Test
	public void testMain() {
		args[0] = "C:/Users/hao/workspace/DroidBenchProj/ActivityCommunication2/app/";
		Settings.addCallBlkListElem("android.content.ContextWrapper/startActivity");
		ResolveIntent.main(args);
	}

}
