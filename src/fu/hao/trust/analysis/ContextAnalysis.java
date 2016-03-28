package fu.hao.trust.analysis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import patdroid.core.MethodInfo;
import patdroid.dalvik.Instruction;
import fu.hao.trust.data.Results;
import fu.hao.trust.data.TargetCall;
import fu.hao.trust.dvm.DalvikVM;
//import fu.hao.trust.dvm.DalvikVM.Register;
import fu.hao.trust.utils.Log;

/**
 * @ClassName: Condition
 * @Description: Extract the context of an API call: dep API and params (entry
 *               point is recored by the static analysis).
 * @author: Hao Fu
 * @date: Mar 9, 2016 3:10:38 PM
 */
public class ContextAnalysis extends TaintAggressive {

	final String TAG = "ContextAnalysis";

	// Store visited target API calls, equivalent to influenced API in
	// InfluenceAnalysis
	// <Target API call loc, Target call>
	Map<Instruction, TargetCall> targetCalls;
	// Whether to record APIs who will be visited and store stop signs.
	// <StopSign, target API call>
	Map<Instruction, Instruction> recordCall;
	boolean retRecordCall = false;
	Instruction stopSign = null;
	// Specification of the target APIs
	Set<String> targetList;

	class CTX_OP_INVOKE implements Rule {
		final String TAG = getClass().toString();

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
					if (!recordCall.isEmpty()) {
						targetCall.setDepAPIs(recordCall.values());
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
			TAINT_OP_IF taintOp = new TAINT_OP_IF();
			Map<Object, Instruction> out = taintOp.flow(vm, inst, in);

			// When sensitive value exists in the branch
			if (out.containsKey(vm.getReg(inst.r0)) || inst.r1 != -1
					&& out.containsKey(vm.getReg(inst.r1))) {
				vm.setPC(vm.getNowPC() + 1);
				stopSign = vm.getCurrStackFrame().getInst((int) inst.extra);
				recordCall.put(stopSign, out.get(vm.getReg(inst.r0)));

				Log.msg(TAG, "CTX_OP_IF: " + stopSign + " " + inst + " "
						+ inst.extra);
			}

			return out;
		}
	}

	class CTX_OP_RETURN_VOID implements Rule {
		final String TAG = getClass().getSimpleName();

		@Override
		public Map<Object, Instruction> flow(DalvikVM vm, Instruction inst,
				Map<Object, Instruction> in) {
			Map<Object, Instruction> out = new HashMap<>(in);
			// If stored bidir conditions are not empty
			if (condition != null) {
				// Register r0 = vm.getReg(condition.r0);

				// if (r0.getData() instanceof SensCtxVar) {
				/*
				 * Map<Instruction, Instruction> copyRec = new HashMap<>();
				 * 
				 * if (!recordCall.isEmpty()) { MethodInfo currtMethod =
				 * vm.getCurrStackFrame().getMethod(); for (Instruction apiCall
				 * : recordCall.values()) {
				 * copyRec.put(currtMethod.insns[currtMethod.insns.length - 1],
				 * apiCall); } }
				 */
				// Reset the recordCall
				recordCall.clear();
				// recordCall.putAll(copyRec);

				// stopSign = vm.getCurrStackFrame().getInst(
				// (int) condition.extra);
				MethodInfo currtMethod = vm.getCurrStackFrame().getMethod();
				stopSign = currtMethod.insns[currtMethod.insns.length - 1];
				// recordCall.put(stopSign,
				// ((SensCtxVar) r0.getData()).getSrc());
				Log.msg(TAG, "API Recording Begin " + stopSign + " "
						+ condition + " " + condition.extra);
				// }
			}

			// Keep the recording to the end of this method
			if (!recordCall.isEmpty()) {
				retRecordCall = true;
			}

			return out;
		}

	}

	class CTX_OP_MOV_CONST implements Rule {
		final String TAG = getClass().toString();

		@Override
		public Map<Object, Instruction> flow(DalvikVM vm, Instruction inst,
				Map<Object, Instruction> in) {
			Map<Object, Instruction> out = new HashMap<>(in);

			if (!recordCall.isEmpty()) {
				// TODO Add only one, but could be influenced by multiple APIs
				out.put(vm.getReg(inst.rdst), recordCall.values().iterator()
						.next());
				for (Instruction met : recordCall.values()) {
					Log.bb(TAG, " dep API " + met);
				}
				Log.warn(TAG, "Found influenced var at " + vm.getReg(inst.rdst));
			} else {
				Log.bb(TAG, "Not API Recording");
			}

			return out;
		}

	}

	class CTX_OP_MOV_REG implements Rule {
		final String TAG = getClass().toString();

		@Override
		public Map<Object, Instruction> flow(DalvikVM vm, Instruction inst,
				Map<Object, Instruction> in) {
			Map<Object, Instruction> out = new HashMap<>(in);

			if (!recordCall.isEmpty()) {
				// TODO Add only one, but could be influenced by multiple APIs
				out.put(vm.getReg(inst.rdst), recordCall.values().iterator()
						.next());
				Log.warn(TAG, "Found influenced var at " + vm.getReg(inst.rdst));
			} else {
				Log.bb(TAG, "Not API Recording");
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
		Map<Object, Instruction> res = super.runAnalysis(vm, inst, in);
		Results.targetCallRes = targetCalls;
		return res;
	}

	public ContextAnalysis() {
		super();
		recordCall = new HashMap<>();
		interested = recordCall.keySet();
		sinks = new HashSet<>();
		byteCodes.put(0x08, new CTX_OP_CMP());
		byteCodes.put(0x0C, new CTX_OP_INVOKE());
		auxByteCodes.put(0x03, new CTX_OP_RETURN_VOID());
		auxByteCodes.put(0x01, new CTX_OP_MOV_REG());
		auxByteCodes.put(0x02, new CTX_OP_MOV_CONST());
		targetCalls = new HashMap<>();
		targetList = new HashSet<>();

		targetList.add("openConnection");
	}
}
