package fu.hao.trust.test;

import java.io.IOException;
import java.util.zip.ZipException;

import org.junit.Test;

import patdroid.core.PrimitiveInfo;
import fu.hao.trust.dvm.DalvikVM;

public class TestPjapps {

	@Test
	public void test() {
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
