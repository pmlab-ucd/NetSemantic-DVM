package fu.hao.trust.analysis;

import java.util.Map;
import java.util.Stack;

import patdroid.dalvik.Instruction;
import fu.hao.trust.data.Branch;
import fu.hao.trust.dvm.DalvikVM;
import fu.hao.trust.dvm.DalvikVM.Register;
import fu.hao.trust.solver.BiDirBranch;
import fu.hao.trust.utils.Log;

/**
 * @ClassName: TaintCtrlDep
 * @Description: Taint analysis with support of control dependence data
 *               correlation
 * @author: Hao Fu
 * @date: Apr 13, 2016 8:45:11 AM
 */
public class TaintCtrlDep extends TaintSumBranch {
	final String TAG = getClass().getSimpleName();

	// The interestedSimple branches that have CDTAINTVar inside the conditions.
	// It is a subset of simpleBranches.
	Stack<Branch> interestedSimple;
	// Add new interested when first meet or bracktrace, rm when encounter the
	// beginning of <rest>
	Stack<BiDirBranch> interestedBiDir;

	class CDTAINT_OP_CMP implements Rule {
		final String TAG = getClass().getName();

		/**
		 * @Title: func
		 * @Description: Helper func for if
		 * @param vm
		 * @param inst
		 * @see fu.hao.trust.dvm.ByteCode#func(fu.hao.trust.dvm.DalvikVM,
		 *      patdroid.dalvik.Instruction)
		 */
		public Map<Object, Instruction> flow(DalvikVM vm, Instruction inst,
				Map<Object, Instruction> in) {
			Branch branch = null;

			ATAINT_OP_CMP taintOp = new ATAINT_OP_CMP();
			Map<Object, Instruction> out = taintOp.flow(vm, inst, in);

			if (!simpleBranches.isEmpty()
					&& simpleBranches.peek().getInstructions().contains(inst)) {
				branch = simpleBranches.peek();
			} else if (!bidirBranches.isEmpty()
					&& bidirBranches.peek().getInstructions().contains(inst)) {
				branch = bidirBranches.peek();
			}

			if (branch != null) {
				Register r0 = null, r1 = null;
				if (inst.r0 != -1) {
					r0 = vm.getReg(inst.r0);
				}

				if (inst.r1 != -1) {
					r1 = vm.getReg(inst.r1);
				}

				// When sensitive value exists in the branch
				if (((r0 != null && out.containsKey(r0)) || (r1 != null && out
						.containsKey(r1)))) {
					branch.addElemSrc(out.get(r0) != null ? out.get(r0) : out
							.get(r1));
					if (branch instanceof BiDirBranch
							&& !interestedBiDir.contains(branch)) {
						interestedBiDir.add((BiDirBranch) branch);
					} else if (!interestedSimple.contains(branch)) {
						interestedSimple.add(branch);
					}

					Log.msg(TAG, "Add CDTAINTBranch " + branch);
				}
			}

			return out;
		}
	}

	@Override
	public Map<Object, Instruction> runAnalysis(DalvikVM vm, Instruction inst,
			Map<Object, Instruction> in) {
		Map<Object, Instruction> out = super.runAnalysis(vm, inst, in);

		if (vm.getAssigned() != null) {
			Object[] assigned = vm.getAssigned();
			// Add ctrl-dep correlated vars
			if (!interestedSimple.isEmpty()) {
				// Set the assigned var as combined value
				// FIXME Multiple controlling if.
				out.put((Register) assigned[0], interestedSimple.peek()
						.getElemSrcs().iterator().next());
				Log.msg(TAG, "Add correlated tained var " + assigned[0]);
			}

			if (!interestedBiDir.isEmpty()) {
				out.put((Register) assigned[0], interestedBiDir.peek()
						.getElemSrcs().iterator().next());
				Log.msg(TAG, "Add correlated tained var " + assigned[0]);
			}
		}

		return out;
	}

	@Override
	public void preprocessing(DalvikVM vm, Instruction inst) {
		super.preprocessing(vm, inst);
		if (!interestedSimple.isEmpty()
				&& !simpleBranches.contains(interestedSimple.peek())) {
			Log.bb(TAG, "Rm CDTAINTBranch " + interestedSimple.pop());
		}

		if (!interestedBiDir.isEmpty()
				&& interestedBiDir.peek().getRestBegin() == inst) {
			Log.bb(TAG, "Rm CDTAINTBranch " + interestedBiDir.pop());
		}

		if (hasRestore && !interestedBiDir.contains(bidirBranches.peek())) {
			interestedBiDir.add(bidirBranches.peek());
			Log.bb(TAG, "Add CDTAINTBranch " + bidirBranches.peek());
		}

		hasRestore = false;
	}

	public TaintCtrlDep() {
		super();
		interestedSimple = new Stack<>();
		interestedBiDir = new Stack<>();

		byteCodes.put(0x08, new CDTAINT_OP_CMP());
	}
}
