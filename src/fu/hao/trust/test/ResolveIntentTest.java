package fu.hao.trust.test;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import fu.hao.trust.dvm.ResolveIntent;
import fu.hao.trust.utils.Settings;

public class ResolveIntentTest {
	
	@Before
	public void prepare() {
		Settings.logLevel = 0;
	}
	
	@Test
	public void testMain() {
		String[] args = new String[1];
		args[0] = "C:/Users/hao/workspace/DroidBenchProj/ActivityCommunication2/app/";
		Settings.addCallBlkListElem("android.content.Context/startActivity");
		ResolveIntent.main(args);
	}

}
