package fu.hao.trust.dvm;

import java.io.IOException;
import java.util.zip.ZipException;

import fu.hao.trust.analysis.Results;
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
		//DalvikVM vm = DalvikVM.v();
		DalvikVM vm = new DalvikVM();
		//Settings.logLevel = 0;
		Settings.apkPath = args[0];
		Settings.susp = args[1]; 
		Settings.apkName = "app-debug";
		Results.reset();
		try {
			Taint taint = Taint.v();
			vm.runMethod(
					Settings.apkPath,
					Settings.susp, "onCreate", taint);
		} catch (ZipException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}
