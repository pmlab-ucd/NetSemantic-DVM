package fu.hao.trust.dvm;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

import fu.hao.trust.analysis.PluginManager;
import fu.hao.trust.data.Results;
import fu.hao.trust.utils.Log;
import fu.hao.trust.utils.Settings;

public class ResolveIntent {

	final static String TAG = "ResolveIntent";

	public static void main(String[] args) {
		try {
			mainTest(args);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void mainTest(String[] args) throws IOException,
			FileNotFoundException {
		File apkFile = new File(args[0]);
		List<String> apkFiles = new ArrayList<>();
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
			if (extension.equalsIgnoreCase(".apk")) {
				apkFiles.add(args[0]);
			}
		}

		for (String apkfile : apkFiles) {
			Settings.apkPath = apkfile;
			Log.msg(TAG, apkfile);
			File apk = new File(Settings.apkPath);
			Settings.apkName = apk.getName();
			String csv = "C:/Users/hao/workspace/NetSemantic-Static/sootOutput/"
					+ Settings.apkName + "_resp_startActServ.csv";
			CSVReader reader = new CSVReader(new FileReader(csv));
			PluginManager pluginManager = new PluginManager();
			Set<String[]> intents = new HashSet<>();
			for (String[] items : reader.readAll()) {
				String[] intent = new String[4];
				intent[0] = items[0];
				intent[1] = items[1];
				intent[2] = "";
				intent[3] = "";
				for (int i = 1; i < items.length; i++) {
					Results.reset();
					Settings.entryClass = items[i].split(": ")[0];
					Settings.entryMethod = items[i].split(": ")[1];
					Main main = new Main();
					main.runMethod(pluginManager);
					if (Results.intent != null) {
						if (Results.intent.getAction() != null) {
							intent[2] = Results.intent.getAction();
						} 
						if (Results.intent.getTargetClass() != null) {
							intent[3] = Results.intent.getTargetClass().fullName;
						} 
						break;
					}
				}
				intents.add(intent);
			}
			reader.close();
			writeCSV(intents);
		}
	}

	public static void writeCSV(Set<String[]> intents) throws IOException {
		String csv = "./output/intents/" + Settings.apkName + "_intents.csv";
		File csvFile = new File(csv);
		Log.msg(TAG, csv);
		if (!csvFile.exists()) {
			csvFile.createNewFile();
		} else {
			csvFile.delete();
			csvFile.createNewFile();
		}
		CSVWriter writer = new CSVWriter(new FileWriter(csv, true));
		List<String[]> results = new ArrayList<>();
		for (String[] intent : intents) {
			List<String> result = new ArrayList<>();
			for (String str : intent) {
				result.add(str);
				Log.bb(TAG, str);
			}

			String[] resultArray = (String[]) result.toArray(new String[result
					.size()]);
			results.add(resultArray);
		}

		writer.writeAll(results);
		writer.close();
	}

}
