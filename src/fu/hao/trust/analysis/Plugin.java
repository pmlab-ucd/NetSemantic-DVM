package fu.hao.trust.analysis;

import java.lang.reflect.Method;
import java.util.Set;

import patdroid.dalvik.Instruction;
import fu.hao.trust.dvm.DalvikVM;

public abstract class Plugin {
	public Set<Object> currtRes;
	public Method method;
	public abstract Set<Object> runAnalysis(DalvikVM vm, Instruction inst, Set<Object> in);
	
	public abstract Set<Object> getCurrRes(); 
	
	public abstract void reset();
}
