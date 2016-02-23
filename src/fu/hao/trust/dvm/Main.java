package fu.hao.trust.dvm;

import java.io.IOException;
import java.util.zip.ZipException;

import fu.hao.trust.utils.Settings;

/**
 * @ClassName: Main
 * @Description: TODO
 * @author: hao
 * @date: Feb 15, 2016 12:08:30 AM
 */
public class Main {
	public static void main(String[] args) {
		DalvikVM vm = new DalvikVM();
		Settings.logLevel = 8;
		try {
			vm.runMethod(
						"C:/Users/hao/workspace/GeneralJava_Loop1/app/app-debug.apk",
						"de.ecspride.LoopExample1", "onCreate", null);
		} catch (ZipException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
