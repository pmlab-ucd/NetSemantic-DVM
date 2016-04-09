package fu.hao.trust.solver;

import patdroid.core.MethodInfo;
import patdroid.dalvik.Instruction;
import fu.hao.trust.data.Branch;
import fu.hao.trust.data.VMState;
import fu.hao.trust.dvm.DalvikVM;
import fu.hao.trust.utils.Log;
import fu.hao.trust.utils.Settings;

/**
 * @ClassName: BiDirBranch
 * @Description: The conditional statement whose "then" block contains "return",
 *               so that "else if " part also should be executed and bi-direction.
 * @author: Hao Fu
 * @date: Mar 30, 2016 3:27:47 PM
 */
public class BiDirBranch extends Branch {
	VMState state;
	/**
	 * @fieldName: remove
	 * @fieldType: boolean
	 * @Description: Whether remove this branch from the stack
	 */
	boolean remove;

	public BiDirBranch(Instruction inst, int index, MethodInfo method,
			DalvikVM vm) {
		super(inst, index, method);
		Log.warn(Settings.getRuntimeCaller(), "New BiDirBranch " + this);
		this.state = vm.storeState();
		remove = false;
	}
	
	public void setRmFlag(boolean remove) {
		this.remove = remove;
	}
	
	public boolean getRmFlag() {
		return remove;
	}

	public VMState getState() {
		return state;
	}

	public BiDirBranch(Instruction inst, int index, MethodInfo method) {
		super(inst, index, method);
	}

}
