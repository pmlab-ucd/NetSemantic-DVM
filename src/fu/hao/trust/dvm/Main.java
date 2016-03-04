package fu.hao.trust.dvm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipException;

import com.opencsv.CSVReader;

import fu.hao.trust.analysis.Results;
import fu.hao.trust.analysis.Taint;
import fu.hao.trust.utils.Log;
import fu.hao.trust.utils.Settings;

/**
 * @ClassName: Main
 * @Description: TODO
 * @author: hao
 * @date: Feb 15, 2016 12:08:30 AM
 */
public class Main {
	final static String TAG = "main";
	
	public static void main(String[] args) {
		Results.reset();

		// Settings.logLevel = 0;

		if (args[2] != null && !"".equals(args[2])) {
			Settings.apkPath = args[0];// + "app-release.apk";
			Settings.suspClass = args[1];
			Settings.suspMethod = args[2];
			runMethod();
			return;
		}

		try {
			List<String> apkFiles = new ArrayList<>();
			File apkFile = new File(args[0]);
			if (apkFile.isDirectory()) {
				String[] dirFiles = apkFile.list(new FilenameFilter() {
					@Override
					public boolean accept(File dir, String name) {
						return (name.endsWith(".apk"));
					}
				});
				for (String s : dirFiles) {
					apkFiles.add(s);
				}
			} else {
				// FIXME
				String extension = apkFile.getName().substring(
						apkFile.getName().lastIndexOf("."));
				if (extension.equalsIgnoreCase(".txt")) {
					BufferedReader rdr = new BufferedReader(new FileReader(
							apkFile));
					String line = null;
					while ((line = rdr.readLine()) != null)
						apkFiles.add(line);
					rdr.close();
				} else if (extension.equalsIgnoreCase(".apk"))
					apkFiles.add(args[0]);
				else {
					Log.err(TAG, "Invalid input file format: " + extension);
					return;
				}
			}

			for (final String fileName : apkFiles) {
				Settings.apkName = fileName;
				Log.debug(TAG, fileName);
				Settings.apkPath = args[0] + fileName;

				// Run callbacks
				String csv = "C:/Users/hao/workspace/TRUST/sootOutput/"
						+ Settings.apkName + "_dummy.csv";
				Log.debug(TAG, csv);
				CSVReader reader = new CSVReader(new FileReader(csv));

				for (String[] items : reader.readAll()) {
					runMethods(items);
				}
				
				reader.close();
				
				// Run suspicious function.
				csv = "C:/Users/hao/workspace/TRUST/sootOutput/"
						+ Settings.apkName + ".csv";
				reader = new CSVReader(new FileReader(csv));
				Log.debug(TAG, csv);
				for (String[] items : reader.readAll()) {
					Settings.suspClass = items[0];
					Settings.suspMethod = items[1];
					Log.debug(TAG, items[0]);
					runMethod();
				}
				
				reader.close();

				
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void runMethod() {
		// DalvikVM vm = DalvikVM.v();
		Results.reset();
		DalvikVM vm = new DalvikVM();
		try {
			Taint taint = new Taint();//Taint.v();
			vm.runMethod(Settings.apkPath, Settings.suspClass,
					Settings.suspMethod, taint);
		} catch (ZipException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void runMethods(String[] items) {
		// DalvikVM vm = DalvikVM.v();
		Results.reset();
		DalvikVM vm = new DalvikVM();
		try {
			Taint taint = new Taint();// Taint.v();
			vm.runMethods(Settings.apkPath, items, taint);
		} catch (ZipException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
