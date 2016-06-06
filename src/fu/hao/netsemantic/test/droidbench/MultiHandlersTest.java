package fu.hao.netsemantic.test.droidbench;

import static org.junit.Assert.*;

import org.junit.Test;

import fu.hao.trust.data.Results;
import fu.hao.trust.dvm.Main;
import fu.hao.trust.utils.Settings;

public class MultiHandlersTest {

	@Test
	public void test() {
		String[] args = new String[4];
		Settings.logLevel = 0;
		args[3] = "ATaint";
		
		args[0] = "C:/Users/hao/workspace/DroidBenchProj/MultiHandlers1/app/";
		args[1] = "srcEventChains";
		Main.main(args);
		
		assertEquals(true, Results.results.isEmpty());
	}

}
