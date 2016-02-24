package fu.hao.trust.analysis;

import java.util.HashSet;
import java.util.Set;

public class Results {
	public static Set<Object> results = new HashSet<>();
	
	public static void reset() {
		results = new HashSet<>();
	}
}
