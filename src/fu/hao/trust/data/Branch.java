package fu.hao.trust.data;

import java.util.Set;

import patdroid.core.MethodInfo;
import patdroid.dalvik.Instruction;

public class Branch {
	protected Instruction inst;
	protected MethodInfo method;
	protected int index;
	protected Instruction stopSign;
	/**
	 * @fieldName: conflitTarget
	 * @fieldType: Set<Object>
	 * @Description: The memory element who has confliction d
	 */
	protected Set<Object> conflitTarget; 

	public Branch(Instruction inst, int index, MethodInfo method) {
		this.inst = inst;
		this.method = method;
		this.index = index;
		stopSign = method.insns[((int) inst.extra)];
	}
	
	public Instruction getInstruction() {
		return inst;
	}
	
	public MethodInfo getMethod() {
		return method;
	}
	
	@Override
	public String toString() {
		return "[Branch: " + index + "--" + inst + "@" + method.name + "]";
	}

}
