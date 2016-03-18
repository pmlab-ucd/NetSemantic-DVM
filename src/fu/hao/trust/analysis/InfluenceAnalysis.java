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
import fu.hao.trust.solver.InfluVar;
import fu.hao.trust.solver.Unknown;
import fu.hao.trust.utils.Log;

/**
 * @ClassName: Condition
 * @Description: Extract the API calls influenced by the target API calls.
 * @author: Hao Fu
 * @date: Mar 9, 2016 3:10:38 PM
 */
public class InfluenceAnalysis extends Taint {

	final String TAG = "InfluenceAnalysis";

	Set<MethodInfo> influencedAPI;
	boolean recordAPI = false;
	Instruction stopSign = null;

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

			return out;
		}
	}

	class INFLU_OP_INVOKE implements Rule {
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
					// InfluVar var = new InfluVar(vm.getReturnReg().getData());
					// FIXME
					out.put(vm.getReturnReg().getData(), null);
					Log.warn(TAG, "Add new influing obj: "
							+ vm.getReturnReg().getData());
				} catch (Exception e) {

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

				Log.debug(TAG, "Influ var detected! " + method + " "
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

			
			if (method != null && recordAPI) {
				if (!interested.isEmpty()) {
					influencedAPI.add(mi);
					Log.warn(TAG, "Found influenced API call " + mi);
				} else {
					recordAPI = false;
				}
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

	class INFLU_OP_RETURN_VOID implements Rule {

		@Override
		public Map<Object, Instruction> flow(DalvikVM vm, Instruction inst,
				Map<Object, Instruction> in) {
			Map<Object, Instruction> out = new HashMap<>(in);

			if (condition != null) {
				Register r0 = vm.getReg(condition.r0);
				// If
				if (r0.getData() instanceof Unknown) {
					stopSign = vm.getCurrStackFrame().getInst(
							(int) condition.extra);
					interested.add(stopSign);
					recordAPI = true;
				}
			}

			return out;
		}

	}

	public InfluenceAnalysis() {
		super();
		sources = new HashSet<>();
		sinks = new HashSet<>();
		interested = new HashSet<>();
		byteCodes.put(0x08, new INFLU_OP_IF());
		byteCodes.put(0x0C, new INFLU_OP_INVOKE());
		auxByteCodes.put(0x15, new INFLU_OP_MOV_RESULT());
		auxByteCodes.put(0x03, new INFLU_OP_RETURN_VOID());
		influencedAPI = new HashSet<>();
	}
}
