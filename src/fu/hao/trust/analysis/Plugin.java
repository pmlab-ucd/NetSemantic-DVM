package fu.hao.trust.analysis;

import java.lang.reflect.Method;
import java.util.Map;

import patdroid.dalvik.Instruction;
import fu.hao.trust.dvm.DalvikVM;

public abstract class Plugin {
	public Map<Object, Method> currtRes;
	public Method method;
	public Instruction condition;
	public Instruction interested;
	/**
	 * @fieldName: here
	 * @fieldType: boolean
	 * @Description: If the current execed inst is interested, set here to true;
	 */
	public boolean here;
	public abstract Map<Object, Method> runAnalysis(DalvikVM vm, Instruction inst, Map<Object, Method> in);
	
	public abstract Map<Object, Method> getCurrRes(); 
	
	public abstract void reset();
}
