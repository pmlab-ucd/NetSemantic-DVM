package fu.hao.trust.analysis;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import patdroid.core.MethodInfo;
import patdroid.dalvik.Instruction;
import fu.hao.trust.data.Branch;
import fu.hao.trust.data.CorrelatedDataFact;
import fu.hao.trust.data.PluginConfig;
import fu.hao.trust.data.TargetCall;
import fu.hao.trust.dvm.DalvikVM;
import fu.hao.trust.solver.BiDirBranch;
import fu.hao.trust.solver.SensCtxVar;
import fu.hao.trust.utils.Log;

/**
 * @ClassName: Condition
 * @Description: Extract the context of an API call: dep API and params (entry
 *               point is recored by the static analysis).
 * @author: Hao Fu
 * @date: Mar 9, 2016 3:10:38 PM
 */
public class ContextAnalysis {

	final String TAG = getClass().getSimpleName();

	// Specification of the target APIs
	Set<String> targetList;
	private PluginConfig config;

	public void ctxInvoke(DalvikVM vm, Instruction inst, CorrelatedDataFact fact, Map<Instruction, TargetCall> targetCalls)  {
			Object[] extra = (Object[]) inst.extra;
			MethodInfo mi = (MethodInfo) extra[0];
			Stack<Branch> interestedSimple = fact.getInterestedSimple();
			Stack<BiDirBranch> interestedBiDir = fact.getInterestedBiDir();
			
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
			} else if (vm.getReflectMethod() != null && config.getSources().contains(Taint.getSootSignature(mi))) {
				vm.getReturnReg().setData(new SensCtxVar(mi.returnType, vm.getReturnReg().getData(), inst));
				Log.bb(TAG, "Set SensCtxVar at RetReg.");
			}

			Log.bb(TAG, "Ret value " + vm.getReturnReg().getData());
	}

	private boolean isTarget(String target) {
		if (targetList.contains(target)) {
			return true;
		}

		return false;
	}
	
	public PluginConfig getConfig() {
		return config;
	}

	public ContextAnalysis() {
		targetList = new HashSet<>();
		targetList.add("openConnection");
		config = new PluginConfig(TAG, Taint.getDefaultSources(), null);
	}

	public void ctxInvoke(DalvikVM vm, Instruction inst,
			Map<Instruction, TargetCall> targetCalls) {
		ctxInvoke(vm, inst, config.getCorrFact(), targetCalls);
	}

	public CorrelatedDataFact getFact() {
		return config.getCorrFact();
	}

	public void setFact(CorrelatedDataFact fact) {
		config.setCorrFact(fact);
	}
}
