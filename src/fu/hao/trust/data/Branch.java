package fu.hao.trust.data;

import patdroid.core.MethodInfo;
import patdroid.dalvik.Instruction;

public class Branch {
	Instruction inst;
	MethodInfo method;

	public Branch(Instruction inst, MethodInfo method) {
		this.inst = inst;
		this.method = method;
	}
	
	public Instruction getInstruction() {
		return inst;
	}
	
	public MethodInfo getMethod() {
		return method;
	}
	
	@Override
	public String toString() {
		return "[Branch: " + inst + "@" + method + "]";
	}

}
