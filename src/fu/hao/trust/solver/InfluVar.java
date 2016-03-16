package fu.hao.trust.solver;

import java.util.HashSet;
import java.util.Set;

import fu.hao.trust.dvm.DalvikVM;
import patdroid.core.ClassInfo;
import patdroid.dalvik.Dalvik;
import patdroid.dalvik.Instruction;
import patdroid.util.Log;

/**
 * @ClassName: InfluVar
 * @Description: Influencing Variable, e.g. URLConnection
 * @author: Hao Fu
 * @date: Mar 10, 2016 7:20:04 PM
 */
public class InfluVar implements BiDirVar {
	Object var;
	static Set<Class<?>> ctxList;
	final String TAG = "DynCtxVar";
	
	static {
		ctxList = new HashSet<>();
		// ctxList.add(Dalvik.findClass("java.net.URLConnection"));
		try {
			ctxList.add(Class.forName("java.net.URLConnection"));
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static boolean isInfluVar(Object obj) {
		for (Class<?> clazz : ctxList) {
			if (clazz.isInstance(obj)) {
				return true;
			}
		}
		
		return false;
	}
	
	public InfluVar(Object obj) throws ClassNotFoundException {
		Log.warn(TAG, obj.toString());
		var = obj;
	}
	
	@Override
	public void addConstriant(DalvikVM vm, Instruction inst) {
		// Null or not
		
	}

}
