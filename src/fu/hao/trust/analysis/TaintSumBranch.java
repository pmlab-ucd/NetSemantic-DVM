package fu.hao.trust.analysis;

import java.util.LinkedList;
import java.util.Map;
import java.util.Stack;

import fu.hao.trust.data.Branch;
import fu.hao.trust.data.MultiValueVar;
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
public class TaintSumBranch extends Taint {

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

		private boolean isLoop(DalvikVM vm, Instruction inst) {
			Instruction[] insns = vm.getCurrStackFrame().getMethod().insns;

			// Scan to check whether the block contains "Return".
			for (int i = vm.getPC(); i < (int) inst.extra; i++) {
				if (insns[i].opcode == Instruction.OP_RETURN) {
					// 遇到return后再往前走的第一个goto index即<rest>起始点
					for (int j = i; j < insns.length; j++) {
						if (insns[j].opcode == Instruction.OP_GOTO
								&& (int) insns[j].extra <= i) {
							if ((int) insns[j].extra <= vm.getNowPC()) {
								Log.msg(TAG, "Detect Loop!");
								return true;
							}
						}
					}
				}
			}

			return false;
		}

		private boolean isNewBiDir(DalvikVM vm, Instruction inst) {
			Instruction[] insns = vm.getCurrStackFrame().getMethod().insns;
			MethodInfo currtMethod = vm.getCurrStackFrame().getMethod();

			// Scan to check whether the block contains "Return".
			for (int i = vm.getPC(); i < (int) inst.extra; i++) {
				if (insns[i].opcode == Instruction.OP_RETURN) {
					BiDirBranch branch = new BiDirBranch(inst, vm.getNowPC(),
							currtMethod, vm);
					branch.setSumPoint(insns[i]);
					Log.bb(TAG, "BiDirSumpoint " + insns[i]);
					bidirBranches.add(branch);
					// 遇到return后再往前走的第一个goto index即<rest>起始点
					for (int j = i; j < insns.length; j++) {
						if (insns[j].opcode == Instruction.OP_GOTO
								&& (int) insns[j].extra <= i) {
							Log.bb(TAG, "Set rest begin "
									+ insns[(int) insns[j].extra]);
							branch.setRestBegin(insns[(int) insns[j].extra]);
						}
					}

					return true;
				}
			}

			return false;
		}

		/**
		 * @Title: isCondBiDir
		 * @Author: Hao Fu
		 * @Description: Whether is a condition statement of a existign
		 *               bidirBranch
		 * @param @param vm
		 * @param @param inst
		 * @param @return
		 * @return boolean
		 * @throws
		 */
		private boolean isCondBiDir(DalvikVM vm, Instruction inst) {
			Instruction[] insns = vm.getCurrStackFrame().getMethod().insns;
			MethodInfo method = null;
			MethodInfo currtMethod = vm.getCurrStackFrame().getMethod();
			if (!bidirBranches.isEmpty()) {
				method = bidirBranches.getLast().getMethod();
			}

			if (currtMethod == method) {
				if ((int) inst.extra < vm.getPC()) {
					return true;
				}

				for (int i = vm.getPC(); i < (int) inst.extra; i++) {
					// Whether is a <cond> of current bidirBranch
					if (insns[i].opcode == Instruction.OP_GOTO
							&& (int) insns[i].extra < vm.getPC()) {
						return true;
					}
				}
			}

			return false;
		}

		private boolean isNewSimple(DalvikVM vm, Instruction inst) {
			if (simpleBranches.isEmpty()
					|| !simpleBranches.peek().getInstructions().contains(inst)) {
				return true;
			}

			return false;
		}

		private boolean isCondSimple(DalvikVM vm, Instruction inst) {
			if (simpleBranches.isEmpty()) {
				return false;
			}
			// In case of multiple conditions (not multiple blocks).
			Branch branch = simpleBranches.peek();
			for (Instruction cond : branch.getInstructions()) {
				if (((int) cond.extra) == ((int) inst.extra)) {
					branch.addInst(inst);
					Log.bb(TAG, "Add cond inst "
							+ branch.getInstructions().getLast() + "@" + branch);
					return true;
				}
			}

			return false;
		}

		@Override
		public Map<String, Map<Object, Instruction>> flow(DalvikVM vm,
				Instruction inst, Map<String, Map<Object, Instruction>> ins) {
			TAINT_OP_IF taintOp = new TAINT_OP_IF();
			Map<String, Map<Object, Instruction>> outs = taintOp.flow(vm, inst, ins);

			Register r0 = null, r1 = null;
			if (inst.r0 != -1) {
				r0 = vm.getReg(inst.r0);
			}

			if (inst.r1 != -1) {
				r1 = vm.getReg(inst.r1);
			}

			// When unknown exists in the branch
			if (r0 != null
					&& (r0.getData() instanceof MultiValueVar)
					|| r1 != null
					&& (r1.getData() instanceof MultiValueVar)) {
				// Force to explore <then>
				vm.setPC(vm.getNowPC() + 1);

				// Handling bidirBranch
				MethodInfo currtMethod = vm.getCurrStackFrame().getMethod();
				Branch branch = null;
				if (isLoop(vm, inst)) {
					return outs;
				}	else if (isNewBiDir(vm, inst)) {
					branch = bidirBranches.getLast();
				} else if (isCondBiDir(vm, inst)) {
					branch = bidirBranches.getLast();
					// Overwrite the previous bidirBranch.
					branch.addInst(inst);
					Log.bb(TAG, "Add bidir cond " + inst);
					((BiDirBranch) branch).backup(vm);
					((BiDirBranch) branch).setRmFlag(false);
				} else {
					if (isCondSimple(vm, inst)) {
						branch = simpleBranches.peek();
					} else if (isNewSimple(vm, inst)) {
						// Handling SimpleBranch
						branch = new Branch(inst, vm.getNowPC(), currtMethod);
						simpleBranches.add(branch);
						Log.warn(TAG, "New Simple Branch " + branch);
					}
					// Whether is the last blk
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
				Log.bb(TAG, "Add " + inst + " to met of " + branch);
				branch.addMet(inst);
			}
			return outs;
		}
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
		public Map<String, Map<Object, Instruction>> flow(DalvikVM vm,
				Instruction inst, Map<String, Map<Object, Instruction>> ins) {
			// Backup the original value of over-written heap element
			TAINT_OP_INSTANCE_PUT_FIELD taintOp = new TAINT_OP_INSTANCE_PUT_FIELD();
			Map<String, Map<Object, Instruction>> outs = taintOp.flow(vm, inst, ins);
			
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

			return outs;
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
		public Map<String, Map<Object, Instruction>> flow(DalvikVM vm,
				Instruction inst, Map<String, Map<Object, Instruction>> ins) {
			// Backup the original value of over-written heap element
			TAINT_OP_STATIC_PUT_FIELD taintOp = new TAINT_OP_STATIC_PUT_FIELD();
			Map<String, Map<Object, Instruction>> outs = taintOp.flow(vm, inst, ins);
			
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

			return outs;
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
			Log.bb(TAG, "Remove simple branch " + branch);
		}

		if (!bidirBranches.isEmpty()
				&& bidirBranches.getLast().getSumPoint() == inst) {
			BiDirBranch branch = bidirBranches.getLast();
			Log.msg(TAG, "Arrive at sum point of bidirbranch " + branch);
			
			if (inst.opcode_aux == Instruction.OP_RETURN_SOMETHING) {
				branch.addValue(vm.getReg(inst.r0), vm.getReg(inst.r0).getData());
			}
			
			if (branch.getRmFlag()) {
				Log.msg(TAG, "Rm BiDirBranch " + branch);
				branch = bidirBranches.removeLast();
				
				if (branch.getSumPoint().opcode_aux == Instruction.OP_RETURN_SOMETHING) {
					branch.valCombination();
				}
			} else {
				// backtrace to last unknown branch
				Log.bb(TAG, "Back to " + branch.getInstructions().getLast());
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
				|| !bidirBranches.isEmpty()
				&& bidirBranches.getLast().Met(inst)) {
			vm.setPass(true);
			// FIXME Not right?
			vm.jump(inst, false);
		}

	}

	@Override
	public Map<String, Map<Object, Instruction>> runAnalysis(DalvikVM vm, Instruction inst, Map<String, Map<Object, Instruction>> ins) {
		// Add conflict vars.
		if (vm.getAssigned() != null && vm.getAssigned()[1] != null && vm.getAssigned()[0] != vm.getReturnReg()) {
			if (!simpleBranches.isEmpty()
					&& vm.getCurrStackFrame().getMethod() == simpleBranches
							.peek().getMethod()) {
				// Set the assigned var as combined value
				Object[] assigned = vm.getAssigned();
				Branch branch = simpleBranches.peek();
				if (assigned[0] instanceof Register && Branch.checkType(assigned[1], assigned[2])) {
					Log.bb(TAG, "Assigned: " + assigned[1] + " " + assigned[2]);
					branch.addValue((Register) assigned[0], assigned[1]);
					branch.addValue((Register) assigned[0], assigned[2]);
				}
			}

			/*
			if (!bidirBranches.isEmpty()
					&& vm.getCurrStackFrame().getMethod() == bidirBranches
							.peek().getMethod()) {
				// Set the assigned var as combined value
				Object[] assigned = vm.getAssigned();
				Branch branch = bidirBranches.getLast();
				if (Branch.checkType(assigned[1], assigned[2])) {
					Log.bb(TAG, "Assigned: " + assigned[1] + " " + assigned[2]);
					// FIXME Support heap element.
					if (assigned[0] instanceof Register) {
						// The original value before the blk will be definitely
						// overwritten.
						branch.addValue((Register) assigned[0], assigned[2]);
					}
				}
			}*/
		}

		return super.runAnalysis(vm, inst, ins);
	}

	public TaintSumBranch() {
		super();
		simpleBranches = new Stack<>();

		byteCodes.put(0x08, new ATAINT_OP_CMP());

		auxByteCodes.put(0x35, new ATAINT_OP_STATIC_PUT_FIELD());
		auxByteCodes.put(0x37, new ATAINT_OP_INSTANCE_PUT_FIELD());
	}

}
