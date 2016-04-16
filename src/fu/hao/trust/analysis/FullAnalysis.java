package fu.hao.trust.analysis;

import java.util.HashMap;
import java.util.Map;

import patdroid.dalvik.Instruction;
import fu.hao.trust.data.Results;
import fu.hao.trust.data.TargetCall;
import fu.hao.trust.dvm.DalvikVM;

public class FullAnalysis extends TaintCtrlDep {
	
	// Store visited target API calls, equivalent to influenced API in
	// InfluenceAnalysis
	// <Target API call loc, Target call>
	Map<Instruction, TargetCall> targetCalls;
	
	ContextAnalysis ctxAnalysis;
	
	class FULL_OP_INVOKE extends TAINT_OP_INVOKE {
		final String TAG = getClass().getSimpleName();

		@Override
		public Map<String, Map<Object, Instruction>> flow(DalvikVM vm,
				Instruction inst, Map<String, Map<Object, Instruction>> ins) {
			ctxAnalysis.ctxInvoke(vm, inst, targetCalls);
			return super.flow(vm, inst, ins);
		}
	}

	public FullAnalysis() {
		targetCalls = new HashMap<>();
		Results.targetCallRes = targetCalls;
		ctxAnalysis = new ContextAnalysis();
		configs.put(ctxAnalysis.TAG, ctxAnalysis.getConfig());
		
		byteCodes.put(0x0C, new FULL_OP_INVOKE());
	}
	
}
