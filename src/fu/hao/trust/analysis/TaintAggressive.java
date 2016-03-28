package fu.hao.trust.analysis;

import java.util.HashMap;
import java.util.Map;

import fu.hao.trust.dvm.DalvikVM;
import fu.hao.trust.solver.BiDirBranch;
import fu.hao.trust.solver.Unknown;
import fu.hao.trust.utils.Log;
import patdroid.dalvik.Instruction;

/**
 * @ClassName: TaintAggresive
 * @Description: The aggressive version of taint analysis.
 * @author: Hao Fu
 * @date: Mar 27, 2016 12:59:39 PM
 */
public class TaintAggressive extends Taint {

	Instruction stopSign = null;

	// Whether add the controlled vars of the branches
	// <StopSign, target API call>
	Map<Instruction, Instruction> addVarsOfBranch;

	class ATAINT_OP_CMP implements Rule {
		final String TAG = getClass().getName();

		@Override
		public Map<Object, Instruction> flow(DalvikVM vm, Instruction inst,
				Map<Object, Instruction> in) {
			TAINT_OP_IF taintOp = new TAINT_OP_IF();
			Map<Object, Instruction> out = taintOp.flow(vm, inst, in);

			// When unknown exists in the branch
			if (inst.r0 != -1 && vm.getReg(inst.r0).getData() instanceof Unknown 
					|| inst.r1 != -1 && vm.getReg(inst.r1).getData() instanceof Unknown) {
				vm.setPC(vm.getNowPC() + 1);
				stopSign = vm.getCurrStackFrame().getInst((int) inst.extra);
				addVarsOfBranch.put(stopSign, out.get(vm.getReg(inst.r0)));
				
				// Scan to check whether the block contains "Return"
				Instruction[] insns = vm.getCurrStackFrame().getMethod().insns;
				for (int i = vm.getPC(); i < (int) inst.extra; i++) {
					if (insns[i].opcode == Instruction.OP_RETURN) {
						BiDirBranch branch = new BiDirBranch(inst, vm.getPC(),
								vm.getCurrStackFrame().getMethod(), vm.storeState());

						vm.addBiDirBranch(branch);
						// Remove just added stop sign since the aggressive is unnecessary and we will come back later
						addVarsOfBranch.remove(stopSign);
						break;
					}
				}

				Log.msg(TAG, "ATAINT_OP_IF: " + stopSign + " " + inst + " "
						+ inst.extra);
			}

			return out;
		}
	}
	
	class ATAINT_OP_MOV_CONST implements Rule {
		final String TAG = getClass().toString();

		@Override
		public Map<Object, Instruction> flow(DalvikVM vm, Instruction inst,
				Map<Object, Instruction> in) {
			Map<Object, Instruction> out = new HashMap<>(in);

			if (!addVarsOfBranch.isEmpty()) {
				// TODO Add only one, but could be influenced by multiple APIs
				out.put(vm.getReg(inst.rdst), addVarsOfBranch.values().iterator()
						.next());
				for (Instruction met : addVarsOfBranch.values()) {
					Log.bb(TAG, " dep API " + met);
				}
				Log.warn(TAG, "Found influenced var at " + vm.getReg(inst.rdst));
			} else {
				Log.bb(TAG, "Not API Recording");
			}

			return out;
		}

	}

	class ATAINT_OP_MOV_REG implements Rule {
		final String TAG = getClass().toString();

		@Override
		public Map<Object, Instruction> flow(DalvikVM vm, Instruction inst,
				Map<Object, Instruction> in) {
			Map<Object, Instruction> out = new HashMap<>(in);

			if (!addVarsOfBranch.isEmpty() && !out.containsKey(vm.getReg(inst.rdst))) {
				// TODO Now only add one, but could be influenced by multiple APIs
				out.put(vm.getReg(inst.rdst), addVarsOfBranch.values().iterator()
						.next());
				Log.warn(TAG, "Found influenced var at " + vm.getReg(inst.rdst));
			} else {
				Log.bb(TAG, "Not API Recording");
			}

			return out;
		}

	}
	
	public TaintAggressive() {
		addVarsOfBranch = new HashMap<>();
		interested = addVarsOfBranch.keySet();
		
		byteCodes.put(0x08, new ATAINT_OP_CMP());
		
		auxByteCodes.put(0x01, new ATAINT_OP_MOV_REG());
		auxByteCodes.put(0x02, new ATAINT_OP_MOV_CONST());
		
	}

}
