package fu.hao.trust.solver;

import java.util.HashSet;
import java.util.Set;

import fu.hao.trust.data.SymbolicVar;
import fu.hao.trust.dvm.DalvikVM;
import patdroid.core.ClassInfo;
import patdroid.dalvik.Instruction;

/**
 * @ClassName: InfluVar
 * @Description: Influencing Variable, e.g. variables from URLConnection
 * @author: Hao Fu
 * @date: Mar 10, 2016 7:20:04 PM
 */
public class InfluVar extends SymbolicVar {
	static Set<Class<?>> influList;
	final String TAG = "InfluVar";
	private ClassInfo type;
	private Instruction src;
	
	boolean on = false;
	
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
		this.type = type; 
		this.src = src;
		addConcreteVal(value);
	}
	
	@Override
	public void addConstriant(DalvikVM vm, Instruction inst) {
		// Null or not
		
	}
	
	@Override
	public String toString() {
		return "[InfluVar for " + getValue() + "]";
	}

	@Override
	public Object getValue() {
		return getLastVal();
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
		addConcreteVal(value);
	}

	public boolean isOn() {
		return on;
	}
	

}
