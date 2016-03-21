package fu.hao.trust.analysis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import patdroid.core.MethodInfo;
import patdroid.dalvik.Instruction;
import fu.hao.trust.dvm.DalvikVM;
import fu.hao.trust.dvm.DalvikVM.Register;
import fu.hao.trust.solver.InfluVar;
import fu.hao.trust.utils.Log;

/**
 * @ClassName: Condition
 * @Description: Extract the API calls influenced by the target API calls, which
 *               generate influencing variables.
 * @author: Hao Fu
 * @date: Mar 9, 2016 3:10:38 PM
 */
public class InfluenceAnalysis extends Taint {

	final String TAG = "InfluenceAnalysis";

	Set<MethodInfo> influencedCalls;
	// Whether to record APIs who will be visited and store stop signs.
	// <StopSign, target API call>
	Map<Instruction, Instruction> recordCall;
	Instruction stopSign = null;

	class INFLU_OP_RETURN_VOID implements Rule {

		@Override
		public Map<Object, Instruction> flow(DalvikVM vm, Instruction inst,
				Map<Object, Instruction> in) {
			Map<Object, Instruction> out = new HashMap<>(in);

			if (condition != null) {
				Register r0 = vm.getReg(condition.r0);
				Log.msg(TAG, "API Recording Begin " + r0.getData() + " "
						+ condition + " " + condition.extra);
				if (r0.getData() instanceof InfluVar) {
					// Reset the recordCall	
					recordCall.clear();
					stopSign = vm.getCurrStackFrame().getInst(
							(int) condition.extra);
					recordCall
							.put(stopSign, ((InfluVar) r0.getData()).getSrc());
					Log.msg(TAG, "API Recording Begin " + stopSign + " "
							+ condition + " " + condition.extra);
				}
			}

			return out;
		}
	}

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
			TAINT_OP_IF taintOp = new TAINT_OP_IF();
			Map<Object, Instruction> out = taintOp.flow(vm, inst, in);

			// When sensitive value exists in the branch
			if (out.containsKey(vm.getReg(inst.r0))) {
				stopSign = vm.getCurrStackFrame().getInst((int) inst.extra);
				recordCall.put(stopSign, out.get(vm.getReg(inst.r0)));
				
				Log.msg(TAG, "INFLU_OP_IF: " + stopSign + " " + inst + " "
						+ inst.extra);
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

			if (out.containsKey(vm.getReturnReg())) {
				Instruction depAPI = null;
				for (Object obj : out.keySet()) {
					if (!in.containsKey(obj)) {
						depAPI = out.get(obj);
						break;
					}
				}

				Log.debug(TAG, "Influencing var detected! " + method + " "
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

			if (method != null && !recordCall.isEmpty()) {
				influencedCalls.add(mi);
				for (Instruction tgtCall : recordCall.keySet()) {
					if (Results.targetCallRes.containsKey(tgtCall)) {
						Results.targetCallRes.get(tgtCall).addInfluAPI(inst);
					}
				}
				Log.warn(TAG, "Found influenced API call " + mi);
			} else {
				Log.bb(TAG, "Not API Recording");
			}

			return out;
		}
	}

	class INFLU_OP_MOV_RESULT implements Rule {

		@Override
		public Map<Object, Instruction> flow(DalvikVM vm, Instruction inst,
				Map<Object, Instruction> in) {
			TAINT_OP_MOV_RESULT taintOp = new TAINT_OP_MOV_RESULT();
			Map<Object, Instruction> out = taintOp.flow(vm, inst, in);

			return out;
		}
	}

	public InfluenceAnalysis() {
		super();
		sources = new HashSet<>();
		sinks = new HashSet<>();
		recordCall = new HashMap<>();
		interested = recordCall.keySet();
		byteCodes.put(0x08, new INFLU_OP_IF());
		byteCodes.put(0x0C, new INFLU_OP_INVOKE());
		auxByteCodes.put(0x15, new INFLU_OP_MOV_RESULT());
		auxByteCodes.put(0x03, new INFLU_OP_RETURN_VOID());
		influencedCalls = new HashSet<>();
	}
}
