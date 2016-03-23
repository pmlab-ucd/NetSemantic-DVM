package fu.hao.trust.data;

import patdroid.core.MethodInfo;
import patdroid.dalvik.Instruction;

public class Branch {
	protected Instruction inst;
	protected MethodInfo method;
	protected int index;

	public Branch(Instruction inst, int index, MethodInfo method) {
		this.inst = inst;
		this.method = method;
		this.index = index;
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
