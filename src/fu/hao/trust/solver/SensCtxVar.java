package fu.hao.trust.solver;

import patdroid.core.ClassInfo;
import patdroid.dalvik.Instruction;
import fu.hao.trust.dvm.DalvikVM;

/**
 * @ClassName: SensCtxVar
 * @Description: Sensitive Contextual Variable (e.g. imei)
 * @author: Hao Fu
 * @date: Mar 15, 2016 8:53:58 PM
 */
public class SensCtxVar implements BiDirVar{
	
	private ClassInfo type;
	private Object value;
	
	public SensCtxVar(ClassInfo type, Object value) {
		this.setType(type);
		this.value = value;
	}

	@Override
	public void addConstriant(DalvikVM vm, Instruction inst) {
		// Null or not
		
	}

	@Override
	public Object getValue() {
		return value;
	}
	
	@Override
	public String toString() {
		return "[SensCtxVar for " + value + "]";
	}

	public ClassInfo getType() {
		return type;
	}

	public void setType(ClassInfo type) {
		this.type = type;
	}
	

}
