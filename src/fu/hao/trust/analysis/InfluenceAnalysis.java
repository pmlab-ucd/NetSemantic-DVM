package fu.hao.trust.analysis;

import java.util.HashSet;
import java.util.Map;

import patdroid.core.MethodInfo;
import patdroid.dalvik.Instruction;
import fu.hao.trust.data.Branch;
import fu.hao.trust.data.Results;
import fu.hao.trust.dvm.DalvikVM;
import fu.hao.trust.solver.InfluVar;
import fu.hao.trust.solver.SensCtxVar;
import fu.hao.trust.utils.Log;

/**
 * @ClassName: Condition
 * @Description: Extract the API calls influenced by the target API calls, which
 *               generate influencing variables.
 * @author: Hao Fu
 * @date: Mar 9, 2016 3:10:38 PM
 */
public class InfluenceAnalysis extends TaintCtrlDep {

	final String TAG = getClass().getSimpleName();

	class INFLU_OP_INVOKE implements Rule {
		final String TAG = getClass().getSimpleName();

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

			// Generate influVar
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

			// Avoid conflicts, let influAnalysis to handle vars related to
			// connection.
			if (out.containsKey(vm.getReturnReg())
					&& (vm.getReturnReg().getData() instanceof SensCtxVar)) {
				out.remove(vm.getReturnReg());
			}

			if (out.containsKey(vm.getReturnReg())) {
				Instruction depAPI = null;
				for (Object obj : out.keySet()) {
					if (!in.containsKey(obj)) {
						depAPI = out.get(obj);
						break;
					}
				}

				Log.debug(TAG, "Influencing var detected! " + getMethod() + " "
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

			// Detect influenced API calls
			Log.bb(TAG, "Method " + getMethod());
			if (getMethod() != null && !interestedSimple.isEmpty()) {
				for (Branch branch : interestedSimple) {
					for (Instruction tgtCall : branch.getElemSrcs()) {
						if (Results.targetCallRes != null
								&& Results.targetCallRes.containsKey(tgtCall)) {
							Log.msg(TAG, "tgt " + tgtCall);
							Results.targetCallRes.get(tgtCall)
									.addInfluAPI(inst);
							break;
						}
					}
				}
				Log.warn(TAG, "Found influenced API call " + mi);
			} else {
				Log.msg(TAG, "Not API Recording");
			}

			return out;
		}
	}

	public InfluenceAnalysis() {
		super();
		sources = new HashSet<>();
		sinks = new HashSet<>();
		
		byteCodes.put(0x0C, new INFLU_OP_INVOKE());
	}
}
