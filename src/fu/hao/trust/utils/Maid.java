package fu.hao.trust.utils;

import java.util.Set;

public class Maid {
	
	public static boolean isElem(Set<String> strSet, String string) {
		for (String str : strSet) {
			if (str.contains(string) || string.contains(str)) {
				return true;
			}
		}
		
		return false;
	}

}
