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

import java.util.HashSet;
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
    
	public static String apkPath;
	public static String platformDir;
	public static String apkName;
	public static String suspClass;
	public static String suspMethod;
	
	private static Set<String> callBlackList;
	
	static DalvikVM vm;
	
	public static void reset() {
		apkPath = null;
		platformDir = null;
		apkName = null;
		suspClass = null;
		suspMethod = null;
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


}
