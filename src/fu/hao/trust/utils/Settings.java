/*
* Copyright 2016 Hao Fu and contributors
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
* Contributors:
*   Hao Fu
*/

package fu.hao.trust.utils;

import fu.hao.trust.dvm.DalvikVM;
import patdroid.util.Log;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Settings {
    /**
     * Minimum log level to be printed
     */
    public static int logLevel = Log.MODE_MSG;
    /**
     * The report mode generates a JSON output
     */
    public static boolean enableReportMode = logLevel >= Log.MODE_REPORT;
    /**
     * The Android API Level
     */
    public static int apiLevel = 15;
    
	private static String apkPath;
	public static String platformDir;
	private static String apkName;
	//public static String suspClass;
	//public static String suspMethod;
	// Whether to exec onCreate() when init Activity
	public static boolean execOnCreate = true;
	private static Map<String, String> intentTargets;
	
	private static Set<String> callBlackList;
	
	static DalvikVM vm;
	
	public static boolean fullLifeExec = false;
	
	private static String entryMethod;
	private static String entryClass;
	
	public static String logTag;
	
	private static List<Pair<String, String>> eventChain; 
	
	public static void reset() {
		setApkPath(null);
		platformDir = null;
		apkName = null;
		setEntryClass(null);
		setEntryMethod(null);
		intentTargets = null;
		callBlackList = null;
		fullLifeExec = false;
		setEntryMethod(null);
	}
	
	public static String getRuntimeCaller() {
		StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
		StackTraceElement caller;
		
		caller = stackTraceElements[3];
		
		// 0 is toString()
		// 1 is this method
		// 2 is the target
		// 3 is the caller of the target
		return "Caller: " + caller.getMethodName() + "@" + caller.getClassName();
	}
	
	public static String getRuntimeCaller(int layer) {
		StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
		StackTraceElement caller;
		if (stackTraceElements.length >= layer) { 
			caller = stackTraceElements[layer];
			return "Caller@" + layer + ": " + caller.getMethodName() + "@" + caller.getClassName();
		} else {
			caller = stackTraceElements[3];
			return "Caller: " + caller.getMethodName() + "@" + caller.getClassName(); 
		}
		
		
	}
	
	public static void setVM(DalvikVM vm) {
		Settings.vm = vm;
	}
	
	public static DalvikVM getVM() {
		return vm;
	}
	
	public static void addCallBlkListElem(String mname) {
		if (callBlackList == null) {
			callBlackList = new HashSet<>();
		}
		
		callBlackList.add(mname);
	}
	
	public static boolean callBlkListHas(String mname) {
		if (callBlackList == null) {
			return false;
		}
		for (String name : callBlackList) {
			if (mname.contains(name)) {
				return true;
			}
		}
		
		return false;
	}
	
	
	public static void addIntentTarget(String action, String intentTarget) {
		if (intentTargets == null) {
			intentTargets = new HashMap<>();
		}
		
		intentTargets.put(action, intentTarget);
	}
	
	public static String getIntentTarget(String action) {
		return intentTargets == null ? null : intentTargets.get(action);
	}

	public static List<Pair<String, String>> getEventChain() {
		if (eventChain == null) {
			eventChain = new LinkedList<>();
		}
		return eventChain;
	}

	public static void setEventChain(List<Pair<String, String>> eventChain) {
		Settings.eventChain = eventChain;
	}

	public static String getOutdir() {
		String outdir = "output/" + Settings.apkName;
		File dir = new File(outdir);
		if (!dir.exists()) {
			if (dir.mkdir()) {
				System.out.println("Directory is created!");
			} else {
				System.out.println("Failed to create directory!");
			}
		}
		return outdir + File.separator;
	}
	
	public static String getStaticOutDir() {
		String outdir = "C:/Users/hao/workspace/NetSemantic-Static/sootOutput/" + Settings.apkName;
		File dir = new File(outdir);
		if (!dir.exists()) {
			if (dir.mkdir()) {
				System.out.println("Directory is created!");
			} else {
				System.out.println("Failed to create directory!");
			}
		}
		return outdir + File.separator;
	}

	public static String getEntryClass() {
		return entryClass;
	}

	public static void setEntryClass(String entryClass) {
		Settings.entryClass = entryClass;
	}

	public static String getEntryMethod() {
		return entryMethod;
	}

	public static void setEntryMethod(String entryMethod) {
		Settings.entryMethod = entryMethod;
	}

	public static String getApkName() {
		return apkName;
	}

	public static void setApkName(String apkName) {
		Settings.apkName = apkName;
	}

	public static String getApkPath() {
		return apkPath;
	}

	public static void setApkPath(String apkPath) {
		Settings.apkPath = apkPath;
	}

}
