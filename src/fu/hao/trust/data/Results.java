package fu.hao.trust.data;

//import java.util.HashMap;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.content.Intent;
import patdroid.dalvik.Instruction;

public class Results {
	public static Set<Object> results = new HashSet<>();
	public static Map<Instruction, TargetCall> targetCallRes;// = new HashMap<>();
	public static List<Intent> intents;
	public static Intent intent;
	
	public static void reset() {
		results = new HashSet<>();
		intent = null;
	}
	
	public static void addIntent(Intent intent) {
		if (intents == null) {
			intents = new ArrayList<>();
		}
		intents.add(intent);
	}
}
