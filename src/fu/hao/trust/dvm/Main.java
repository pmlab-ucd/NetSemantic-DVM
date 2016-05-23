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

import fu.hao.trust.analysis.FullAnalysis;
import fu.hao.trust.analysis.PluginManager;
import fu.hao.trust.analysis.Taint;
import fu.hao.trust.analysis.TaintSumBranch;
import fu.hao.trust.data.Results;
import fu.hao.trust.utils.Log;
import fu.hao.trust.utils.Settings;

/**
 * @ClassName: Main
 * @Description: Main
 * @author: hao
 * @date: Feb 15, 2016 12:08:30 AM
 */
public class Main {
	final static String TAG = "main";
	// for the constructor
	private static String[] initArgTypes;
	private static Object[] initArgs;

	// for the method with parameters
	private static Object[] miParams;

	public static void main(String[] args, Object[] miParams) {
		initMI(miParams);
		main(args);
	}

	public static void main(String[] args, Object[] miParams,
			String[] initArgTypes, Object[] initArgs) {
		initMI(miParams);
		initThisObj(initArgTypes, args);
		main(args);
	}

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
					apkFiles.add(apkFile + File.separator + s);
				}
			} else {
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

			for (final String apk : apkFiles) {
				long beforeRun = System.nanoTime();
				Settings.reset();
				Settings.apkPath = apk;
				File file = new File(apk);
				Settings.apkName = file.getName();
				Main main = new Main();
				// Settings.logLevel = 0;
				PluginManager pluginManager = new PluginManager();
				for (int i = 0; i < args.length; i++) {
					if (args[i] != null && args[i].equalsIgnoreCase("Taint")) {
						pluginManager.addPlugin(new Taint());
					} else if (args[i] != null
							&& args[i].equalsIgnoreCase("ATaint")) {
						pluginManager.addPlugin(new TaintSumBranch());
					} else if (args[i] != null
							&& args[i].equalsIgnoreCase("Full")) {
						pluginManager.addPlugin(new FullAnalysis());
					}
				}

				if (args[2] != null && !"".equals(args[2])) {
					Settings.entryClass = args[1];
					Settings.entryMethod = args[2];
					getResolvedIntents();
					main.runMethod(pluginManager);
					Log.msg(TAG, "Analysis has run for "
							+ (System.nanoTime() - beforeRun) / 1E9
							+ " seconds\n");
				}
			}

			/*
			 * for (final String fileName : apkFiles) { beforeRun =
			 * System.nanoTime(); Results.reset(); Settings.apkName = fileName;
			 * Log.debug(TAG, fileName); Settings.apkPath = args[0] + fileName;
			 * 
			 * // Run callbacks String csv =
			 * "C:/Users/hao/workspace/TRUST/sootOutput/" + Settings.apkName +
			 * "_dummy.csv"; Log.debug(TAG, csv); CSVReader reader = new
			 * CSVReader(new FileReader(csv));
			 * 
			 * for (String[] items : reader.readAll()) { main.runMethods(items,
			 * pluginManager); }
			 * 
			 * reader.close();
			 * 
			 * // Run suspicious function. csv =
			 * "C:/Users/hao/workspace/TRUST/sootOutput/" + Settings.apkName +
			 * ".csv"; File file = new File(csv); if (!file.exists()) { return;
			 * }
			 * 
			 * reader = new CSVReader(new FileReader(csv)); Log.debug(TAG, csv);
			 * for (String[] items : reader.readAll()) { Settings.suspClass =
			 * items[0]; Settings.suspMethod = items[1]; Log.debug(TAG,
			 * items[0]); main.runMethod(pluginManager); }
			 * 
			 * reader.close(); Log.msg(TAG, "Analysis has run for " +
			 * (System.nanoTime() - beforeRun) / 1E9 + " seconds\n");
			 * 
			 * }
			 */

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void initThisObj(String[] argTypeNames, Object[] args) {
		initArgTypes = argTypeNames;
		initArgs = args;
	}

	private static void initMI(Object[] params) {
		miParams = params;
	}

	public void runMethod(PluginManager pluginManager) {
		// DalvikVM vm = DalvikVM.v();
		// Results.reset();
		DalvikVM vm = new DalvikVM(Settings.apkPath);

		if (initArgs != null || initArgTypes != null) {
			vm.initThisObj(Settings.entryClass, initArgTypes, initArgs);
		}

		try {
			vm.runMethod(Settings.apkPath, Settings.entryClass,
					Settings.entryMethod, pluginManager, miParams);
		} catch (ZipException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void runMethods(String[] items, PluginManager pluginManager) {
		// DalvikVM vm = DalvikVM.v();
		// Results.reset();
		DalvikVM vm = new DalvikVM(Settings.apkPath);
		try {
			vm.runMethods(Settings.apkPath, items, pluginManager);
		} catch (ZipException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void getResolvedIntents() {
		try {
			String csv = "./output/intents/" + Settings.apkName + "_target.csv";
			File file = new File(csv);
			if (file.exists()) {
				CSVReader reader = new CSVReader(new FileReader(csv));
				for (String[] intent : reader.readAll()) {
					Settings.addIntentTarget(intent[0], intent[1]);
				}

				reader.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
