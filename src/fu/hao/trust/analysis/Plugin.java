package fu.hao.trust.analysis;

import java.util.Set;

import patdroid.dalvik.Instruction;
import fu.hao.trust.dvm.DalvikVM;

public abstract class Plugin {
	protected Set<Object> currtRes;
	public abstract Set<Object> runAnalysis(DalvikVM vm, Instruction inst, Set<Object> in);
	
	public abstract Set<Object> getCurrRes(); 
}
