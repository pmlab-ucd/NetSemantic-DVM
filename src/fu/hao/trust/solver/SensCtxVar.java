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
	
	public SensCtxVar(ClassInfo type) {
		type = this.type;
	}

	@Override
	public void addConstriant(DalvikVM vm, Instruction inst) {
		// TODO Auto-generated method stub
		
	}
	

}
