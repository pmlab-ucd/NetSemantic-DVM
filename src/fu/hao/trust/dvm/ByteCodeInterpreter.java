package fu.hao.trust.dvm;

import java.util.HashMap;
import java.util.Map;

import fu.hao.trust.utils.Log;
import patdroid.core.ClassInfo;
import patdroid.dalvik.Instruction;

public class ByteCodeInterpreter {
	private final String TAG = getClass().toString();
	
	class op_move_reg implements ByteCode {
		/** 
		 * @Title: func 
		 * @Description: move vx,vy
		 * Moves the content of vy into vx. Both registers must be in the first 256 register range.
		 * @param vm
		 * @param inst 
		 * @see fu.hao.trust.dvm.ByteCode#func(fu.hao.trust.dvm.DalvikVM, patdroid.dalvik.Instruction) 
		 */
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			if (inst.r0 == -1) {
				Log.err(TAG, "Cannot find rx in the inst");
				return;
			}
			if (inst.r1 != -1) {
				vm.regs[inst.r0].copy(vm.regs[inst.r1]);
			} else {
				
			}
			// useless now since we know what is the next inst
			// vm.pc = vm.pc + 2; 
		}
	}
	
	static Map<Integer, ByteCode> byteCodes = new HashMap<>();
	
	/**
	 * interpret auxiliary opcodes
	 */
	ByteCodeInterpreter() {
		// 
		byteCodes.put(0x01, new op_move_reg());
		public final static byte OP_MOV_CONST = 0x02;
		public final static byte OP_RETURN_VOID = 0x03;
		public final static byte OP_RETURN_SOMETHING = 0x04;
		public final static byte OP_MONITOR_ENTER = 0x05;
		public final static byte OP_MONITOR_EXIT = 0x06;
		public final static byte OP_SP_ARGUMENTS = 0x07;
		public final static byte OP_NEW_INSTANCE = 0x08;
		public final static byte OP_NEW_ARRAY = 0x09;
		public final static byte OP_NEW_FILLED_ARRAY = 0x0A;
		public final static byte OP_INVOKE_DIRECT = 0x0B;
		public final static byte OP_INVOKE_SUPER = 0x0C;
		public final static byte OP_INVOKE_VIRTUAL = 0x0D;
		public final static byte OP_INVOKE_STATIC = 0x0E;
		public final static byte OP_INVOKE_INTERFACE = 0x0F;
		public final static byte OP_A_INSTANCEOF = 0x10;
		public final static byte OP_A_ARRAY_LENGTH = 0x11;
		public final static byte OP_A_CHECKCAST = 0x12;
		public final static byte OP_A_NOT = 0x13;
		public final static byte OP_A_NEG = 0x14;
		public final static byte OP_MOV_RESULT = 0x15;
		public final static byte OP_MOV_EXCEPTION = 0x16;
		public final static byte OP_A_CAST = 0x17;
		public final static byte OP_IF_EQ = 0x18;
		public final static byte OP_IF_NE = 0x19;
		public final static byte OP_IF_LT = 0x1A;
		public final static byte OP_IF_GE = 0x1B;
		public final static byte OP_IF_GT = 0x1C;
		public final static byte OP_IF_LE = 0x1D;
		public final static byte OP_IF_EQZ = 0x1E;
		public final static byte OP_IF_NEZ = 0x1F;
		public final static byte OP_IF_LTZ = 0x20;
		public final static byte OP_IF_GEZ = 0x21;
		public final static byte OP_IF_GTZ = 0x22;
		public final static byte OP_IF_LEZ = 0x23;
		public final static byte OP_ARRAY_GET = 0x24;
		public final static byte OP_ARRAY_PUT = 0x25;
		public static final byte OP_A_ADD = 0x26;
		public static final byte OP_A_SUB = 0x27;
		public static final byte OP_A_MUL = 0x28;
		public static final byte OP_A_DIV = 0x29;
		public static final byte OP_A_REM = 0x2A;
		public static final byte OP_A_AND = 0x2B;
		public static final byte OP_A_OR = 0x2C;
		public static final byte OP_A_XOR = 0x2D;
		public static final byte OP_A_SHL = 0x2E;
		public static final byte OP_A_SHR = 0x2F;
		public static final byte OP_A_USHR = 0x30;
		public static final byte OP_CMP_LONG = 0x31;
		public static final byte OP_CMP_LESS = 0x32;
		public static final byte OP_CMP_GREATER = 0x33;
		public static final byte OP_STATIC_GET_FIELD = 0x34;
		public static final byte OP_STATIC_PUT_FIELD = 0x35;
		public static final byte OP_INSTANCE_GET_FIELD = 0x36;
		public static final byte OP_INSTANCE_PUT_FIELD = 0x37;
		public static final byte OP_EXCEPTION_TRYCATCH = 0x38;
		public static final byte OP_EXCEPTION_THROW = 0x39;
	}
}
