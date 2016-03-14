package fu.hao.trust.analysis;

import java.lang.reflect.Method;
import java.util.Set;

import patdroid.dalvik.Instruction;
import fu.hao.trust.dvm.DalvikVM;

public abstract class Plugin {
	public Set<Object> currtRes;
	public Method method;
	public Instruction condition;
	public Instruction interested;
	/**
	 * @fieldName: here
	 * @fieldType: boolean
	 * @Description: If the current execed inst is interested, set here to true;
	 */
	public boolean here;
	public abstract Set<Object> runAnalysis(DalvikVM vm, Instruction inst, Set<Object> in);
	
	public abstract Set<Object> getCurrRes(); 
	
	public abstract void reset();
}
