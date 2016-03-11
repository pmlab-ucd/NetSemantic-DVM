package fu.hao.trust.solver;

import java.util.HashSet;
import java.util.Set;

import patdroid.core.ClassInfo;
import patdroid.dalvik.Dalvik;
import patdroid.dalvik.Instruction;

/**
 * @ClassName: CtxVar
 * @Description: Contextual Variable, e.g. URLConnection
 * @author: Hao Fu
 * @date: Mar 10, 2016 7:20:04 PM
 */
public class InfluVar {
	Object var;
	static Set<Class<?>> ctxList;
	
	public static boolean isCtxVar(Object obj) {
		for (Class<?> clazz : ctxList) {
			if (clazz.isInstance(obj)) {
				return true;
			}
		}
		
		return false;
	}
	
	public InfluVar(Object obj) throws ClassNotFoundException {
		if (ctxList == null) {
			ctxList = new HashSet<>();
			// ctxList.add(Dalvik.findClass("java.net.URLConnection"));
			ctxList.add(Class.forName("java.net.URLConnection"));
		}
		
		var = obj;
	}
	
	public void addConstraint(Instruction inst) {
		
	}

}
