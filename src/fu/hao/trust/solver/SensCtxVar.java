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
	Object value;
	
	public SensCtxVar(ClassInfo type, Object value) {
		type = this.type;
		value = this.value;
	}

	@Override
	public void addConstriant(DalvikVM vm, Instruction inst) {
		// Null or not
		
	}

	@Override
	public Object getValue() {
		return value;
	}
	

}
