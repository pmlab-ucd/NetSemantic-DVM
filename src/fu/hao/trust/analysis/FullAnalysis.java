package fu.hao.trust.analysis;

import java.util.HashMap;
import java.util.Map;

import android.view.View;
import patdroid.core.MethodInfo;
import patdroid.dalvik.Instruction;
import fu.hao.trust.data.Results;
import fu.hao.trust.data.TargetCall;
import fu.hao.trust.dvm.DalvikVM;
import fu.hao.trust.utils.Log;

public class FullAnalysis extends TaintCtrlDep {
	
	// Store visited target API calls, equivalent to influenced API in
	// InfluenceAnalysis
	// <Target API call loc, Target call>
	Map<Instruction, TargetCall> targetCalls;
	
	ContextAnalysis ctxAnalysis;
	InfluenceAnalysis influAnalysis;
	
	class FULL_OP_INVOKE extends TAINT_OP_INVOKE {
		final String TAG = getClass().getSimpleName();

		@Override
		public Map<String, Map<Object, Instruction>> flow(DalvikVM vm,
				Instruction inst, Map<String, Map<Object, Instruction>> ins) {
			Log.bb(TAG, "targetcalls " + targetCalls);
			Map<String, Map<Object, Instruction>> outs = super.flow(vm, inst, ins);
			ctxAnalysis.ctxInvoke(vm, inst, targetCalls);
			influAnalysis.influInvoke(vm, inst, targetCalls);
			Log.bb(TAG, "Target calls " + Results.targetCallRes);

			Object[] extra = (Object[]) inst.extra;
			MethodInfo mi = (MethodInfo) extra[0];
			if (influAnalysis.isUIAPI(mi.toString())) {
				Log.bb(TAG, "Potential fedView");
				Map<Object, Instruction> out = outs.get(influAnalysis.TAG);
				
				// The register index referred by args
				int[] args = (int[]) extra[1];
				
				for (int i = 1; i < args.length; i++) {
					Instruction src = null;
					if (out.containsKey(vm.getReg(args[i]))) {
						src = out.get(vm.getReg(args[i]));						
					}
					if (vm.getReg(args[i]).isUsed() && out.containsKey(vm.getReg(args[i]).getData())) {
						src = out.get(vm.getReg(args[i]).getData());
					}
					
					if (src != null) {
						if (targetCalls != null
								&& targetCalls.containsKey(src)) {
							Log.msg(TAG, "Target call matched! " + src);
							targetCalls.get(src).addFedView((View) vm.getReg(args[0]).getData());;
							break;
						}
					}
				}
			}
			
			return outs;
		}
	}

	public FullAnalysis() {
		targetCalls = new HashMap<>();
		Results.targetCallRes = targetCalls;
		ctxAnalysis = new ContextAnalysis();
		influAnalysis = new InfluenceAnalysis();
		configs.put(ctxAnalysis.TAG, ctxAnalysis.getConfig());
		configs.put(influAnalysis.TAG, influAnalysis.getConfig());
		
		byteCodes.put(0x0C, new FULL_OP_INVOKE());
	}
	
}
