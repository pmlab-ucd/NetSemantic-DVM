package fu.hao.trust.analysis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import patdroid.core.MethodInfo;
import patdroid.dalvik.Instruction;
import fu.hao.trust.data.Branch;
import fu.hao.trust.data.Results;
import fu.hao.trust.data.TargetCall;
import fu.hao.trust.dvm.DalvikVM;
import fu.hao.trust.utils.Log;

/**
 * @ClassName: Condition
 * @Description: Extract the context of an API call: dep API and params (entry
 *               point is recored by the static analysis).
 * @author: Hao Fu
 * @date: Mar 9, 2016 3:10:38 PM
 */
public class ContextAnalysis extends TaintCtrlDep {

	final String TAG = getClass().getSimpleName();

	// Store visited target API calls, equivalent to influenced API in
	// InfluenceAnalysis
	// <Target API call loc, Target call>
	Map<Instruction, TargetCall> targetCalls;
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
					if (!interestedSimple.isEmpty()) {
						for (Branch branch : interestedSimple) {
							Log.msg(TAG, "Add depAPI " + branch.getElemSrcs());
							targetCall.addDepAPIs(branch.getElemSrcs());
						}
					} else if (!interestedBiDir.isEmpty()) {
						for (Branch branch : interestedBiDir) {
							Log.msg(TAG, "Add depAPI " + branch.getElemSrcs());
							targetCall.addDepAPIs(branch.getElemSrcs());
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

		Results.targetCallRes = targetCalls;
		return out;
	}

	public ContextAnalysis() {
		super();
		sinks = new HashSet<>();
		byteCodes.put(0x0C, new CTX_OP_INVOKE());

		targetCalls = new HashMap<>();
		targetList = new HashSet<>();

		targetList.add("openConnection");
	}
}
