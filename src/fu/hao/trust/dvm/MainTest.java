package fu.hao.trust.dvm;

import static org.junit.Assert.*;

import org.junit.Test;

import fu.hao.trust.utils.Settings;

public class MainTest {
	String[] args = new String[2];
	public MainTest() {
		Settings.logLevel = 9;
	}

	@Test
	public void testGeneralJava_SourceCodeSpecific1() {	
		args[0] = "C:/Users/hao/workspace/DroidBenchProj/GeneralJava_SourceCodeSpecific1/app/app-debug.apk";
		args[1] = "de.ecspride.MainActivity";
		Main.main(args);
	}
	
	//@Test
	public void testGeneralJava_Loop1() {
		args[0] = "C:/Users/hao/workspace/GeneralJava_Loop1/app/app-debug.apk";
		args[1] = "de.ecspride.LoopExample1";
		Main.main(args);
	}
	
	//@Test
	public void testGeneralJava_Loop2() {
		args[0] = "C:/Users/hao/workspace/GeneralJava_Loop2/app/app-debug.apk";
		args[1] = "de.ecspride.LoopExample2";
		Main.main(args);
	}

}
