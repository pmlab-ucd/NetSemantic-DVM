package fu.hao.trust.test;

import java.io.IOException;
import java.util.zip.ZipException;

import org.junit.Test;

import patdroid.core.PrimitiveInfo;
import fu.hao.trust.data.Results;
import fu.hao.trust.dvm.DalvikVM;
import fu.hao.trust.utils.Settings;

public class PjappsTest {

	@Test
	public void test() {
		String[] args = new String[4];
		Settings.logLevel = 0;
		
		args[0] = "C:/Users/hao/workspace/PJApps/app/663e8eb52c7b4a14e2873b1551748587018661b3.apk";
		args[1] = "com.android.main.MainService";
		args[2] = "execTask";
		args[3] = "Ctx";
		
		//Main.main(args);
		System.out.println("REs: " + Results.results);
		System.out.println("REs: " + Results.targetCallRes);
		
		DalvikVM vm = new DalvikVM();
		Object[] params = new Object[3];
		params[0] = "3lgoagdmfejekgfos9t15chojm";
		params[1] = new PrimitiveInfo(3); 
		params[2] = new PrimitiveInfo(3);
		try {
			vm.runMethod("C:/Users/hao/workspace/PJApps/app/663e8eb52c7b4a14e2873b1551748587018661b3.apk",
					"com.android.main.Base64", "encodebook", null, params);
		} catch (ZipException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	@Test
	public void testEncryption() {
		DalvikVM vm = new DalvikVM();
		Object[] params = new Object[3];
		params[0] = "2maodb3ialke8mdeme3gkos9g1icaofm";
		params[1] = new PrimitiveInfo(6); 
		params[2] = new PrimitiveInfo(3);
		try {
			vm.runMethod("C:/Users/hao/workspace/PJApps/app/663e8eb52c7b4a14e2873b1551748587018661b3.apk",
					"com.android.main.Base64", "encodebook", null, params);
		} catch (ZipException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
