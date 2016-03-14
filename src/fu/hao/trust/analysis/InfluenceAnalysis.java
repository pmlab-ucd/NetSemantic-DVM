package fu.hao.trust.analysis;

import java.util.HashSet;
import java.util.Set;

import patdroid.core.MethodInfo;
import patdroid.dalvik.Instruction;
import fu.hao.trust.analysis.Taint.TAINT_OP_INVOKE;
import fu.hao.trust.analysis.Taint.TAINT_OP_MOV_RESULT;
import fu.hao.trust.dvm.DalvikVM;
import fu.hao.trust.solver.InfluVar;
import fu.hao.trust.utils.Log;

/**
 * @ClassName: Condition
 * @Description: Extract conditional factors.
 * @author: Hao Fu
 * @date: Mar 9, 2016 3:10:38 PM
 */
public class InfluenceAnalysis extends Taint {
	
	final String TAG = "ForwardAnalysis";

	class COND_OP_IF implements Rule {
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
			

			return out;
		}
	}

	class COND_OP_INVOKE implements Rule {
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

			if (mi.name.contains("equals")) {
				for (int i = 0; i < args.length; i++) {
					if (InfluVar.isCtxVar(vm.getReg(args[i]).getData())) {
						vm.storeState();
						vm.jump(inst, false);
					}
				}
			}

			if (vm.getReturnReg().getData() != null
					&& InfluVar.isCtxVar(vm.getReturnReg().getData())) {
				// If "this" is a CtxVar, add the return val as CtxVar
				try {
					InfluVar var = new InfluVar(vm.getReturnReg().getData());
					out.add(vm.getReturnReg().getData());
					Log.warn(TAG, "Add new influing obj: " + vm.getReturnReg().getData());
				} catch (Exception e) {

				}
			}

			return out;
		}
	}

	class COND_OP_MOV_RESULT implements Rule {

		@Override
		public Set<Object> flow(DalvikVM vm, Instruction inst, Set<Object> in) {
			TAINT_OP_MOV_RESULT taintOp = new TAINT_OP_MOV_RESULT();
			Set<Object> out = taintOp.flow(vm, inst, in);

			return out;
		}

	}

	public InfluenceAnalysis() {
		super();
		sources = new HashSet<>();
		sinks = new HashSet<>();
		byteCodes.put(0x08, new COND_OP_IF());
		byteCodes.put(0x0C, new COND_OP_INVOKE());
		auxByteCodes.put(0x15, new COND_OP_MOV_RESULT());
	}
}
