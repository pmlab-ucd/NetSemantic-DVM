package fu.hao.trust.test;

import static org.junit.Assert.*;

import org.junit.Test;

import fu.hao.trust.dvm.Main;
import fu.hao.trust.utils.Settings;

public class ContextAnalysisTest {

	@Test
	public void test() {
		String[] args = new String[4];
		Settings.logLevel = 0;
		
		args[0] = "C:/Users/hao/workspace/PJApps/app/PJApps.apk";
		args[1] = "fu.hao.pjapps.MainActivity";
		args[2] = "testCtx";
		args[3] = "Ctx";
		
		Main.main(args);
	}

}
