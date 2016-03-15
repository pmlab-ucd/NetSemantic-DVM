package fu.hao.trust.analysis;

import java.util.HashSet;
import java.util.Set;

import patdroid.core.ClassInfo;
import patdroid.core.MethodInfo;
import patdroid.dalvik.Instruction;
import fu.hao.trust.dvm.DalvikVM;
import fu.hao.trust.dvm.DalvikVM.Register;
import fu.hao.trust.solver.InfluVar;
import fu.hao.trust.solver.Unknown;
import fu.hao.trust.utils.Log;

/**
 * @ClassName: Condition
 * @Description: Extract conditional factors.
 * @author: Hao Fu
 * @date: Mar 9, 2016 3:10:38 PM
 */
public class InfluenceAnalysis extends Taint {
	
	final String TAG = "ForwardAnalysis";
	
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
		public Set<Object> flow(DalvikVM vm, Instruction inst, Set<Object> in) {
			TAINT_OP_IF taintOp = new TAINT_OP_IF();
			Set<Object> out = taintOp.flow(vm, inst, in);
			
			

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
		public Set<Object> flow(DalvikVM vm, Instruction inst, Set<Object> in) {
			TAINT_OP_INVOKE taintOp = new TAINT_OP_INVOKE();
			Set<Object> out = taintOp.flow(vm, inst, in);
			Object[] extra = (Object[]) inst.extra;
			MethodInfo mi = (MethodInfo) extra[0];
			int[] args = (int[]) extra[1];

			if (mi.name.contains("equals")) {
				Log.warn(TAG, "dd found");
				for (int i = 0; i < args.length; i++) {
					if (out.contains(vm.getReg(args[i]).getData())) {
						//vm.storeState();
						//vm.jump(inst, false);
						Log.warn(TAG, "ddd found");
						vm.getReturnReg().setData(new Unknown(ClassInfo.primitiveBoolean));
					}
				}
			}

			if (vm.getReturnReg().getData() != null
					&& InfluVar.isCtxVar(vm.getReturnReg().getData())) {
				// If "this" is a CtxVar, add the return val as CtxVar
				try {
					//InfluVar var = new InfluVar(vm.getReturnReg().getData());
					out.add(vm.getReturnReg().getData());
					Log.warn(TAG, "Add new influing obj: " + vm.getReturnReg().getData());
				} catch (Exception e) {

				}
			}
			
			if (method != null && recordAPI) {
				if (!here) {
					influencedAPI.add(mi);
					Log.warn(TAG, "Found influenced API " + mi);
				} else {
					recordAPI = false;
				}
			}

			return out;
		}
	}

	class INFLU_OP_MOV_RESULT implements Rule {

		@Override
		public Set<Object> flow(DalvikVM vm, Instruction inst, Set<Object> in) {
			TAINT_OP_MOV_RESULT taintOp = new TAINT_OP_MOV_RESULT();
			Set<Object> out = taintOp.flow(vm, inst, in);

			return out;
		}

	}
	
	class INFLU_OP_RETURN_VOID implements Rule {

		@Override
		public Set<Object> flow(DalvikVM vm, Instruction inst, Set<Object> in) {
			Set<Object> out = new HashSet<>(in);
			
			if (condition != null) {
				Register r0 = vm.getReg(condition.r0);
				// If 
				if (r0.getData() instanceof Unknown) {
					stopSign = vm.getCurrStackFrame().getInst((int) condition.extra);
					interested = stopSign;
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
		byteCodes.put(0x08, new INFLU_OP_IF());
		byteCodes.put(0x0C, new INFLU_OP_INVOKE());
		auxByteCodes.put(0x15, new INFLU_OP_MOV_RESULT());
		auxByteCodes.put(0x03, new INFLU_OP_RETURN_VOID());
		influencedAPI = new HashSet<>();
	}
}
