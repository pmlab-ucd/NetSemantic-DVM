package fu.hao.trust.analysis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import patdroid.core.MethodInfo;
import patdroid.dalvik.Instruction;
import fu.hao.trust.data.Branch;
import fu.hao.trust.data.Results;
import fu.hao.trust.data.TargetCall;
import fu.hao.trust.dvm.DalvikVM;
import fu.hao.trust.dvm.DalvikVM.Register;
//import fu.hao.trust.dvm.DalvikVM.Register;
import fu.hao.trust.utils.Log;

/**
 * @ClassName: Condition
 * @Description: Extract the context of an API call: dep API and params (entry
 *               point is recored by the static analysis).
 * @author: Hao Fu
 * @date: Mar 9, 2016 3:10:38 PM
 */
public class ContextAnalysis extends TaintAdv {

	final String TAG = getClass().getSimpleName();

	// Store visited target API calls, equivalent to influenced API in
	// InfluenceAnalysis
	// <Target API call loc, Target call>
	Map<Instruction, TargetCall> targetCalls;
	// Whether to record APIs who will be visited and store stop signs.
	// <StopSign, target API call>
	Stack<Branch> interested;
	// Specification of the target APIs
	Set<String> targetList;

	class CTX_OP_INVOKE implements Rule {
		final String TAG = getClass().getName();

		/**
		 * @Title: flow
		 * @Description: The op who creates SensCtx variable, and may includes
		 *               target API calls.
		 * @param vm
		 * @param inst
		 * @param in
		 * @return
		 * @see fu.hao.trust.analysis.Rule#flow(fu.hao.trust.dvm.DalvikVM,
		 *      patdroid.dalvik.Instruction, java.util.Map)
		 */
		@Override
		public Map<Object, Instruction> flow(DalvikVM vm, Instruction inst,
				Map<Object, Instruction> in) {
			TAINT_OP_INVOKE taintOp = new TAINT_OP_INVOKE();
			Map<Object, Instruction> out = taintOp.flow(vm, inst, in);
			Object[] extra = (Object[]) inst.extra;
			MethodInfo mi = (MethodInfo) extra[0];

			if (isTarget(mi.name)) {
				if (!targetCalls.containsKey(inst)) {
					TargetCall targetCall = new TargetCall(inst, vm);
					targetCalls.put(inst, targetCall);
					if (!interested.isEmpty()) {
						for (Branch branch : interested) {
							Log.msg(TAG, "Add depAPI " + branch.getElemSrc());
							targetCall.addDepAPI(branch.getElemSrc());
						}
					} else {
						Log.bb(TAG, "Not API Recording");
					}
					Log.warn(TAG, "Found a target API call:" + targetCall);
				} else {
					TargetCall targetCall = targetCalls.get(inst);
					targetCall.addParams(inst, vm);
				}
			}

			Log.bb(TAG, "Ret value " + vm.getReturnReg().getData());
			return out;
		}
	}

	class CTX_OP_CMP implements Rule {
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

			if (!simpleBranches.isEmpty()) {
				branch = simpleBranches.peek();
			}
			ATAINT_OP_CMP taintOp = new ATAINT_OP_CMP();
			Map<Object, Instruction> out = taintOp.flow(vm, inst, in);

			if (branch != null || branch == null && !simpleBranches.isEmpty()) {
				Register r0 = null, r1 = null;
				if (inst.r0 != -1) {
					r0 = vm.getReg(inst.r0);
				}

				if (inst.r1 != -1) {
					r1 = vm.getReg(inst.r1);
				}

				// When sensitive value exists in the branch
				if ((r0 != null && out.containsKey(r0) || r1 != null
						&& out.containsKey(r1))
						&& simpleBranches.peek() != branch) {
					branch = simpleBranches.peek();
					Log.bb(TAG, "r0 map " + out.get(r0));
					Log.bb(TAG, "r1 map " + out.get(r1));
					branch.setElemSrc(out.get(r0) != null ? out.get(r0) : out
							.get(r1));
					interested.add(branch);

					Log.msg(TAG, "CTX_OP_IF: New CtxBranch " + branch);
				}
			}

			return out;
		}
	}

	private boolean isTarget(String target) {
		if (targetList.contains(target)) {
			return true;
		}

		return false;
	}

	@Override
	public Map<Object, Instruction> runAnalysis(DalvikVM vm, Instruction inst,
			Map<Object, Instruction> in) {
		Map<Object, Instruction> out = super.runAnalysis(vm, inst, in);

		if (!interested.isEmpty() && vm.getAssigned() != null) {
			// Set the assigned var as combined value
			Object[] assigned = vm.getAssigned();
			// FIXME Multiple controlling if.
			out.put((Register) assigned[0], interested.peek().getElemSrc());
		}

		Results.targetCallRes = targetCalls;
		return out;
	}

	@Override
	public void preprocessing(DalvikVM vm, Instruction inst) {
		super.preprocessing(vm, inst);
		if (!interested.isEmpty()
				&& !simpleBranches.contains(interested.peek())) {
			interested.pop();
		}
	}

	public ContextAnalysis() {
		super();
		interested = new Stack<>();
		sinks = new HashSet<>();
		byteCodes.put(0x08, new CTX_OP_CMP());
		byteCodes.put(0x0C, new CTX_OP_INVOKE());

		targetCalls = new HashMap<>();
		targetList = new HashSet<>();

		targetList.add("openConnection");
	}
}
