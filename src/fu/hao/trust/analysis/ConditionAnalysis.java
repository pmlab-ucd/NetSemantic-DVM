package fu.hao.trust.analysis;

import java.util.Set;

import patdroid.dalvik.Instruction;
import fu.hao.trust.dvm.DalvikVM;

/**
 * @ClassName: Condition
 * @Description: Extract conditional factors.
 * @author: Hao Fu
 * @date: Mar 9, 2016 3:10:38 PM
 */
public class ConditionAnalysis extends Taint {
	
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
			if (inst.r0 != -1 && in.contains(vm.getReg(inst.r0))) {
				out.add(new CondtionalFactor(vm.getReg(inst.r0).getData()));
			}
			
			if (inst.r1 != -1 && in.contains(vm.getReg(inst.r1))) {
				out.add(new CondtionalFactor(vm.getReg(inst.r0).getData()));
			}

			return out;
		}
	}
	
	
	public ConditionAnalysis() {
		super();
		byteCodes.put(0x08, new COND_OP_IF());
	}
}
