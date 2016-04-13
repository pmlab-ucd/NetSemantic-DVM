package fu.hao.trust.analysis;

import java.util.LinkedList;
import java.util.Map;
import java.util.Stack;

import fu.hao.trust.data.Branch;
import fu.hao.trust.data.SymbolicVar;
import fu.hao.trust.dvm.DVMClass;
import fu.hao.trust.dvm.DVMObject;
import fu.hao.trust.dvm.DalvikVM;
import fu.hao.trust.dvm.DalvikVM.Register;
import fu.hao.trust.solver.BiDirBranch;
import fu.hao.trust.utils.Log;
import patdroid.core.FieldInfo;
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
	
	protected boolean hasRestore = false;

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

				// Scan to check whether the block contains "Return"
				Instruction[] insns = vm.getCurrStackFrame().getMethod().insns;
				MethodInfo method = null;
				MethodInfo currtMethod = vm.getCurrStackFrame().getMethod();
				if (bidirBranches.peek() != null) {
					method = bidirBranches.peek().getMethod();
				}
				for (int i = vm.getPC(); i < (int) inst.extra; i++) {
					if (insns[i].opcode == Instruction.OP_RETURN) {
						// FIXME In case of multiple conditions.
						BiDirBranch branch = new BiDirBranch(inst,
								vm.getNowPC(), currtMethod, vm);
						branch.addMet(inst);
						branch.setSumPoint(insns[i]);
						Log.bb(TAG, "BiDirSumpoint " + insns[i]);
						bidirBranches.add(branch);
						// vm.addBiDirBranch(branch);
						Log.bb(TAG, "Update BiDirBranch " + branch);
						// 遇到return后再往前走的第一个goto index即<rest>起始点
						for (int j = i; j < insns.length; j++) {
							if (insns[j].opcode == Instruction.OP_GOTO
									&& (int) insns[j].extra <= i) {
								Log.bb(TAG, "Set rest begin "
										+ insns[(int) insns[j].extra]);
								branch.setRestBegin(insns[(int) insns[j].extra]);
								break;
							}
						}

						return out;
					}

					if (insns[i].opcode == Instruction.OP_GOTO) {
						if (currtMethod == method) {
							BiDirBranch branch = bidirBranches.peek();
							// Overwrite the previous bidirBranch.
							branch.addInst(inst);
							Log.bb(TAG, branch);
							branch.backup(vm);
							branch.setRmFlag(false);
							return out;
						}
					}

				}

				// Jump to <rest>
				if ((int) inst.extra < vm.getPC()) {
					if (currtMethod == method) {
						BiDirBranch branch = bidirBranches.peek();
						// Overwrite the previous bidirBranch.
						branch.addInst(inst);
						Log.bb(TAG, branch);
						branch.backup(vm);
						branch.setRmFlag(false);
						return out;
					}
				}

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

			branch.addMet(inst);
			return branch;
		}

	}

	/**
	 * @Title: preprocessing
	 * @Author: Hao Fu
	 * @Description: Run before the interpreter.run()
	 * @param @param vm
	 * @param @param inst
	 * @see fu.hao.trust.analysis.Taint#preprocessing(fu.hao.trust.dvm.DalvikVM,
	 *      patdroid.dalvik.Instruction)
	 */
	public void preprocessing(DalvikVM vm, Instruction inst) {
		if (!simpleBranches.isEmpty()
				&& simpleBranches.peek().getSumPoint() == inst) {
			Branch branch = simpleBranches.pop();
			branch.valCombination();
			Log.bb(TAG, "Remove branch ");
		}

		if (!bidirBranches.isEmpty()
				&& bidirBranches.peek().getSumPoint() == inst) {
			BiDirBranch branch = bidirBranches.peek();
			Log.msg(TAG, "Arrive at sum point of bidirbranch " + branch);
			if (branch.getRmFlag()) {
				branch = bidirBranches.removeLast();
				if (branch.getSumPoint().opcode_aux != Instruction.OP_RETURN_VOID) {
					branch.valCombination();
				}
			} else {
				// backtrace to last unknown branch
				Log.bb(TAG, "Back to " + branch);
				branch.restore(vm);
				hasRestore = true;
				// vm.restoreFullState();
				// FIXME currently do not explore all blks yet.
				branch.setRmFlag(true);
				// TODO Add the current plugin result.
				vm.setPass(true);
			}
		}

		// To avoid infinity loop
		if (!simpleBranches.isEmpty() && simpleBranches.peek().Met(inst)
				|| !bidirBranches.isEmpty() && bidirBranches.peek().Met(inst)) {
			vm.setPass(true);
			vm.jump(inst, false);
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

		if (!bidirBranches.isEmpty() && vm.getAssigned() != null) {
			// Set the assigned var as combined value
			Object[] assigned = vm.getAssigned();
			Branch branch = bidirBranches.peek();
			if (Branch.checkType(assigned[1], assigned[2])) {
				Log.bb(TAG, "Assigned: " + assigned[1] + " " + assigned[2]);
				// FIXME Support heap element.
				if (assigned[0] instanceof Register) {
					// The original value before the blk will be definitely
					// overwritten.
					branch.addValue((Register) assigned[0], assigned[2]);
				}
			}
		}

		return super.runAnalysis(vm, inst, in);
	}

	class ATAINT_OP_INSTANCE_PUT_FIELD implements Rule {
		final String TAG = getClass().getSimpleName();

		/**
		 * @Title: func
		 * @Description: iput v0,v2, Test2.i6:I // field@0002 Stores v1 into
		 *               field@0002 (entry #2 in the field id table). The
		 *               instance is referenced by v0
		 * @param vm
		 * @param inst
		 * @see fu.hao.trust.dvm.ByteCode#func(fu.hao.trust.dvm.DalvikVM,
		 *      patdroid.dalvik.Instruction)
		 */
		@Override
		public Map<Object, Instruction> flow(DalvikVM vm, Instruction inst,
				Map<Object, Instruction> in) {
			// Backup the original value of over-written heap element
			TAINT_OP_INSTANCE_PUT_FIELD taintOp = new TAINT_OP_INSTANCE_PUT_FIELD();
			Map<Object, Instruction> out = taintOp.flow(vm, inst, in);
			DVMObject obj = (DVMObject) vm.getReg(inst.r0).getData();
			FieldInfo fieldInfo = (FieldInfo) inst.extra;
			if (vm.getAssigned() != null) {
				for (BiDirBranch branch : bidirBranches) {
					if (!branch.getState().isSaved(obj, fieldInfo)) {
						branch.getState().saveField(obj,
								(FieldInfo) vm.getAssigned()[1],
								vm.getAssigned()[2]);
					}
				}
			}

			return out;
		}
	}

	class ATAINT_OP_STATIC_PUT_FIELD implements Rule {
		final String TAG = getClass().getSimpleName();

		/**
		 * @Title: func
		 * @Description: iput v0,v2, Test2.i6:I // field@0002 Stores v1 into
		 *               field@0002 (entry #2 in the field id table). The
		 *               instance is referenced by v0
		 * @param vm
		 * @param inst
		 * @see fu.hao.trust.dvm.ByteCode#func(fu.hao.trust.dvm.DalvikVM,
		 *      patdroid.dalvik.Instruction)
		 */
		@Override
		public Map<Object, Instruction> flow(DalvikVM vm, Instruction inst,
				Map<Object, Instruction> in) {
			// Backup the original value of over-written heap element
			TAINT_OP_STATIC_PUT_FIELD taintOp = new TAINT_OP_STATIC_PUT_FIELD();
			Map<Object, Instruction> out = taintOp.flow(vm, inst, in);
			if (vm.getAssigned() != null) {
				DVMClass clazz = (DVMClass) vm.getAssigned()[0];
				String fieldName = (String) vm.getAssigned()[1];
				for (BiDirBranch branch : bidirBranches) {
					if (!branch.getState().isSaved(clazz, fieldName)) {
						branch.getState().saveField(clazz, fieldName,
								vm.getAssigned()[2]);
					}
				}
			}

			return out;
		}
	}

	public TaintAdv() {
		simpleBranches = new Stack<>();

		byteCodes.put(0x08, new ATAINT_OP_CMP());

		auxByteCodes.put(0x35, new ATAINT_OP_STATIC_PUT_FIELD());
		auxByteCodes.put(0x37, new ATAINT_OP_INSTANCE_PUT_FIELD());
	}

}
