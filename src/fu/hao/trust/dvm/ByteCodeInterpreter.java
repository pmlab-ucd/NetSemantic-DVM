package fu.hao.trust.dvm;

import java.util.HashMap;
import java.util.Map;

import javax.management.RuntimeErrorException;

import fu.hao.trust.dvm.DalvikVM.JVM_STACK_FRAME;
import fu.hao.trust.utils.Log;
import patdroid.dalvik.Instruction;

public class ByteCodeInterpreter {
	private final String TAG = getClass().toString();
	
	class OP_MOVE_REG implements ByteCode {
		/** 
		 * @Title: func 
		 * @Description: move vx,vy
		 * Moves the content of vy into vx.
		 * Do not distinguish the range of reg and the type of vx,y
		 * @param vm
		 * @param inst 
		 * @see fu.hao.trust.dvm.ByteCode#func(fu.hao.trust.dvm.DalvikVM, patdroid.dalvik.Instruction) 
		 */
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			if (inst.rdst == -1 || inst.r0 == -1) {
				Log.err(getClass().toString(), "Cannot find rx in the inst");
				return;
			}
			vm.regs[inst.rdst].copy(vm.regs[inst.r1]);
			Log.debug(getClass().toString(), "mov " + inst.r0 + " to " + inst.r1);
			// useless now since we know what is the next inst
			// vm.pc(), vm.pc + 2; 
		}
	}
	
	class OP_MOV_CONST implements ByteCode {
		/** 
		 * @Title: func 
		 * @Description: mov rdst, const; const
		 * @param vm
		 * @param inst 
		 * @see fu.hao.trust.dvm.ByteCode#func(fu.hao.trust.dvm.DalvikVM, patdroid.dalvik.Instruction) 
		 */
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			if (inst.type != null){
				vm.regs[inst.rdst].copy(inst.type, inst.extra);
			} else {
				Log.err(getClass().toString(), "Cannot identify target in the inst");
			}
		}
	}
	
	class OP_RETURN_VOID implements ByteCode {
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			Log.debug(getClass().toString(), "return void");
		}
	}
	
	class OP_RETURN_SOMETHING implements ByteCode {
		/** 
		 * @Title: func 
		 * @Description: return v0
		 * Returns with return value in v0.
		 * Used in the callee
		 * @param vm
		 * @param inst 
		 * @see fu.hao.trust.dvm.ByteCode#func(fu.hao.trust.dvm.DalvikVM, patdroid.dalvik.Instruction) 
		 */
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			if (inst.r0 == -1) {
				Log.err(TAG, "return errot");
				return;
			}
			
			// the caller stack of this invocation 
			JVM_STACK_FRAME caller = vm.curr_jvm_stack.prev_stack;
			caller.return_val_reg = inst.r0;
			
			// context switch back to the caller
			vm.jvm_stack_depth--;
			vm.curr_jvm_stack = caller;
			// this is a trick
			vm.pc = vm.curr_jvm_stack.pc;
		}
	}
	
	class OP_MONITOR_ENTER implements ByteCode {
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			// TODO Auto-generated method stub
			
		}
	}
	
	class OP_MONITOR_EXIT implements ByteCode {
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			// TODO Auto-generated method stub
			
		}
	}
	
	class OP_SP_ARGUMENTS implements ByteCode {
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			// TODO Auto-generated method stub
			
		}
	}
	
	class OP_NEW_INSTANCE implements ByteCode {
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			// TODO Auto-generated method stub
			
		}
	}
	
	class OP_NEW_ARRAY implements ByteCode {
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			// TODO Auto-generated method stub
			
		}
	}
	
	class OP_NEW_FILLED_ARRAY implements ByteCode {
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			// TODO Auto-generated method stub
			
		}
	}
	
	class OP_INVOKE_DIRECT implements ByteCode {
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			// TODO Auto-generated method stub
			
		}
	}
	
	class OP_INVOKE_SUPER implements ByteCode {
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			// TODO Auto-generated method stub
			
		}
	}
	
	class OP_INVOKE_VIRTUAL implements ByteCode {
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			// TODO Auto-generated method stub
			
		}
	}
	
	class OP_INVOKE_STATIC implements ByteCode {
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			// TODO Auto-generated method stub
			
		}
	}
	
	class OP_INVOKE_INTERFACE implements ByteCode {
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			// TODO Auto-generated method stub
			
		}
	}
	
	class OP_A_INSTANCEOF implements ByteCode {
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			// TODO Auto-generated method stub
			
		}
	}
	
	class OP_A_ARRAY_LENGTH implements ByteCode {
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			// TODO Auto-generated method stub
			
		}
	}
	
	class OP_A_CHECKCAST implements ByteCode {
		/** 
		 * @Title: func 
		 * @Description: 1F04 0100 - check-cast v4, Test3 // type@0001
		 * Checks whether the object reference in v4 can be cast to type@0001 (entry #1 in the type id table)
		 * @param vm
		 * @param inst 
		 * @see fu.hao.trust.dvm.ByteCode#func(fu.hao.trust.dvm.DalvikVM, patdroid.dalvik.Instruction) 
		 */
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			if (!inst.type.equals(vm.regs[inst.rdst])) {
				Log.err(TAG, "not consistent type when cast!");
			}
			
		}
	}
	
	class OP_A_NOT implements ByteCode {
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			// TODO Auto-generated method stub
			
		}
	}
	
	class OP_A_NEG implements ByteCode {
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			// TODO Auto-generated method stub
			
		}
	}
	
	class OP_MOV_RESULT implements ByteCode {
		/** 
		 * @Title: func 
		 * @Description: move-result v0
		 * Move the return value of a previous method invocation into v0.
		 * @param vm
		 * @param inst 
		 * @see fu.hao.trust.dvm.ByteCode#func(fu.hao.trust.dvm.DalvikVM, patdroid.dalvik.Instruction) 
		 */
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			if (inst.type == null) {
				Log.err(TAG, "cannot identify res type!");
			}
			// TODO type checking before moving 
			vm.regs[vm.curr_jvm_stack.return_val_reg].copy(inst.type, inst.extra);
		}
	}
	
	class OP_MOV_EXCEPTION implements ByteCode {
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			// TODO Auto-generated method stub
			
		}
	}
	
	class OP_A_CAST implements ByteCode {
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			// TODO Auto-generated method stub
			
		}
	}
	
	class OP_IF_EQ implements ByteCode {
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			// TODO Auto-generated method stub
			
		}
	}
	
	class OP_IF_LT implements ByteCode {
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			// TODO Auto-generated method stub
			
		}
	}
	
	class OP_IF_NE implements ByteCode {
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			// TODO Auto-generated method stub
			
		}
	}
	
	class OP_IF_GE implements ByteCode {
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			// TODO Auto-generated method stub
			
		}
	}
	
	class OP_IF_GT implements ByteCode {
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			// TODO Auto-generated method stub
			
		}
	}
	
	class OP_IF_LE implements ByteCode {
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			// TODO Auto-generated method stub
			
		}
	}
	
	class OP_IF_EQZ implements ByteCode {
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			// TODO Auto-generated method stub
			
		}
	}
	
	class OP_IF_NEZ implements ByteCode {
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			// TODO Auto-generated method stub
			
		}
	}
	
	class OP_IF_LTZ implements ByteCode {
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			// TODO Auto-generated method stub
			
		}
	}
	
	class OP_IF_GTZ implements ByteCode {
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			// TODO Auto-generated method stub
			
		}
	}
	
	class OP_IF_GEZ implements ByteCode {
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			// TODO Auto-generated method stub
			
		}
	}
	
	class OP_IF_LEZ implements ByteCode {
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			// TODO Auto-generated method stub
			
		}
	}
	
	class OP_ARRAY_GET implements ByteCode {
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			// TODO Auto-generated method stub
			
		}
	}
	
	class OP_ARRAY_PUT implements ByteCode {
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			// TODO Auto-generated method stub
			
		}
	}
	
	class OP_A_ADD implements ByteCode {
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			// TODO Auto-generated method stub
			
		}
	}
	
	class OP_A_SUB implements ByteCode {
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			// TODO Auto-generated method stub
			
		}
	}
	
	class OP_A_MUL implements ByteCode {
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			// TODO Auto-generated method stub
			
		}
	}
	
	class OP_A_DIV implements ByteCode {
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			// TODO Auto-generated method stub
			
		}
	}
	
	class OP_A_REM implements ByteCode {
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			// TODO Auto-generated method stub
			
		}
	}
	
	class OP_A_AND implements ByteCode {
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			// TODO Auto-generated method stub
			
		}
	}
	
	class OP_A_XOR implements ByteCode {
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			// TODO Auto-generated method stub
			
		}
	}
	
	class OP_A_OR implements ByteCode {
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			// TODO Auto-generated method stub
			
		}
	}
	
	class OP_A_SHL implements ByteCode {
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			// TODO Auto-generated method stub
			
		}
	}
	
	class OP_A_SHR implements ByteCode {
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			// TODO Auto-generated method stub
			
		}
	}
	
	class OP_A_USHR implements ByteCode {
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			// TODO Auto-generated method stub
			
		}
	}
	
	class OP_CMP_LONG implements ByteCode {
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			// TODO Auto-generated method stub
			
		}
	}
	
	class OP_CMP_LESS implements ByteCode {
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			// TODO Auto-generated method stub
			
		}
	}
	
	class OP_STATIC_GET_FIELD implements ByteCode {
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			// TODO Auto-generated method stub
			
		}
	}
	
	class OP_INSTANCE_GET_FIELD implements ByteCode {
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			// TODO Auto-generated method stub
			
		}
	}
	
	class OP_STATIC_PUT_FIELD implements ByteCode {
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			// TODO Auto-generated method stub
			
		}
	}
	
	class OP_CMP_GREATER implements ByteCode {
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			// TODO Auto-generated method stub
			
		}
	}
	
	class OP_INSTANCE_PUT_FIELD implements ByteCode {
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			// TODO Auto-generated method stub
			
		}
	}
	
	class OP_EXCEPTION_TRYCATCH implements ByteCode {
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			// TODO Auto-generated method stub
			
		}
	}
	
	class OP_EXCEPTION_THROW implements ByteCode {
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			// TODO Auto-generated method stub
			
		}
	}
	
	static Map<Integer, ByteCode> byteCodes = new HashMap<>();
	
	/**
	 * interpret auxiliary opcodes
	 */
	ByteCodeInterpreter() {
		// 
		byteCodes.put(0x01, new OP_MOVE_REG());
		byteCodes.put(0x02, new OP_MOV_CONST());
		byteCodes.put(0x03, new OP_RETURN_VOID());
		byteCodes.put(0x04, new OP_RETURN_SOMETHING());
		byteCodes.put(0x05, new OP_MONITOR_ENTER());
		byteCodes.put(0x06, new OP_MONITOR_EXIT());
		byteCodes.put(0x07, new OP_SP_ARGUMENTS());
		byteCodes.put(0x08, new OP_NEW_INSTANCE());
		byteCodes.put(0x09, new OP_NEW_ARRAY());
		byteCodes.put(0x0A, new OP_NEW_FILLED_ARRAY());
		byteCodes.put(0x0B, new OP_INVOKE_DIRECT());
		byteCodes.put(0x0C, new OP_INVOKE_SUPER());
		byteCodes.put(0x0D, new OP_INVOKE_VIRTUAL());
		byteCodes.put(0x0E, new OP_INVOKE_STATIC());
		byteCodes.put(0x0F, new OP_INVOKE_INTERFACE());
		byteCodes.put(0x10, new OP_A_INSTANCEOF());
		byteCodes.put(0x11, new OP_A_ARRAY_LENGTH()) ;
		byteCodes.put(0x12, new OP_A_CHECKCAST());
		byteCodes.put(0x13, new OP_A_NOT());
		byteCodes.put(0x14, new OP_A_NEG());
		byteCodes.put(0x15, new OP_MOV_RESULT());
		byteCodes.put(0x16, new OP_MOV_EXCEPTION());
		byteCodes.put(0x17 ,new OP_A_CAST());
		byteCodes.put(0x18 ,new OP_IF_EQ());
		byteCodes.put(0x19 ,new OP_IF_NE());
		byteCodes.put(0x1A ,new OP_IF_LT());
		byteCodes.put(0x1B ,new OP_IF_GE());
		byteCodes.put(0x1C ,new OP_IF_GT());
		byteCodes.put(0x1D ,new OP_IF_LE());
		byteCodes.put(0x1E ,new OP_IF_EQZ());
		byteCodes.put(0x1F ,new OP_IF_NEZ());
		byteCodes.put(0x20 ,new OP_IF_LTZ());
		byteCodes.put(0x21 ,new OP_IF_GEZ());
		byteCodes.put(0x22 ,new OP_IF_GTZ());
		byteCodes.put(0x23 ,new OP_IF_LEZ());
		byteCodes.put(0x24 ,new OP_ARRAY_GET());
		byteCodes.put(0x25 ,new OP_ARRAY_PUT());
		byteCodes.put(0x26 ,new OP_A_ADD());
		byteCodes.put(0x27 ,new OP_A_SUB());
		byteCodes.put(0x28 ,new OP_A_MUL());
		byteCodes.put(0x29 ,new OP_A_DIV());
		byteCodes.put(0x2A ,new OP_A_REM());
		byteCodes.put(0x2B ,new OP_A_AND());
		byteCodes.put(0x2C ,new OP_A_OR());
		byteCodes.put(0x2D ,new OP_A_XOR());
		byteCodes.put(0x2E ,new OP_A_SHL());
		byteCodes.put(0x2F ,new OP_A_SHR());
		byteCodes.put(0x30 ,new OP_A_USHR());
		byteCodes.put(0x31 ,new OP_CMP_LONG());
		byteCodes.put(0x32 ,new OP_CMP_LESS());
		byteCodes.put(0x33 ,new OP_CMP_GREATER());
		byteCodes.put(0x34 ,new OP_STATIC_GET_FIELD());
		byteCodes.put(0x35 ,new OP_STATIC_PUT_FIELD());
		byteCodes.put(0x36 ,new OP_INSTANCE_GET_FIELD());
		byteCodes.put(0x37 ,new OP_INSTANCE_PUT_FIELD());
		byteCodes.put(0x38 ,new OP_EXCEPTION_TRYCATCH());
		byteCodes.put(0x39 ,new OP_EXCEPTION_THROW());
	}
}
