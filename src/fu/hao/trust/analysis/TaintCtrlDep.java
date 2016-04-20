package fu.hao.trust.analysis;

import java.util.Map;
import java.util.Stack;

import patdroid.core.MethodInfo;
import patdroid.dalvik.Instruction;
import fu.hao.trust.data.Branch;
import fu.hao.trust.data.CorrelatedDataFact;
import fu.hao.trust.data.PluginConfig;
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

	class CDTAINT_OP_CMP extends ATAINT_OP_CMP {
		final String TAG = getClass().getName();

		/**
		 * @Title: func
		 * @Description: Helper func for if
		 * @param vm
		 * @param inst
		 * @see fu.hao.trust.dvm.ByteCode#func(fu.hao.trust.dvm.DalvikVM,
		 *      patdroid.dalvik.Instruction)
		 */
		@Override
		public Map<String, Map<Object, Instruction>> flow(DalvikVM vm,
				Instruction inst, Map<String, Map<Object, Instruction>> ins) {
			Branch branch = null;

			Map<String, Map<Object, Instruction>> outs = super.flow(vm, inst,
					ins);

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

				for (String tag : configs.keySet()) {
					Map<Object, Instruction> out = outs.get(tag);
					Stack<Branch> interestedSimple = configs.get(tag)
							.getCorrFact().getInterestedSimple();
					Stack<BiDirBranch> interestedBiDir = configs.get(tag)
							.getCorrFact().getInterestedBiDir();
					// When sensitive var exists in the branch.
					if (((r0 != null && out.containsKey(r0)) || (r1 != null && out
							.containsKey(r1)))) {
						branch.addElemSrc(out.get(r0) != null ? out.get(r0)
								: out.get(r1));
						if (branch instanceof BiDirBranch
								&& !interestedBiDir.contains(branch)) {
							interestedBiDir.add((BiDirBranch) branch);
						} else if (!interestedSimple.contains(branch)) {
							interestedSimple.add(branch);
						}

						Log.msg(tag, "Add CDTaintBranch " + branch);
					}
				}
			}

			return outs;
		}
	}

	@Override
	public Map<String, Map<Object, Instruction>> runAnalysis(DalvikVM vm,
			Instruction inst, Map<String, Map<Object, Instruction>> ins) {
		Map<String, Map<Object, Instruction>> outs = super.runAnalysis(vm,
				inst, ins);

		if (vm.getAssigned() != null && vm.getAssigned()[0] instanceof Register) {
			Object[] assigned = vm.getAssigned();
			for (String tag : outs.keySet()) {
				Map<Object, Instruction> out = outs.get(tag);
				Stack<Branch> interestedSimple = configs.get(tag).getCorrFact()
						.getInterestedSimple();
				Stack<BiDirBranch> interestedBiDir = configs.get(tag)
						.getCorrFact().getInterestedBiDir();
				// Add ctrl-dep correlated vars
				if (!interestedSimple.isEmpty()
						&& vm.getCurrStackFrame().getMethod() == interestedSimple
								.peek().getMethod()) {
					// Set the assigned var as combined value
					// FIXME Multiple controlling if.
					for (Instruction elemSrc : interestedSimple.peek()
							.getElemSrcs()) {
						Log.warn(TAG, elemSrc);
						Object[] extra = (Object[]) elemSrc.extra;
						MethodInfo mi = (MethodInfo) extra[0];
						PluginConfig config = configs.get(tag);

						if (config.getSources().contains(
								Taint.getSootSignature(mi))) {
							out.put((Register) assigned[0], elemSrc);
						}
					}

					Log.bb(TAG, "a" + interestedSimple.peek().getElemSrcs());
					Log.msg(tag, "Add correlated tained var " + assigned[0]);
				}

				if (!interestedBiDir.isEmpty()
						&& vm.getCurrStackFrame().getMethod() == interestedBiDir
								.peek().getMethod()) {
					// Set the assigned var as combined value
					// FIXME Multiple controlling if.
					for (Instruction elemSrc : interestedBiDir.peek()
							.getElemSrcs()) {
						Log.warn(TAG, elemSrc);
						Object[] extra = (Object[]) elemSrc.extra;
						MethodInfo mi = (MethodInfo) extra[0];
						PluginConfig config = configs.get(tag);

						if (config.getSources().contains(
								Taint.getSootSignature(mi))) {
							out.put((Register) assigned[0], elemSrc);
						}
					}
					Log.bb(TAG, "a" + interestedBiDir.peek().getElemSrcs());
					Log.msg(tag, "Add correlated tained var " + assigned[0]);
				}
			}
		}

		return outs;
	}

	@Override
	public void preprocessing(DalvikVM vm, Instruction inst) {
		super.preprocessing(vm, inst);
		for (PluginConfig config : configs.values()) {
			CorrelatedDataFact corrFact = config.getCorrFact();
			Stack<Branch> interestedSimple = corrFact.getInterestedSimple();
			Stack<BiDirBranch> interestedBiDir = corrFact.getInterestedBiDir();
			if (!interestedSimple.isEmpty()
					&& !simpleBranches.contains(interestedSimple.peek())) {
				Log.bb(config.getTag(), "Rm Simple CDTAINTBranch "
						+ interestedSimple.pop());
			}

			// At RestBegin, check if there exists unexplored block. If yes,
			// stopping proceeding and restore to explore the unexplored blk.
			if (!interestedBiDir.isEmpty()
					&& interestedBiDir.peek().getRestBegin() == inst
					&& !hasRestore) {
				Log.bb(config.getTag(), "Rm Bidir CDTAINTBranch "
						+ interestedBiDir.pop());
			}
		}

		hasRestore = false;
	}

	public TaintCtrlDep() {
		byteCodes.put(0x08, new CDTAINT_OP_CMP());
	}
}
