package fu.hao.trust.analysis;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import patdroid.core.MethodInfo;
import patdroid.dalvik.Instruction;
import fu.hao.trust.dvm.DalvikVM;
import fu.hao.trust.dvm.DalvikVM.Register;
import fu.hao.trust.solver.SensCtxVar;
import fu.hao.trust.utils.Log;

/**
 * @ClassName: Condition
 * @Description: Extract the context of an API call: dep API and params (entry
 *               point is recored by the static analysis).
 * @author: Hao Fu
 * @date: Mar 9, 2016 3:10:38 PM
 */
public class ContextAnalysis extends Taint {

	final String TAG = "ContextAnalysis";

	// Store visited target API calls, equivalent to influenced API in InfluenceAnalysis
	Set<MethodInfo> targetCalls;
	// Whether to record APIs will be visited.
	boolean recordCall;
	//Set<MethodInfo> recordCall;
	Instruction stopSign = null;
	// Specification of the target APIs
	Set<String> targetList;

	class CTX_OP_IF implements Rule {
		/**
		 * @Title: func
		 * @Description: Helper func for if
		 * @param vm
		 * @param inst
		 * @see fu.hao.trust.dvm.ByteCode#func(fu.hao.trust.dvm.DalvikVM,
		 *      patdroid.dalvik.Instruction)
		 */
		public Map<Object, Method> flow(DalvikVM vm, Instruction inst, Map<Object, Method> in) {
			TAINT_OP_IF taintOp = new TAINT_OP_IF();
			Map<Object, Method> out = taintOp.flow(vm, inst, in);

			// Sensitive value exists in the branch
			if (out.containsKey(vm.getReg(inst.r0))) {
				stopSign = vm.getCurrStackFrame().getInst((int) inst.extra);
				interested = stopSign;
				recordCall = true;
				here = false;
				Log.msg(TAG, "CTX_OP_IF: " + stopSign + " " + inst + " "
						+ inst.extra);
			}

			return out;
		}
	}

	class CTX_OP_INVOKE implements Rule {
		/**
		 * @Title: func
		 * @Description: Helper func for if
		 * @param vm
		 * @param inst
		 * @see fu.hao.trust.dvm.ByteCode#func(fu.hao.trust.dvm.DalvikVM,
		 *      patdroid.dalvik.Instruction)
		 */
		public Map<Object, Method> flow(DalvikVM vm, Instruction inst, Map<Object, Method> in) {
			TAINT_OP_INVOKE taintOp = new TAINT_OP_INVOKE();
			Map<Object, Method> out = taintOp.flow(vm, inst, in);
			Object[] extra = (Object[]) inst.extra;
			MethodInfo mi = (MethodInfo) extra[0];
			int[] args = (int[]) extra[1];

			if (out.size() != in.size()) {
				Log.debug(TAG, "Ctx-Generator detected! "
						+ vm.getReturnReg().getData());
				// Set return variable as a bidiVar
				vm.getReturnReg().setData(
						new SensCtxVar(mi.returnType, vm.getReturnReg()
								.getData()));
				if (isSrc) {
					// Record dep API
					
				}
			}

			if (recordCall) {
				if (!here) {
					if (isNetCall(mi.name)) {
						targetCalls.add(mi);
						Log.warn(TAG, "Found influenced API: " + mi
								+ ", its Param: "
								+ vm.getReg(args[0]).getData()
								+ ", it deps on API: ");
					}
				} else {
					recordCall = false;
					Log.msg(TAG, "Record API end");
				}
			}

			Log.warn(TAG, "Ret value " + vm.getReturnReg().getData());
			return out;
		}
	}

	class CTX_OP_MOV_RESULT implements Rule {

		@Override
		public Map<Object, Method> flow(DalvikVM vm, Instruction inst, Map<Object, Method> in) {
			TAINT_OP_MOV_RESULT taintOp = new TAINT_OP_MOV_RESULT();
			Map<Object, Method> out = taintOp.flow(vm, inst, in);

			return out;
		}

	}

	class CTX_OP_RETURN_VOID implements Rule {

		@Override
		public Map<Object, Method> flow(DalvikVM vm, Instruction inst, Map<Object, Method> in) {
			Map<Object, Method> out = new HashMap<>(in);
			// If stored bidir condition is not empty
			if (condition != null) {
				Register r0 = vm.getReg(condition.r0);
				if (r0.getData() instanceof SensCtxVar) {
					stopSign = vm.getCurrStackFrame().getInst(
							(int) condition.extra);
					interested = stopSign;
					recordCall = true;
					here = false;
					Log.msg(TAG, "Record API Begin " + stopSign + " "
							+ condition + " " + condition.extra);
				}
			}

			return out;
		}

	}

	class CTX_OP_MOV_CONST implements Rule {

		@Override
		public Map<Object, Method> flow(DalvikVM vm, Instruction inst, Map<Object, Method> in) {
			Map<Object, Method> out = new HashMap<>(in);

			if (recordCall) {
				if (!here) {
					// FIXME
					out.put(vm.getReg(inst.rdst), null);
					Log.warn(TAG,
							"Found influenced var at " + vm.getReg(inst.rdst));
				} else {
					recordCall = false;
					Log.msg(TAG, "Record API end");
				}
			}

			return out;
		}

	}

	class CTX_OP_MOV_REG implements Rule {

		@Override
		public Map<Object, Method> flow(DalvikVM vm, Instruction inst, Map<Object, Method> in) {
			Map<Object, Method> out = new HashMap<>(in);

			if (recordCall) {
				if (!here) {
					// FIXME
					out.put(vm.getReg(inst.rdst), null);
					Log.warn(TAG,
							"Found influenced var at " + vm.getReg(inst.rdst));
				} else {
					recordCall = false;
					Log.msg(TAG, "Record API end");
				}
			}

			return out;
		}

	}

	private boolean isNetCall(String target) {
		if (targetList.contains(target)) {
			return true;
		}

		return false;
	}

	public ContextAnalysis() {
		super();
		recordCall = false;// new HashSet<>();
		sinks = new HashSet<>();
		byteCodes.put(0x08, new CTX_OP_IF());
		byteCodes.put(0x0C, new CTX_OP_INVOKE());
		auxByteCodes.put(0x15, new CTX_OP_MOV_RESULT());
		auxByteCodes.put(0x03, new CTX_OP_RETURN_VOID());
		auxByteCodes.put(0x01, new CTX_OP_MOV_REG());
		auxByteCodes.put(0x02, new CTX_OP_MOV_CONST());
		targetCalls = new HashSet<>();
		targetList = new HashSet<>();

		targetList.add("openConnection");

	}
}
