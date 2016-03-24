package fu.hao.trust.solver;

import java.util.HashSet;
import java.util.Set;

import fu.hao.trust.dvm.DalvikVM;
import patdroid.core.ClassInfo;
import patdroid.dalvik.Instruction;

/**
 * @ClassName: InfluVar
 * @Description: Influencing Variable, e.g. variables from URLConnection
 * @author: Hao Fu
 * @date: Mar 10, 2016 7:20:04 PM
 */
public class InfluVar implements BiDirVar {
	private Object value;
	static Set<Class<?>> influList;
	final String TAG = "InfluVar";
	private ClassInfo type;
	private Instruction src;
	
	static {
		influList = new HashSet<>();
		// influList.add(Dalvik.findClass("java.net.URLConnection"));
		try {
			influList.add(Class.forName("java.net.URLConnection"));
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public static boolean isInfluVar(Object obj) {
		for (Class<?> clazz : influList) {
			if (clazz.isInstance(obj)) {
				return true;
			}
		}
		
		return false;
	}
	
	public InfluVar(ClassInfo type, Object value, Instruction src) throws ClassNotFoundException {
		this.value = value;
		this.type = type; 
		this.src = src;
	}
	
	@Override
	public void addConstriant(DalvikVM vm, Instruction inst) {
		// Null or not
		
	}
	
	@Override
	public String toString() {
		return "[InfluVar for " + value + "]";
	}

	@Override
	public Object getValue() {
		return value;
	}
	
	public ClassInfo getType() {
		return type;
	}

	public void setType(ClassInfo type) {
		this.type = type;
	}

	public Instruction getSrc() {
		return src;
	}

	public void setSrc(Instruction src) {
		this.src = src;
	}

	@Override
	public void setValue(Object value) {
		this.value = value;
	}
	

}
