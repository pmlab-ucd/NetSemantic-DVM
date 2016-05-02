package fu.hao.trust.test;

import java.io.IOException;
import java.util.zip.ZipException;

import org.junit.Test;

import fu.hao.trust.dvm.DalvikVM;
import fu.hao.trust.utils.Settings;

public class InterpreterTest {

	String tag = "TEST DVM";

	// @Test
	public void testSwitch() {
		try {
			prepare("testSwitch");
		} catch (ZipException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// @Test
	public void testCMP() {
		try {
			prepare("testCMP");
		} catch (ZipException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// @Test
	public void testIF() {
		try {
			prepare("testIF");
		} catch (ZipException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// @Test
	public void testArray() {
		try {
			prepare("testArray");
		} catch (ZipException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testInvoke() {
		try {
			prepare("testInvoke");
		} catch (ZipException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// @Test
	public void testArith() {
		try {
			prepare("testArith");
		} catch (ZipException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void prepare(String method) throws ZipException, IOException {
		DalvikVM vm = new DalvikVM("C:/Users/hao/workspace/TestDVM/app/app-release.apk");
		Settings.logLevel = 9;
		vm.runMethod("C:/Users/hao/workspace/TestDVM/app/app-release.apk",
				"fu.hao.testdvm.MainActivity", method, null, null);
	}

}
