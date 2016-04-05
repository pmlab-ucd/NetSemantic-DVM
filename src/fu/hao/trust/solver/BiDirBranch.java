package fu.hao.trust.solver;

import patdroid.core.MethodInfo;
import patdroid.dalvik.Instruction;
import fu.hao.trust.data.Branch;
import fu.hao.trust.data.VMState;

/**
 * @ClassName: BiDirBranch
 * @Description: The conditional statement whose "then" block contains "return",
 *               so that "else if " part also should be executed and bi-direction.
 * @author: Hao Fu
 * @date: Mar 30, 2016 3:27:47 PM
 */
public class BiDirBranch extends Branch {
	VMState state;

	public BiDirBranch(Instruction inst, int index, MethodInfo method,
			VMState state) {
		super(inst, index, method);
		this.state = state;
	}

	public VMState getState() {
		return state;
	}

	public BiDirBranch(Instruction inst, int index, MethodInfo method) {
		super(inst, index, method);
	}

}
