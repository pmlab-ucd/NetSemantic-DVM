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

import fu.hao.trust.analysis.ContextAnalysis;
import fu.hao.trust.analysis.InfluenceAnalysis;
import fu.hao.trust.analysis.Plugin;
import fu.hao.trust.analysis.PluginManager;
import fu.hao.trust.analysis.Taint;
import fu.hao.trust.analysis.TaintAdv;
import fu.hao.trust.data.Results;
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

			Main main = new Main();
			Settings.reset();
			// Settings.logLevel = 0;
			PluginManager pluginManager = new PluginManager();
			Settings.apkName = "Test";

			for (int i = 0; i < args.length; i++) {
				if (args[i] != null && args[i].equalsIgnoreCase("Taint")) {
					pluginManager.addPlugin(new Taint());
				} else if (args[i] != null && args[i].equalsIgnoreCase("ATaint")) {
					pluginManager.addPlugin(new TaintAdv());
				} else if (args[i] != null && args[i].equalsIgnoreCase("Ctx")) {
					Plugin plugin = new ContextAnalysis();
					pluginManager.addPlugin(plugin);
				} else if (args[i] != null && args[i].equalsIgnoreCase("Influ")) {
					Plugin plugin = new InfluenceAnalysis();
					pluginManager.addPlugin(plugin);
				} else if (args[i] != null && args[i].equalsIgnoreCase("Full")) {
					// TODO
					Settings.apkPath = args[0];// + "app-release.apk";
					Settings.suspClass = args[1];
					Settings.suspMethod = args[2];
					pluginManager.addPlugin(new ContextAnalysis());
					pluginManager.addPlugin(new InfluenceAnalysis());
					main.runMethod(pluginManager);
					return;
				}
			}

			if (args[2] != null && !"".equals(args[2])) {
				Settings.apkPath = args[0];// + "app-release.apk";
				Settings.suspClass = args[1];
				Settings.suspMethod = args[2];
				main.runMethod(pluginManager);
				return;
			}

			for (final String fileName : apkFiles) {
				Results.reset();
				Settings.apkName = fileName;
				Log.debug(TAG, fileName);
				Settings.apkPath = args[0] + fileName;

				// Run callbacks
				String csv = "C:/Users/hao/workspace/TRUST/sootOutput/"
						+ Settings.apkName + "_dummy.csv";
				Log.debug(TAG, csv);
				CSVReader reader = new CSVReader(new FileReader(csv));
				
				for (String[] items : reader.readAll()) {
					main.runMethods(items, pluginManager);
				}

				reader.close();

				// Run suspicious function.
				csv = "C:/Users/hao/workspace/TRUST/sootOutput/"
						+ Settings.apkName + ".csv";
				File file = new File(csv);
				if (!file.exists()) {
					return;
				}

				reader = new CSVReader(new FileReader(csv));
				Log.debug(TAG, csv);
				for (String[] items : reader.readAll()) {
					Settings.suspClass = items[0];
					Settings.suspMethod = items[1];
					Log.debug(TAG, items[0]);
					main.runMethod(pluginManager);
				}

				reader.close();

			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void runMethod(PluginManager pluginManager) {
		// DalvikVM vm = DalvikVM.v();
		// Results.reset();
		DalvikVM vm = new DalvikVM();
		try {
			vm.runMethod(Settings.apkPath, Settings.suspClass,
					Settings.suspMethod, pluginManager, null);
		} catch (ZipException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void runMethods(String[] items, PluginManager pluginManager) {
		// DalvikVM vm = DalvikVM.v();
		// Results.reset();
		DalvikVM vm = new DalvikVM();
		try {
			vm.runMethods(Settings.apkPath, items, pluginManager);
		} catch (ZipException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
