package fu.hao.trust.data;

//import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import patdroid.dalvik.Instruction;

public class Results {
	public static Set<Object> results = new HashSet<>();
	public static Map<Instruction, TargetCall> targetCallRes;// = new HashMap<>();
	
	public static void reset() {
		results = new HashSet<>();
	}
}
