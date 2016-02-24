package fu.hao.trust.dvm;

import java.io.IOException;
import java.util.zip.ZipException;

import fu.hao.trust.analysis.Taint;
import fu.hao.trust.utils.Settings;

/**
 * @ClassName: Main
 * @Description: TODO
 * @author: hao
 * @date: Feb 15, 2016 12:08:30 AM
 */
public class Main {
	public static void main(String[] args) {
		DalvikVM vm = DalvikVM.v();
		Settings.logLevel = 0;
		Settings.apkPath = "C:/Users/hao/workspace/GeneralJava_Loop1/app/app-debug.apk";
		Settings.apkName = "app-debug";
		try {
			Taint taint = Taint.v();
			vm.runMethod(
					"C:/Users/hao/workspace/GeneralJava_Loop1/app/app-debug.apk",
					"de.ecspride.LoopExample1", "onCreate", taint);
		} catch (ZipException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
