package fu.hao.trust.solver;

import java.util.HashSet;
import java.util.Set;

import patdroid.core.ClassInfo;
import patdroid.dalvik.Dalvik;

/**
 * @ClassName: CtxVar
 * @Description: Contextual Variable, e.g. URLConnection
 * @author: Hao Fu
 * @date: Mar 10, 2016 7:20:04 PM
 */
public class CtxVar {
	Object var;
	Set<Class<?>> ctxList;
	
	public boolean isCtxVar(Object obj) {
		for (Class<?> clazz : ctxList) {
			if (clazz.isInstance(obj)) {
				return true;
			}
		}
		
		return false;
	}
	
	CtxVar() throws ClassNotFoundException {
		ctxList = new HashSet<>();
		// ctxList.add(Dalvik.findClass("java.net.URLConnection"));
		ctxList.add(Class.forName("java.net.URLConnection"));
	}

}
