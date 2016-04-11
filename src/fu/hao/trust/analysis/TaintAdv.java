package fu.hao.trust.analysis;

import java.util.LinkedList;
import java.util.Map;
import java.util.Stack;

import fu.hao.trust.data.Branch;
import fu.hao.trust.data.SymbolicVar;
import fu.hao.trust.dvm.DalvikVM;
import fu.hao.trust.dvm.DalvikVM.Register;
import fu.hao.trust.solver.BiDirBranch;
import fu.hao.trust.utils.Log;
import patdroid.core.MethodInfo;
import patdroid.dalvik.Instruction;

/**
 * @ClassName: TaintAdv
 * @Description: The taint analysis with branch summary support.
 * @author: Hao Fu
 * @date: Mar 27, 2016 12:59:39 PM
 */
public class TaintAdv extends Taint {

	private final String TAG = getClass().getSimpleName();

	/**
	 * @fieldName: unknownCond
	 * @fieldType: Stack<Instruction>
	 * @Description: To store the unknown simpleBranches met for this trace.
	 */
	protected LinkedList<BiDirBranch> bidirBranches = new LinkedList<>();

	/**
	 * @fieldName: sumPoints
	 * @fieldType: Map<Instruction,Branch>
	 * @Description: the summary point of the branch, in which the values
	 *               combined
	 */
	protected Stack<Branch> simpleBranches;

	class ATAINT_OP_CMP implements Rule {
		final String TAG = getClass().getName();

		@Override
		public Map<Object, Instruction> flow(DalvikVM vm, Instruction inst,
				Map<Object, Instruction> in) {
			TAINT_OP_IF taintOp = new TAINT_OP_IF();
			Map<Object, Instruction> out = taintOp.flow(vm, inst, in);

			Register r0 = null, r1 = null;
			if (inst.r0 != -1) {
				r0 = vm.getReg(inst.r0);
			}

			if (inst.r1 != -1) {
				r1 = vm.getReg(inst.r1);
			}

			// When unknown exists in the branch
			if (r0 != null
					&& (r0.getData() instanceof SymbolicVar || out
							.containsKey(r0))
					|| r1 != null
					&& (r1.getData() instanceof SymbolicVar || out
							.containsKey(r1))) {
				vm.setPC(vm.getNowPC() + 1);

				// TODO out.get(vm.getReg(inst.r0))
				int simpleBranch = -1;

				// Scan to check whether the block contains "Return"
				Instruction[] insns = vm.getCurrStackFrame().getMethod().insns;
				for (int i = vm.getPC(); i < (int) inst.extra; i++) {
					if (insns[i].opcode == Instruction.OP_RETURN
							|| insns[i].opcode == Instruction.OP_GOTO) {
						simpleBranch = i - 1;
						break;
					}
				}

				MethodInfo currtMethod = vm.getCurrStackFrame().getMethod();

				if (simpleBranch == -1) {
					Branch branch = isNewBranch(vm, inst, currtMethod);
					if (branch != null) {
						if (currtMethod.insns[((int) inst.extra) - 1].opcode == Instruction.OP_IF) {
							branch.addInst(currtMethod.insns[((int) inst.extra) - 1]);
							Log.bb(TAG, "Add cond inst "
									+ branch.getInstructions().getLast() + "@"
									+ branch);
						} else {
							branch.setSumPoint(currtMethod.insns[((int) inst.extra)]);
							Log.bb(TAG, "Set sum point " + inst.extra + " "
									+ branch.getSumPoint());
						}
					}
				} else {
					// FIXME In case of multiple conditions.
					BiDirBranch branch = new BiDirBranch(inst, vm.getNowPC(),
							vm.getCurrStackFrame().getMethod(), vm);
					branch.setSumPoint(vm.getCurrStackFrame().getInst(
							simpleBranch));
					Log.bb(TAG, "BiDirSumpoint "
							+ vm.getCurrStackFrame().getInst(simpleBranch));
					bidirBranches.add(branch);
					vm.addBiDirBranch(branch);
					Log.warn(TAG, "New BiDirBranch " + branch);
				}

				Log.msg(TAG, "ATAINT_OP_IF: " + " " + inst + " " + inst.extra);
			}

			return out;
		}

		private Branch isNewBranch(DalvikVM vm, Instruction inst,
				MethodInfo currtMethod) {
			Branch branch = simpleBranches.isEmpty() ? null : simpleBranches
					.peek();

			if (branch != null) {
				if (currtMethod.insns[((int) inst.extra) - 1].opcode == Instruction.OP_IF) {
					return branch;
				}
				
				for (Instruction cond : branch.getInstructions()) {
					if (((int) cond.extra) == ((int) inst.extra)) {
						branch.addInst(inst);
						Log.bb(TAG, "Add cond inst "
								+ branch.getInstructions().getLast() + "@"
								+ branch);
						return branch;
					}
				}
			}

			// In case of multiple conditions (not multiple blocks).
			if (branch == null || !branch.getInstructions().contains(inst)) {
				branch = new Branch(inst, vm.getNowPC(), currtMethod);
				simpleBranches.add(branch);
				Log.warn(TAG, "New Simple Branch " + branch);
			}

			return branch;
		}

	}

	class ATAINT_OP_MOV_CONST implements Rule {
		final String TAG = getClass().toString();

		@Override
		public Map<Object, Instruction> flow(DalvikVM vm, Instruction inst,
				Map<Object, Instruction> in) {

			TAINT_OP_MOV_CONST taintOp = new TAINT_OP_MOV_CONST();
			Map<Object, Instruction> out = taintOp.flow(vm, inst, in);
			/*
			 * if (!simpleBranches.isEmpty()) { // TODO Add only one, but could
			 * be influenced by multiple APIs //out.put(vm.getReg(inst.rdst),
			 * addVarsOfBranch.values().iterator() //.next()); //for
			 * (Instruction met : addVarsOfBranch.values()) { //Log.bb(TAG,
			 * " dep API " + met); //} Log.warn(TAG, "Found influenced var at "
			 * + vm.getReg(inst.rdst)); } else { Log.bb(TAG,
			 * "Not API Recording"); }
			 */

			return out;
		}

	}

	class ATAINT_OP_MOV_REG implements Rule {
		final String TAG = getClass().toString();

		@Override
		public Map<Object, Instruction> flow(DalvikVM vm, Instruction inst,
				Map<Object, Instruction> in) {
			TAINT_OP_MOV_REG taintOp = new TAINT_OP_MOV_REG();
			Map<Object, Instruction> out = taintOp.flow(vm, inst, in);
			/*
			 * if (!addVarsOfBranch.isEmpty() &&
			 * !out.containsKey(vm.getReg(inst.rdst))) { // TODO Now only add
			 * one, but could be influenced by multiple APIs
			 * out.put(vm.getReg(inst.rdst), addVarsOfBranch.values().iterator()
			 * .next());
			 * 
			 * if (!(vm.getReg(inst.rdst).getData() instanceof SymbolicVar)) {
			 * ConcreteVar mcvv; if (vm.getReg(inst.rdst).getData() instanceof
			 * ConcreteVar) { mcvv = vm.g
			 * mcvv.addValue(vm.getReg(inst.rdst).getData()); } } Log.warn(TAG,
			 * "Found influenced var at " + vm.getReg(inst.rdst)); } else {
			 * Log.bb(TAG, "Not API Recording"); }
			 */

			return out;
		}

	}

	public void preprocessing(DalvikVM vm, Instruction inst) {
		if (!simpleBranches.isEmpty()
				&& simpleBranches.peek().getSumPoint() == inst) {
			Branch branch = simpleBranches.pop();
			branch.valCombination();
			Log.bb(TAG, "Remove branch ");
		}

	}

	@Override
	public Map<Object, Instruction> runAnalysis(DalvikVM vm, Instruction inst,
			Map<Object, Instruction> in) {
		if (!simpleBranches.isEmpty() && vm.getAssigned() != null) {
			// Set the assigned var as combined value
			Object[] assigned = vm.getAssigned();
			Branch branch = simpleBranches.peek();
			if (Branch.checkType(assigned[1], assigned[2])) {
				Log.bb(TAG, "Assigned: " + assigned[1] + " " + assigned[2]);
				branch.addValue((Register) assigned[0], assigned[1]);
				branch.addValue((Register) assigned[0], assigned[2]);
			}
		}

		if (!vm.getBiDirBranches().isEmpty()
				&& vm.getBiDirBranches().peek().getSumPoint() == inst) {
			BiDirBranch branch = vm.getBiDirBranches().peek();
			Log.msg(TAG, "Arrive at sum point of bidirbranch " + branch);
			branch.addValue(vm.getReturnReg(), vm.getReturnReg().getData());

			if (branch.getRmFlag()) {
				branch = vm.getBiDirBranches().removeLast();
				branch.valCombination();
			} else {
				// backtrace to last unknown branch
				vm.restoreState();
				// FIXME currently do not explore all blks yet.
				branch.setRmFlag(true);
			}
		}

		return super.runAnalysis(vm, inst, in);
	}

	class ATAINT_OP_MOV_RESULT implements Rule {
		final String TAG = getClass().getName();

		/**
		 * @Title: func
		 * @Description: move-result v0 Move the return value of a previous
		 *               method invocation into v0.
		 * @param vm
		 * @param inst
		 * @see fu.hao.trust.dvm.ByteCode#func(fu.hao.trust.dvm.DalvikVM,
		 *      patdroid.dalvik.Instruction)
		 */
		@Override
		public Map<Object, Instruction> flow(DalvikVM vm, Instruction inst,
				Map<Object, Instruction> in) {
			TAINT_OP_MOV_RESULT taintOp = new TAINT_OP_MOV_RESULT();
			Map<Object, Instruction> out = taintOp.flow(vm, inst, in);

			return out;
		}
	}

	class ATAINT_OP_GOTO implements Rule {
		/**
		 * @Title: func
		 * @Description: goto 0005 // -0010 Jumps to current position-16 words
		 *               (hex 10). 0005 is the label of the target instruction.
		 * @param vm
		 * @param inst
		 * @see fu.hao.trust.dvm.ByteCode#func(fu.hao.trust.dvm.DalvikVM,
		 *      patdroid.dalvik.Instruction)
		 */
		@Override
		public Map<Object, Instruction> flow(DalvikVM vm, Instruction inst,
				Map<Object, Instruction> in) {
			if (!vm.getBiDirBranches().isEmpty()
					&& vm.getBiDirBranches().peek().getMethod().insns.length - 1 == vm
							.getNowPC()) {
				vm.getBiDirBranches().peek().setRmFlag(true);
			}
			return in;
		}
	}

	public TaintAdv() {
		simpleBranches = new Stack<>();

		byteCodes.put(0x08, new ATAINT_OP_CMP());

		auxByteCodes.put(0x01, new ATAINT_OP_MOV_REG());
		auxByteCodes.put(0x02, new ATAINT_OP_MOV_CONST());
		auxByteCodes.put(0x15, new ATAINT_OP_MOV_RESULT());
	}

}
