package fu.hao.trust.data;

import patdroid.dalvik.Instruction;
import fu.hao.trust.dvm.DalvikVM;

/**
 * @ClassName: SymbolicVar
 * @Description: Representation of a variable whose value is symbolic (so has constraints).
 * @author: Hao Fu
 * @date: Mar 30, 2016 2:19:54 PM
 */
public abstract class SymbolicVar extends MultiValueVar {
	public abstract void addConstriant(DalvikVM vm, Instruction inst);
	
	public abstract Object getValue();
	
	public abstract void setValue(Object value);
	
}
