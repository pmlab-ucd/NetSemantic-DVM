package fu.hao.trust.data;

import java.lang.reflect.Method;
import java.util.LinkedList;

import fu.hao.trust.dvm.DalvikVM.Register;
import fu.hao.trust.dvm.DalvikVM.StackFrame;
import fu.hao.trust.dvm.VMHeap;

public class VMState {
	VMHeap heap;
	int pc;
	LinkedList<StackFrame> stack;

	// Instruction condition;

	// The reg that stores return value;
	Register retValReg;

	Register[] callingCtx;

	Method pluginMethod;

	public VMState(VMHeap heap, LinkedList<StackFrame> stack, Register retValReg,
			Register[] callingCtx, int pc, Method pluginMethod) {
		this.heap = heap;
		this.stack = stack;
		this.retValReg = retValReg;
		this.callingCtx = callingCtx;
		this.pc = pc;
		this.pluginMethod = pluginMethod;
	}
	
	public VMHeap getHeap() {
		return heap;
	}
	
	public int getPC() {
		return pc;
	}
	
	public LinkedList<StackFrame> getStack() {
		return stack;
	}
	
	public Register getRetValReg() {
		return retValReg;
	}
	
	public Register[] getCallingCtx() {
		return callingCtx;
	}
	
	public Method getPluginMethod() {
		return pluginMethod;
	}

}
