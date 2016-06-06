package fu.hao.trust.data;

//import java.util.HashMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import fu.hao.trust.utils.Log;
import android.content.Intent;
import patdroid.dalvik.Instruction;
import patdroid.util.Pair;

public class Results {
	public static Set<Object> results = new HashSet<>();
	public static Map<Instruction, TargetCall> targetCallRes;// = new
																// HashMap<>();
	public static List<Intent> intents;
	public static Intent intent;
	// (class: fieldname), value, srcApis
	private static Map<String, Pair<Object, Instruction>> sTaintedFields; // static
	private static Map<String, Pair<Object, Instruction>> iTaintedFields; // instance
	// APIs, e.g. setHint() and getHint()
	private static Map<String, Pair<Object, Instruction>> aTaintedFields; 
	private static boolean hasNewTaintedHeapLoc;

	private static String TAG = Results.class.getSimpleName();
	
	static {
		sTaintedFields = new HashMap<>();
		iTaintedFields = new HashMap<>();
		aTaintedFields = new HashMap<>();
	}

	public static void reset() {
		results = new HashSet<>();
		intent = null;
		sTaintedFields = new HashMap<>();
		hasNewTaintedHeapLoc = false;
	}

	public static void addIntent(Intent intent) {
		if (intents == null) {
			intents = new ArrayList<>();
		}
		intents.add(intent);
	}

	public static Map<String, Pair<Object, Instruction>> getSTaintedFields() {
		return sTaintedFields;
	}

	public static void addTaintedField(String fieldInfo,
			Pair<Object, Instruction> infos,
			Map<String, Pair<Object, Instruction>> taintedFields) {
		// Only support String field now.
		if (!(infos.getFirst() instanceof String)) {
			return;
		}

		Log.msg(TAG, "New recorded tainted field: " + fieldInfo);

		taintedFields.put(fieldInfo, infos);
	}

	public static void setSTainedFields(
			Map<String, Pair<Object, Instruction>> taintedFields) {
		Results.sTaintedFields = taintedFields;
	}

	public static boolean isHasNewTaintedHeapLoc() {
		return hasNewTaintedHeapLoc;
	}

	public static void setHasNewTaintedHeapLoc(boolean hasNewTaintedHeapLoc) {
		Log.msg(TAG, "Set new tainted heap loc true");
		Results.hasNewTaintedHeapLoc = hasNewTaintedHeapLoc;
	}

	public static Map<String, Pair<Object, Instruction>> getITaintedFields() {
		return iTaintedFields;
	}

	public static void setITaintedFields(
			Map<String, Pair<Object, Instruction>> iTaintedFields) {
		Results.iTaintedFields = iTaintedFields;
	}

	public static Map<String, Pair<Object, Instruction>> getATaintedFields() {
		return aTaintedFields;
	}

	public static void setATaintedFields(
			Map<String, Pair<Object, Instruction>> aTaintedFields) {
		Results.aTaintedFields = aTaintedFields;
	}
}
