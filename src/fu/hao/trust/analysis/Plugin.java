package fu.hao.trust.analysis;

import java.lang.reflect.Method;
import java.util.Map;

import patdroid.dalvik.Instruction;
import fu.hao.trust.dvm.DalvikVM;

public abstract class Plugin {
	public Map<Object, Instruction> currtRes;
	public Method method;
	public Instruction condition;
	
	public abstract Map<Object, Instruction> runAnalysis(DalvikVM vm, Instruction inst, Map<Object, Instruction> in);
	
	public abstract Map<Object, Instruction> getCurrRes(); 
	
	public abstract void reset();
	
	public abstract void preprocessing(DalvikVM vm, Instruction inst);
}
