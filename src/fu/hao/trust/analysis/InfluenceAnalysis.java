package fu.hao.trust.analysis;

import java.util.HashSet;
import java.util.Map;
import java.util.Stack;

import patdroid.core.MethodInfo;
import patdroid.dalvik.Instruction;
import fu.hao.trust.data.Branch;
import fu.hao.trust.data.Results;
import fu.hao.trust.dvm.DalvikVM;
import fu.hao.trust.dvm.DalvikVM.Register;
import fu.hao.trust.solver.InfluVar;
import fu.hao.trust.solver.SensCtxVar;
import fu.hao.trust.utils.Log;

/**
 * @ClassName: Condition
 * @Description: Extract the API calls influenced by the target API calls, which
 *               generate influencing variables.
 * @author: Hao Fu
 * @date: Mar 9, 2016 3:10:38 PM
 */
public class InfluenceAnalysis extends TaintAdv {

	final String TAG = "InfluenceAnalysis";

	// The interestedSimple branches that have influVar inside the conditions.
	// It is a
	// subset of simpleBranches.
	Stack<Branch> interestedSimple;

	class INFLU_OP_IF implements Rule {
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

			if (!simpleBranches.isEmpty()) {
				branch = simpleBranches.peek();
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
					interestedSimple.add(branch);

					Log.msg(TAG, "New InfluBranch " + branch);
				}
			}

			return out;
		}
	}

	class INFLU_OP_INVOKE implements Rule {
		final String TAG = getClass().toString();

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
			TAINT_OP_INVOKE taintOp = new TAINT_OP_INVOKE();
			Map<Object, Instruction> out = taintOp.flow(vm, inst, in);
			Object[] extra = (Object[]) inst.extra;
			MethodInfo mi = (MethodInfo) extra[0];

			// Generate influVar
			if (vm.getReturnReg().getData() != null
					&& InfluVar.isInfluVar(vm.getReturnReg().getData())) {
				// If "this" is a CtxVar, add the return val as CtxVar
				try {
					// FIXME
					vm.getReturnReg().setData(
							new InfluVar(mi.returnType, vm.getReturnReg()
									.getData(), inst));
					out.put(vm.getReturnReg().getData(), inst);
					Log.warn(TAG, "Add new influencing obj: "
							+ vm.getReturnReg().getData());
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

			// Avoid conflicts, let influAnalysis to handle vars related to
			// connection.
			if (out.containsKey(vm.getReturnReg())
					&& (vm.getReturnReg().getData() instanceof SensCtxVar)) {
				out.remove(vm.getReturnReg());
			}

			if (out.containsKey(vm.getReturnReg())) {
				Instruction depAPI = null;
				for (Object obj : out.keySet()) {
					if (!in.containsKey(obj)) {
						depAPI = out.get(obj);
						break;
					}
				}

				Log.debug(TAG, "Influencing var detected! " + getMethod() + " "
						+ vm.getReturnReg().getData());
				// Set return variable as a bidiVar
				try {
					vm.getReturnReg().setData(
							new InfluVar(mi.returnType, vm.getReturnReg()
									.getData(), depAPI));
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			}

			// Detect influenced API calls
			if (getMethod() != null && !interestedSimple.isEmpty()) {
				for (Branch branch : interestedSimple) {
					for (Instruction tgtCall : branch.getElemSrcs()) {
						if (Results.targetCallRes != null
								&& Results.targetCallRes.containsKey(tgtCall)) {
							Log.msg(TAG, "tgt " + tgtCall);
							Results.targetCallRes.get(tgtCall)
									.addInfluAPI(inst);
							break;
						}
					}
				}
				Log.warn(TAG, "Found influenced API call " + mi);
			} else {
				Log.msg(TAG, "Not API Recording");
			}

			return out;
		}
	}

	@Override
	public void preprocessing(DalvikVM vm, Instruction inst) {
		super.preprocessing(vm, inst);
		if (!interestedSimple.isEmpty()
				&& !simpleBranches.contains(interestedSimple.peek())) {
			interestedSimple.pop();
		}
	}

	@Override
	public Map<Object, Instruction> runAnalysis(DalvikVM vm, Instruction inst,
			Map<Object, Instruction> in) {
		Map<Object, Instruction> out = super.runAnalysis(vm, inst, in);

		// Add ctrl-dep correlated vars
		if (!interestedSimple.isEmpty() && vm.getAssigned() != null) {
			// Set the assigned var as combined value
			Object[] assigned = vm.getAssigned();
			// FIXME Multiple controlling if.
			out.put((Register) assigned[0], interestedSimple.peek()
					.getElemSrcs().iterator().next());
			Log.msg(TAG, "Add correlated tained var " + assigned[0]);
		}

		return out;
	}

	public InfluenceAnalysis() {
		super();
		sources = new HashSet<>();
		sinks = new HashSet<>();
		interestedSimple = new Stack<>();
		byteCodes.put(0x08, new INFLU_OP_IF());
		byteCodes.put(0x0C, new INFLU_OP_INVOKE());
	}
}
