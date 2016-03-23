package fu.hao.trust.solver;

import patdroid.core.MethodInfo;
import patdroid.dalvik.Instruction;
import fu.hao.trust.data.Branch;
import fu.hao.trust.data.VMState;

public class BiDirBranch extends Branch {
	
	VMState state;

	public BiDirBranch(Instruction inst, int index, MethodInfo method, VMState state) {
		super(inst, index, method);
		this.state = state;
	}
	
	public VMState getState() {
		return state;
	}
	
	

}
