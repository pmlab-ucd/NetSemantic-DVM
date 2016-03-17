package fu.hao.trust.analysis;

import java.util.HashSet;
import java.util.Set;

import patdroid.core.MethodInfo;
import patdroid.dalvik.Instruction;
import fu.hao.trust.dvm.DalvikVM;
import fu.hao.trust.dvm.DalvikVM.Register;
import fu.hao.trust.solver.SensCtxVar;
import fu.hao.trust.solver.Unknown;
import fu.hao.trust.utils.Log;

/**
 * @ClassName: Condition
 * @Description: Extract conditional factors.
 * @author: Hao Fu
 * @date: Mar 9, 2016 3:10:38 PM
 */
public class ContextAnalysis extends Taint {

	final String TAG = "ContextAnalysis";

	Set<MethodInfo> influencedAPI;
	boolean recordAPI = false;
	Instruction stopSign = null;
	Set<String> netCalls;

	class CTX_OP_IF implements Rule {
		/**
		 * @Title: func
		 * @Description: Helper func for if
		 * @param vm
		 * @param inst
		 * @see fu.hao.trust.dvm.ByteCode#func(fu.hao.trust.dvm.DalvikVM,
		 *      patdroid.dalvik.Instruction)
		 */
		public Set<Object> flow(DalvikVM vm, Instruction inst, Set<Object> in) {
			TAINT_OP_IF taintOp = new TAINT_OP_IF();
			Set<Object> out = taintOp.flow(vm, inst, in);

			// Sensitive value exists in the branch
			if (out.contains(vm.getReg(inst.r0))) {
				stopSign = vm.getCurrStackFrame().getInst(
						(int) inst.extra);
				interested = stopSign;
				recordAPI = true;
				here = false;
				Log.warn(TAG, "CTX_OP_IF: " + stopSign + " " + inst + " " + inst.extra);
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
		public Set<Object> flow(DalvikVM vm, Instruction inst, Set<Object> in) {
			TAINT_OP_INVOKE taintOp = new TAINT_OP_INVOKE();
			Set<Object> out = taintOp.flow(vm, inst, in);
			Object[] extra = (Object[]) inst.extra;
			MethodInfo mi = (MethodInfo) extra[0];
			int[] args = (int[]) extra[1];

			if (out.size() != in.size()) {
				Log.warn(TAG, "Ctx Generator detected! " + vm.getReturnReg().getData());
				vm.getReturnReg().setData(
						new SensCtxVar(mi.returnType, vm.getReturnReg().getData()));
			}

			if (recordAPI) {
				if (!here) {
					if (isNetCall(mi.name)) {
						influencedAPI.add(mi);
						Log.warn(TAG, "Found influenced API " + mi + " " + vm.getReg(args[0]).getData());
					}
				} else {
					recordAPI = false;
					Log.warn(TAG, "Record API end");
				}
			}

			Log.warn(TAG, "Ret value " + vm.getReturnReg().getData());
			return out;
		}
	}

	class CTX_OP_MOV_RESULT implements Rule {

		@Override
		public Set<Object> flow(DalvikVM vm, Instruction inst, Set<Object> in) {
			TAINT_OP_MOV_RESULT taintOp = new TAINT_OP_MOV_RESULT();
			Set<Object> out = taintOp.flow(vm, inst, in);

			return out;
		}

	}

	class CTX_OP_RETURN_VOID implements Rule {

		@Override
		public Set<Object> flow(DalvikVM vm, Instruction inst, Set<Object> in) {
			Set<Object> out = new HashSet<>(in);

			if (condition != null) {
				Register r0 = vm.getReg(condition.r0);
				// If
				if (r0.getData() instanceof Unknown) {
					stopSign = vm.getCurrStackFrame().getInst(
							(int) condition.extra);
					interested = stopSign;
					recordAPI = true;
					here = false;
					Log.warn(TAG, "Record API Begin " + stopSign + " " + condition + " " + condition.extra);
				}
			}

			return out;
		}

	}
	
	class CTX_OP_MOV_CONST implements Rule {

		@Override
		public Set<Object> flow(DalvikVM vm, Instruction inst, Set<Object> in) {
			Set<Object> out = new HashSet<>(in);

			if (recordAPI) {
				if (!here) {
					out.add(vm.getReg(inst.rdst));
					Log.warn(TAG, "Found influenced var ");
				} else {
					recordAPI = false;
					Log.warn(TAG, "Record API end");
				}
			}

			return out;
		}

	}
	
	class CTX_OP_MOV_REG implements Rule {

		@Override
		public Set<Object> flow(DalvikVM vm, Instruction inst, Set<Object> in) {
			Set<Object> out = new HashSet<>(in);

			if (recordAPI) {
				if (!here) {
					out.add(vm.getReg(inst.rdst));
					Log.warn(TAG, "Found influenced var ");
				} else {
					recordAPI = false;
					Log.warn(TAG, "Record API end");
				}
			}

			return out;
		}

	}
	
	private boolean isNetCall(String target) {
		if (netCalls.contains(target)) {
			return true;
		}
		
		return false;
	}

	public ContextAnalysis() {
		super();
		sinks = new HashSet<>();
		byteCodes.put(0x08, new CTX_OP_IF());
		byteCodes.put(0x0C, new CTX_OP_INVOKE());
		auxByteCodes.put(0x15, new CTX_OP_MOV_RESULT());
		auxByteCodes.put(0x03, new CTX_OP_RETURN_VOID());
		auxByteCodes.put(0x01, new CTX_OP_MOV_REG());
		auxByteCodes.put(0x02, new CTX_OP_MOV_CONST());
		influencedAPI = new HashSet<>();
		netCalls = new HashSet<>();
		
		netCalls.add("openConnection");
		
	}
}
