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
	public static Map<Instruction, TargetCall> targetCallRes;// = new HashMap<>();
	public static List<Intent> intents;
	public static Intent intent;
	// (class: fieldname), value, srcApis
	private static Map<String, Pair<Object, Instruction>> taintedFields;
	private static boolean hasNewTaintedHeapLoc;
	
	private static String TAG = Results.class.getSimpleName();
	
	public static void reset() {
		results = new HashSet<>();
		intent = null;
		taintedFields = null;
		hasNewTaintedHeapLoc = false;
	}
	
	public static void addIntent(Intent intent) {
		if (intents == null) {
			intents = new ArrayList<>();
		}
		intents.add(intent);
	}

	public static Map<String, Pair<Object, Instruction>> getTaintedFields() {
		if (taintedFields == null) {
			taintedFields = new HashMap<>();
		}
		return taintedFields;
	}
	
	public static void addTaintedField(String fieldInfo, Pair<Object, Instruction> infos) {
		// Only support String field now.
		if (!(infos.getFirst() instanceof String)) {
			return;
		}
		
		Log.msg(TAG, "New recorded tainted field: " + fieldInfo);
		
		getTaintedFields().put(fieldInfo, infos);
	}

	public static void setTainedFields(Map<String, Pair<Object, Instruction>> taintedFields) {
		Results.taintedFields = taintedFields;
	}

	public static boolean isHasNewTaintedHeapLoc() {
		return hasNewTaintedHeapLoc;
	}

	public static void setHasNewTaintedHeapLoc(boolean hasNewTaintedHeapLoc) {
		Log.msg(TAG, "Set new tainted heap loc true");
		Results.hasNewTaintedHeapLoc = hasNewTaintedHeapLoc;
	}
}
