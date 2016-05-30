package fu.hao.trust.data;

//import java.util.HashMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
	
	public static void reset() {
		results = new HashSet<>();
		intent = null;
		taintedFields = null;
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
		
		getTaintedFields().put(fieldInfo, infos);
	}

	public static void setTainedFields(Map<String, Pair<Object, Instruction>> taintedFields) {
		Results.taintedFields = taintedFields;
	}
}
