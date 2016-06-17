package fu.hao.trust.dvm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.zip.ZipException;

import patdroid.dalvik.Instruction;
import patdroid.util.Pair;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;

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
				Results.reset();
				Settings.reset();
				PluginManager pluginManager = new PluginManager();
				int i = 2;
				while (i < args.length) {
					if (args[i] == null) {
						i++;
					} if (args[i].equalsIgnoreCase("--runEntryException")) {
						Settings.setRunEntryException(true);
						i++;
					} else if (args[i].equalsIgnoreCase("--norun")) {
						Settings.addCallBlkListElem(args[i + 1]);
						i += 2;
					} if (args[i].equalsIgnoreCase("Taint")) {
						pluginManager.addPlugin(new Taint());
						i++;
					} else if (args[i].equalsIgnoreCase("ATaint")) {
						pluginManager.addPlugin(new TaintSumBranch());
						i++;
					} else if (args[i].equalsIgnoreCase("Full")) {
						pluginManager.addPlugin(new FullAnalysis());
						i++;
					} else {
						i++;
					}
				}
				
				long beforeRun = System.nanoTime();
				Settings.setApkPath(apk);
				File file = new File(apk);
				Settings.setApkName(file.getName());
				Main main = new Main();
				// Settings.logLevel = 0;
				
				if (args[1] != null && args[1].endsWith("EventChains")) {
					// Run callbacks
					String csv = Settings.getStaticOutDir()
							+ Settings.getApkName() + "_" + args[1] + ".csv";
					if (args[1].contains("src")) {
						Settings.setRecordTaintedFields(true);
					} else if (args[1].contains("sink")) {
						// Settings.setInitTaintedFields(true);
						// Settings.initTaintedFields();
					}
					System.out.println(TAG + csv);
					file = new File(csv);
					if (file.exists()) {
						CSVReader reader = new CSVReader(new FileReader(csv));
						Queue<List<Pair<String, String>>> eventChains = Settings
								.getSrcChains();
						for (String[] chain : reader.readAll()) {
							List<Pair<String, String>> eventChain = new LinkedList<>();
							for (String sootMethod : chain) {
								// sootMethod = sootMethod.replace("<", "");
								// sootMethod = sootMethod.replace(">", "");
								String[] splited = sootMethod.split(": ");
								String sootClass = splited[0];
								sootMethod = splited[1];
								Pair<String, String> event = new Pair<>(
										sootClass, sootMethod);
								eventChain.add(event);
							}
							eventChains.add(eventChain);
						}
						reader.close();

						csv = Settings.getStaticOutDir()
								+ Settings.getApkName()
								+ "_sinkEventChains.csv";

						reader = new CSVReader(new FileReader(csv));
						eventChains = Settings.getSinkChains();
						for (String[] chain : reader.readAll()) {
							List<Pair<String, String>> eventChain = new LinkedList<>();
							for (String sootMethod : chain) {
								// sootMethod = sootMethod.replace("<", "");
								// sootMethod = sootMethod.replace(">", "");
								String[] splited = sootMethod.split(": ");
								String sootClass = splited[0];
								sootMethod = splited[1];
								Pair<String, String> event = new Pair<>(
										sootClass, sootMethod);
								eventChain.add(event);
							}
							eventChains.add(eventChain);
						}
						reader.close();

						eventChains = Settings.getSrcChains();
						while (!eventChains.isEmpty()) {
							Settings.setCheckNewTaintedHeapLoc(true);
							Results.reset();
							List<Pair<String, String>> eventChain = eventChains
									.poll();
							Settings.setEventChain(eventChain);
							if (eventChain.size() > 0) {
								Pair<String, String> entryEvent = eventChain
										.remove(0);
								if (entryEvent.getSecond().startsWith(
										"onCreate")
										|| entryEvent.getSecond().startsWith(
												"onStart")
										|| entryEvent.getSecond().startsWith(
												"onReceive")) {
									Settings.setEntryClass(entryEvent
											.getFirst());
									Settings.setEntryMethod(entryEvent
											.getSecond());
									getResolvedIntents();
									Log.msg(TAG, "Src Chain: " + eventChain);
									Log.debug(TAG, "Src Entry event: "
											+ entryEvent);
									main.runMethod(pluginManager);
								} else {
									throw new RuntimeException("Entry method "
											+ entryEvent.getSecond()
											+ " is not supported!");
								}
							}
							eventChain.clear();
							// if (Settings.isRecordTaintedFields()) {
							// writeTaintedFields();
							// }
						}

						eventChains = Settings.getEventChains();
						while (!eventChains.isEmpty()) {
							Settings.setCheckNewTaintedHeapLoc(false);
							Results.reset();
							Log.msg(TAG,
									"hhas " + Results.isHasNewTaintedHeapLoc());
							List<Pair<String, String>> eventChain = eventChains
									.poll();
							Settings.setEventChain(eventChain);
							Log.debug(TAG, "Generated Entry event: "
									+ eventChain);
							if (eventChain.size() > 0) {
								Pair<String, String> entryEvent = eventChain
										.remove(0);
								Settings.setEntryClass(entryEvent.getFirst());
								Settings.setEntryMethod(entryEvent.getSecond());
								getResolvedIntents();

								main.runMethod(pluginManager);
							}
							eventChain.clear();
							if (Settings.isRecordTaintedFields()) {
								writeTaintedFields();
							}
						}

					}
				} else if (args[2] != null && !"".equals(args[2])) {
					Settings.setEntryClass(args[1]);
					Settings.setEntryMethod(args[2]);
					getResolvedIntents();
					main.runMethod(pluginManager);
				}

				Log.msg(TAG, "Analysis has run for "
						+ (System.nanoTime() - beforeRun) / 1E9 + " seconds\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Deprecated
	private static void writeTaintedFields() throws IOException {
		String csv = Settings.getOutdir() + Settings.getApkName()
				+ "_SrcTaintedFields.csv";
		File csvFile = new File(csv);
		Log.msg(TAG, csv);
		if (!csvFile.exists()) {
			csvFile.createNewFile();
		}

		CSVWriter writer = new CSVWriter(new FileWriter(csv, true));
		List<String[]> results = new ArrayList<>();
		for (String fieldInfo : Results.getSTaintedFields().keySet()) {
			List<String> result = new ArrayList<>();
			result.add(fieldInfo);
			Pair<Object, Instruction> infos = Results.getSTaintedFields().get(
					fieldInfo);
			result.add(infos.getFirst().toString());
			result.add(infos.getSecond().toString());
			String[] resultArray = (String[]) result.toArray(new String[result
					.size()]);
			results.add(resultArray);
		}

		for (String fieldInfo : Results.getITaintedFields().keySet()) {
			List<String> result = new ArrayList<>();
			result.add(fieldInfo);
			Pair<Object, Instruction> infos = Results.getITaintedFields().get(
					fieldInfo);
			result.add(infos.getFirst().toString());
			result.add(infos.getSecond().toString());
			String[] resultArray = (String[]) result.toArray(new String[result
					.size()]);
			results.add(resultArray);
		}

		for (String fieldInfo : Results.getATaintedFields().keySet()) {
			List<String> result = new ArrayList<>();
			result.add(fieldInfo);
			Pair<Object, Instruction> infos = Results.getATaintedFields().get(
					fieldInfo);
			result.add(infos.getFirst().toString());
			result.add(infos.getSecond().toString());
			String[] resultArray = (String[]) result.toArray(new String[result
					.size()]);
			results.add(resultArray);
		}

		writer.writeAll(results);
		writer.close();
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
		DalvikVM vm = new DalvikVM(Settings.getApkPath());

		if (initArgs != null || initArgTypes != null) {
			vm.initThisObj(Settings.getEntryClass(), initArgTypes, initArgs);
		}

		try {
			vm.runMethod(Settings.getApkPath(), Settings.getEntryClass(),
					Settings.getEntryMethod(), pluginManager, miParams);
		} catch (ZipException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Deprecated
	public void runMethods(String[] items, PluginManager pluginManager) {
		// DalvikVM vm = DalvikVM.v();
		// Results.reset();
		DalvikVM vm = new DalvikVM(Settings.getApkPath());
		try {
			vm.runMethods(Settings.getApkPath(), items, pluginManager);
		} catch (ZipException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void getResolvedIntents() {
		try {
			String csv = Settings.getOutdir() + Settings.getApkName()
					+ "_intent_target.csv";
			File file = new File(csv);
			if (file.exists()) {
				CSVReader reader = new CSVReader(new FileReader(csv));
				for (String[] intent : reader.readAll()) {
					Settings.addIntentTarget(intent[0], intent[1]);
					Log.msg(TAG, "Retrieve intent target: " + intent[0] + ", "
							+ intent[1]);
				}

				reader.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
