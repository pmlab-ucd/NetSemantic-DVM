package fu.hao.trust.dvm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.management.RuntimeErrorException;

import org.jf.dexlib2.Opcode;
import org.jf.dexlib2.iface.instruction.PayloadInstruction;
import org.jf.dexlib2.iface.instruction.SwitchPayload;

import fu.hao.trust.dvm.DalvikVM.JVM_STACK_FRAME;
import fu.hao.trust.dvm.DalvikVM.simple_dvm_register;
import fu.hao.trust.utils.Log;
import patdroid.core.ClassInfo;
import patdroid.core.FieldInfo;
import patdroid.core.PrimitiveInfo;
import patdroid.dalvik.Instruction;

public class Interpreter {
	private final String TAG = getClass().toString();

	class OP_MOVE_REG implements ByteCode {
		/**
		 * @Title: func
		 * @Description: move vx,vy Moves the content of vy into vx. Do not
		 *               distinguish the range of reg and the type of vx,y
		 * @param vm
		 * @param inst
		 * @see fu.hao.trust.dvm.ByteCode#func(fu.hao.trust.dvm.DalvikVM,
		 *      patdroid.dalvik.Instruction)
		 */
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			if (inst.rdst == -1 || inst.r0 == -1) {
				Log.err(getClass().toString(), "Cannot find rx in the inst");
				return;
			}
			vm.regs[inst.rdst].copy(vm.regs[inst.r1]);
			Log.debug(getClass().toString(), "mov " + inst.r0 + " to "
					+ inst.r1);
			// vm.pc(), vm.pc + 2;
			jump(vm, inst, true);

		}
	}

	class OP_MOV_CONST implements ByteCode {
		/**
		 * @Title: func
		 * @Description: mov rdst, const; const
		 * @param vm
		 * @param inst
		 * @see fu.hao.trust.dvm.ByteCode#func(fu.hao.trust.dvm.DalvikVM,
		 *      patdroid.dalvik.Instruction)
		 */
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			if (inst.type != null) {
				vm.regs[inst.rdst].copy(inst.type, inst.extra);
			} else {
				Log.err(getClass().toString(),
						"Cannot identify target in the inst");
			}

			jump(vm, inst, true);

		}
	}

	class OP_RETURN_VOID implements ByteCode {
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			Log.debug(getClass().toString(), "return void");
			JVM_STACK_FRAME caller = vm.curr_jvm_stack.prev_stack;
			vm.jvm_stack_depth--;
			vm.curr_jvm_stack = caller;
			// this is a trick

		}
	}

	class OP_RETURN_SOMETHING implements ByteCode {
		/**
		 * @Title: func
		 * @Description: return v0 Returns with return value in v0. Used in the
		 *               callee
		 * @param vm
		 * @param inst
		 * @see fu.hao.trust.dvm.ByteCode#func(fu.hao.trust.dvm.DalvikVM,
		 *      patdroid.dalvik.Instruction)
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
		/**
		 * @Title: func
		 * @Description: new-instance v0, java.io.FileInputStream // type@0015
		 *               Instantiates type@0015 (entry #15H in the type table)
		 *               and puts its reference into v0.
		 * @param vm
		 * @param inst
		 * @see fu.hao.trust.dvm.ByteCode#func(fu.hao.trust.dvm.DalvikVM,
		 *      patdroid.dalvik.Instruction)
		 */
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			if (inst.type == null) {
				Log.err(TAG, "cannot create obj of null class");
			}
			vm.regs[inst.rdst].type = inst.type;
			vm.regs[inst.rdst].data = new DVMObject(inst.type);
			Log.debug(TAG, "new object of " + inst.type + "created.");
			jump(vm, inst, true);
		}
	}

	class OP_NEW_ARRAY implements ByteCode {
		/**
		 * @Title: func
		 * @Description: new-array v2, v1, char[] // type@0025 Generates a new
		 *               array of type@0025 type and v1 size and puts the
		 *               reference to the new array into v2.
		 * @param vm
		 * @param inst
		 * @see fu.hao.trust.dvm.ByteCode#func(fu.hao.trust.dvm.DalvikVM,
		 *      patdroid.dalvik.Instruction)
		 */
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			if (inst.type == null) {
				Log.err(TAG, "cannot create array of null class");
			}
			vm.regs[inst.rdst].type = inst.type;
			int count = Integer.parseInt(vm.regs[inst.r0].data.toString());
			
			Object[] newArray;
			if (inst.type.isPrimitive()) {				
				newArray = new PrimitiveInfo[count];				
			} else {
				newArray = new DVMObject[count];
			}
			
			vm.regs[inst.rdst].data = newArray;
			Log.debug(TAG, "a new array of " + inst.type + " in size " + count
					+ "created.");
			jump(vm, inst, true);
		}
	}

	class OP_NEW_FILLED_ARRAY implements ByteCode {
		/**
		 * @Title: func
		 * @Description: filled-new-array {v0,v0},[I // type@0D53 Generates a
		 *               new array of type@0D53. The array's size will be 2 and
		 *               both elements will be filled with the contents of v0
		 *               register.
		 * @param vm
		 * @param inst
		 * @see fu.hao.trust.dvm.ByteCode#func(fu.hao.trust.dvm.DalvikVM,
		 *      patdroid.dalvik.Instruction)
		 */
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			// TODO Auto-generated method stub
			Log.err(TAG, "stub! " + inst);
			jump(vm, inst, true);

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
		/**
		 * @Title: func
		 * @Description: instance-of v0, v4, Test3 // type@0001 Checks whether
		 *               the object reference in v4 is an instance of type@0001
		 *               (entry #1 in the type id table). Sets v0 to non-zero if
		 *               v4 is instance of Test3, 0 otherwise.
		 * @param vm
		 * @param inst
		 * @see fu.hao.trust.dvm.ByteCode#func(fu.hao.trust.dvm.DalvikVM,
		 *      patdroid.dalvik.Instruction)
		 */
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			if (inst.type.equals(vm.regs[inst.rdst])) {
				vm.regs[inst.r0].data = 99;
				Log.debug(TAG, "same type when instanceof");
			} else {
				vm.regs[inst.r0].data = 0;
				Log.debug(TAG, "NOT same type when instanceof");
			}

			jump(vm, inst, true);

		}
	}

	class OP_A_ARRAY_LENGTH implements ByteCode {
		/**
		 * @Title: func
		 * @Description: array-length v1, v1 Calculates the number of elements
		 *               of the array referenced by v1 and puts the result into
		 *               v1.
		 * @param vm
		 * @param inst
		 * @see fu.hao.trust.dvm.ByteCode#func(fu.hao.trust.dvm.DalvikVM,
		 *      patdroid.dalvik.Instruction)
		 */
		@Override
		public void func(DalvikVM vm, Instruction inst) {

		}
	}

	class OP_A_CHECKCAST implements ByteCode {
		/**
		 * @Title: func
		 * @Description: 1F04 0100 - check-cast v4, Test3 // type@0001 Checks
		 *               whether the object reference in v4 can be cast to
		 *               type@0001 (entry #1 in the type id table)
		 * @param vm
		 * @param inst
		 * @see fu.hao.trust.dvm.ByteCode#func(fu.hao.trust.dvm.DalvikVM,
		 *      patdroid.dalvik.Instruction)
		 */
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			if (!inst.type.isConvertibleTo(vm.regs[inst.rdst].type)) {
				Log.err(TAG, "not consistent type when cast!");
			}

			jump(vm, inst, true);

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
		 * @Description: move-result v0 Move the return value of a previous
		 *               method invocation into v0.
		 * @param vm
		 * @param inst
		 * @see fu.hao.trust.dvm.ByteCode#func(fu.hao.trust.dvm.DalvikVM,
		 *      patdroid.dalvik.Instruction)
		 */
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			if (inst.type == null) {
				Log.err(TAG, "cannot identify res type!");
			}
			// type checking before moving?
			vm.regs[vm.curr_jvm_stack.return_val_reg].copy(inst.type,
					inst.extra);
			jump(vm, inst, true);

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
		/**
		 * @Title: func
		 * @Description: if-eq v3, v11, 0080 // +0066 Jumps to the current
		 *               position+66H words if v3==v11. 0080 is the label of the
		 *               target instruction.
		 * @param vm
		 * @param inst
		 * @see fu.hao.trust.dvm.ByteCode#func(fu.hao.trust.dvm.DalvikVM,
		 *      patdroid.dalvik.Instruction)
		 */
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			PrimitiveInfo[] res = OP_CMP(vm, inst, false);
			PrimitiveInfo op1 = res[0];
			PrimitiveInfo op2 = res[1];

			if (op1.equals(op2)) {
				jump(vm, inst, false);

				Log.debug(TAG, "equ: " + inst);
			} else {
				jump(vm, inst, true);

				Log.debug(TAG, "not equ: " + inst);
			}
		}
	}

	class OP_IF_LT implements ByteCode {
		/**
		 * @Title: func
		 * @Description: if-lt v2, v3, 0023 // -0035 Jumps to the current
		 *               position-35H words if v2<v3. 0023 is the label of the
		 *               target instruction.
		 * @param vm
		 * @param inst
		 * @see fu.hao.trust.dvm.ByteCode#func(fu.hao.trust.dvm.DalvikVM,
		 *      patdroid.dalvik.Instruction)
		 */
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			PrimitiveInfo[] res = OP_CMP(vm, inst, false);
			PrimitiveInfo op1 = res[0];
			PrimitiveInfo op2 = res[1];

			if (op1.intValue() < op2.intValue()) {
				jump(vm, inst, false);

				Log.debug(TAG, "less: " + inst);
			} else {
				jump(vm, inst, true);

				Log.debug(TAG, "not lesss: " + inst);
			}
		}
	}

	class OP_IF_NE implements ByteCode {
		/**
		 * @Title: func
		 * @Description: if-ne v3, v10, 002c // +0010 Jumps to the current
		 *               position+10H words if v3!=v10. 002c is the label of the
		 *               target instruction.
		 * @param vm
		 * @param inst
		 * @see fu.hao.trust.dvm.ByteCode#func(fu.hao.trust.dvm.DalvikVM,
		 *      patdroid.dalvik.Instruction)
		 */
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			PrimitiveInfo[] res = OP_CMP(vm, inst, false);
			PrimitiveInfo op1 = res[0];
			PrimitiveInfo op2 = res[1];

			if (op1.intValue() != op2.intValue()) {
				jump(vm, inst, false);

				Log.debug(TAG, "not equ: " + inst);
			} else {
				jump(vm, inst, true);

				Log.debug(TAG, "equ: " + inst);
			}
		}
	}

	class OP_IF_GE implements ByteCode {
		/**
		 * @Title: func
		 * @Description: if-ge v0, v1, 002b // +001b Jumps to the current
		 *               position+1BH words if v0>=v1. 002b is the label of the
		 *               target instruction.
		 * @param vm
		 * @param inst
		 * @see fu.hao.trust.dvm.ByteCode#func(fu.hao.trust.dvm.DalvikVM,
		 *      patdroid.dalvik.Instruction)
		 */
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			PrimitiveInfo[] res = OP_CMP(vm, inst, false);
			PrimitiveInfo op1 = res[0];
			PrimitiveInfo op2 = res[1];

			if (op1.intValue() >= op2.intValue()) {
				jump(vm, inst, false);

				Log.debug(TAG, "ge: " + inst);
			} else {
				jump(vm, inst, true);

				Log.debug(TAG, "not ge: " + inst);
			}

		}
	}

	class OP_IF_GT implements ByteCode {
		/**
		 * @Title: func
		 * @Description: if-ge v0, v1, 002b // +001b Jumps to the current
		 *               position+1BH words if v0>v1. 002b is the label of the
		 *               target instruction.
		 * @param vm
		 * @param inst
		 * @see fu.hao.trust.dvm.ByteCode#func(fu.hao.trust.dvm.DalvikVM,
		 *      patdroid.dalvik.Instruction)
		 */
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			PrimitiveInfo[] res = OP_CMP(vm, inst, false);
			PrimitiveInfo op1 = res[0];
			PrimitiveInfo op2 = res[1];

			if (op1.intValue() > op2.intValue()) {
				jump(vm, inst, false);

				Log.debug(TAG, "g: " + inst);
			} else {
				jump(vm, inst, true);

				Log.debug(TAG, "not g: " + inst);
			}
		}
	}

	class OP_IF_LE implements ByteCode {
		/**
		 * @Title: func
		 * @Description: if-le v6, v5, 0144 // +000b Jumps to the current
		 *               position+0BH words if v6<=v5. 0144 is the label of the
		 *               target instruction.
		 * @param vm
		 * @param inst
		 * @see fu.hao.trust.dvm.ByteCode#func(fu.hao.trust.dvm.DalvikVM,
		 *      patdroid.dalvik.Instruction)
		 */
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			PrimitiveInfo[] res = OP_CMP(vm, inst, false);
			PrimitiveInfo op1 = res[0];
			PrimitiveInfo op2 = res[1];

			if (op1.intValue() <= op2.intValue()) {
				jump(vm, inst, false);

				Log.debug(TAG, "le: " + inst);
			} else {
				jump(vm, inst, true);

				Log.debug(TAG, "not le: " + inst);
			}
		}
	}

	class OP_IF_EQZ implements ByteCode {
		/**
		 * @Title: func
		 * @Description: (·Ç JavaDoc)
		 * @param vm
		 * @param inst
		 * @see fu.hao.trust.dvm.ByteCode#func(fu.hao.trust.dvm.DalvikVM,
		 *      patdroid.dalvik.Instruction)
		 */
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			PrimitiveInfo[] res = OP_CMP(vm, inst, true);
			PrimitiveInfo op1 = res[0];
			PrimitiveInfo op2 = res[1];
			if (op1.equals(op2)) {
				jump(vm, inst, false);
				Log.debug(TAG, "equ: " + inst);
			} else {
				jump(vm, inst, true);
				Log.debug(TAG, "not equ: " + inst);
			}
		}
	}

	class OP_IF_NEZ implements ByteCode {
		/**
		 * @Title: func
		 * @Description: (·Ç JavaDoc)
		 * @param vm
		 * @param inst
		 * @see fu.hao.trust.dvm.ByteCode#func(fu.hao.trust.dvm.DalvikVM,
		 *      patdroid.dalvik.Instruction)
		 */
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			PrimitiveInfo[] res = OP_CMP(vm, inst, true);
			PrimitiveInfo op1 = res[0];
			PrimitiveInfo op2 = res[1];
			if (!op1.equals(op2)) {
				jump(vm, inst, false);
				Log.debug(TAG, "not equ: " + inst);
			} else {
				jump(vm, inst, true);
				Log.debug(TAG, "not equ: " + inst);
			}
		}
	}

	class OP_IF_LTZ implements ByteCode {
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			PrimitiveInfo[] res = OP_CMP(vm, inst, true);
			PrimitiveInfo op1 = res[0];
			PrimitiveInfo op2 = res[1];
			if (op1.intValue() < op2.intValue()) {
				jump(vm, inst, false);
				Log.debug(TAG, "not equ: " + inst);
			} else {
				jump(vm, inst, true);
				Log.debug(TAG, "not equ: " + inst);
			}
		}
	}

	class OP_IF_GTZ implements ByteCode {
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			PrimitiveInfo[] res = OP_CMP(vm, inst, true);
			PrimitiveInfo op1 = res[0];
			PrimitiveInfo op2 = res[1];
			if (op1.intValue() > op2.intValue()) {
				jump(vm, inst, false);
				Log.debug(TAG, "not equ: " + inst);
			} else {
				jump(vm, inst, true);
				Log.debug(TAG, "not equ: " + inst);
			}
		}
	}

	class OP_IF_GEZ implements ByteCode {
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			PrimitiveInfo[] res = OP_CMP(vm, inst, true);
			PrimitiveInfo op1 = res[0];
			PrimitiveInfo op2 = res[1];
			if (op1.intValue() >= op2.intValue()) {
				jump(vm, inst, false);
				Log.debug(TAG, "not equ: " + inst);
			} else {
				jump(vm, inst, true);
				Log.debug(TAG, "not equ: " + inst);
			}
		}
	}

	class OP_IF_LEZ implements ByteCode {
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			PrimitiveInfo[] res = OP_CMP(vm, inst, true);
			PrimitiveInfo op1 = res[0];
			PrimitiveInfo op2 = res[1];
			if (op1.intValue() <= op2.intValue()) {
				jump(vm, inst, false);
				Log.debug(TAG, "not equ: " + inst);
			} else {
				jump(vm, inst, true);
				Log.debug(TAG, "not equ: " + inst);
			}
		}
	}

	class OP_ARRAY_GET implements ByteCode {
		/**
		 * @Title: func
		 * @Description: aget v7, v3, v6 Gets an integer array element. The
		 *               array is referenced by v3 and the element is indexed by
		 *               v6. The element will be put into v7.
		 * @param vm
		 * @param inst
		 * @see fu.hao.trust.dvm.ByteCode#func(fu.hao.trust.dvm.DalvikVM,
		 *      patdroid.dalvik.Instruction)
		 */
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			// dest reg
			simple_dvm_register rdst = vm.regs[inst.rdst];
			// array reg
			Object[] array = (Object[]) vm.regs[inst.r0].data;
			// index reg
			int index = ((PrimitiveInfo) vm.regs[inst.r1].data).intValue();
			
			rdst.type = vm.regs[inst.r0].type.getElementClass();
			rdst.data = array[index];
		}
	}

	class OP_ARRAY_PUT implements ByteCode {
		/**
		 * @Title: func
		 * @Description: aput v0, v3, v5 Puts the integer value in v2 into an
		 *               integer array referenced by v0. The target array
		 *               element is indexed by v1.
		 * @param vm
		 * @param inst
		 * @see fu.hao.trust.dvm.ByteCode#func(fu.hao.trust.dvm.DalvikVM,
		 *      patdroid.dalvik.Instruction)
		 */
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			// dest reg
			simple_dvm_register rdst = vm.regs[inst.rdst];
			// array reg
			Object[] array = (Object[]) vm.regs[inst.r0].data;
			// index reg
			int index = ((PrimitiveInfo) vm.regs[inst.r1].data).intValue();
			if (!rdst.type.isConvertibleTo(vm.regs[inst.r0].type.getElementClass())) {
				Log.err(TAG, "inconsistent type " + inst);
				return;
			}
			array[index] = rdst.data;
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
		/**
		 * @Title: func
		 * @Description: cmpl-float v0, v6, v7 Compares the float values in v6
		 *               and v7 then sets v0 accordingly. NaN bias is less-than,
		 *               the instruction will return -1 if any of the parameters
		 *               is NaN. Compare operations return positive value if the
		 *               first operand is greater than the second operand, 0 if
		 *               they are equal and negative value if the first operand
		 *               is smaller than the second operand.
		 * @param vm
		 * @param inst
		 * @see fu.hao.trust.dvm.ByteCode#func(fu.hao.trust.dvm.DalvikVM,
		 *      patdroid.dalvik.Instruction)
		 */
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			PrimitiveInfo[] res = OP_CMP(vm, inst, true);
			PrimitiveInfo op1 = res[0];
			PrimitiveInfo op2 = res[1];
			simple_dvm_register rdst = vm.regs[inst.rdst];
			rdst.type = ClassInfo.primitiveInt;

			if (op1.longValue() > op2.longValue()) {
				rdst.data = new PrimitiveInfo(1);
			} else if (op1.longValue() == op2.longValue()) {
				rdst.data = new PrimitiveInfo(0);
			} else {
				rdst.data = new PrimitiveInfo(-1);
			}

			jump(vm, inst, true);

			Log.debug(TAG, "CMPLong " + inst);
		}
	}

	class OP_CMP_LESS implements ByteCode {
		/**
		 * @Title: func
		 * @Description: cmpl-float v0, v6, v7 Compares the float values in v6
		 *               and v7 then sets v0 accordingly. NaN bias is less-than,
		 *               the instruction will return -1 if any of the parameters
		 *               is NaN.
		 * @param vm
		 * @param inst
		 * @see fu.hao.trust.dvm.ByteCode#func(fu.hao.trust.dvm.DalvikVM,
		 *      patdroid.dalvik.Instruction)
		 */
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			simple_dvm_register r0 = vm.regs[inst.r0], r1 = vm.regs[inst.r1];
			if (!r0.type.equals(r1.type)) {
				Log.err(TAG, "incosistent type " + inst);
				return;
			}

			PrimitiveInfo op1 = (PrimitiveInfo) vm.regs[inst.r0].data;
			PrimitiveInfo op2 = (PrimitiveInfo) vm.regs[inst.r1].data;
			simple_dvm_register rdst = vm.regs[inst.rdst];
			rdst.type = ClassInfo.primitiveInt;
			// FIXME NaN handling
			if (r0.type.equals(ClassInfo.primitiveFloat)) {
				if (op1.floatValue() > op2.floatValue()) {
					rdst.data = new PrimitiveInfo(1);
				} else if (op1.floatValue() == op2.floatValue()) {
					rdst.data = new PrimitiveInfo(0);
				} else {
					rdst.data = new PrimitiveInfo(-1);
				}
			} else if (r0.type.equals(ClassInfo.primitiveDouble)) {
				if (op1.doubleValue() > op2.doubleValue()) {
					rdst.data = new PrimitiveInfo(1);
				} else if (op1.doubleValue() == op2.doubleValue()) {
					rdst.data = new PrimitiveInfo(0);
				} else {
					rdst.data = new PrimitiveInfo(-1);
				}
			} else {
				Log.err(TAG, "unknown type " + inst);
			}

			jump(vm, inst, true);

		}
	}

	class OP_CMP_GREATER implements ByteCode {
		/**
		 * @Title: func
		 * @Description: cmpg-float v0, v6, v7 Compares the float values in v6
		 *               and v7 then sets v0 accordingly. NaN bias is
		 *               greater-than, the instruction will return 1 if any of
		 *               the parameters is NaN.
		 * @param vm
		 * @param inst
		 * @see fu.hao.trust.dvm.ByteCode#func(fu.hao.trust.dvm.DalvikVM,
		 *      patdroid.dalvik.Instruction)
		 */
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			simple_dvm_register r0 = vm.regs[inst.r0], r1 = vm.regs[inst.r1];
			if (!r0.type.equals(r1.type)) {
				Log.err(TAG, "incosistent type " + inst);
				return;
			}

			PrimitiveInfo op1 = (PrimitiveInfo) vm.regs[inst.r0].data;
			PrimitiveInfo op2 = (PrimitiveInfo) vm.regs[inst.r1].data;
			simple_dvm_register rdst = vm.regs[inst.rdst];
			rdst.type = ClassInfo.primitiveInt;
			// FIXME NaN handling
			if (r0.type.equals(ClassInfo.primitiveFloat)) {
				if (op1.floatValue() > op2.floatValue()) {
					rdst.data = new PrimitiveInfo(1);
				} else if (op1.floatValue() == op2.floatValue()) {
					rdst.data = new PrimitiveInfo(0);
				} else {
					rdst.data = new PrimitiveInfo(-1);
				}
			} else if (r0.type.equals(ClassInfo.primitiveDouble)) {
				if (op1.doubleValue() > op2.doubleValue()) {
					rdst.data = new PrimitiveInfo(1);
				} else if (op1.doubleValue() == op2.doubleValue()) {
					rdst.data = new PrimitiveInfo(0);
				} else {
					rdst.data = new PrimitiveInfo(-1);
				}
			} else {
				Log.err(TAG, "unknown type " + inst);
			}

			jump(vm, inst, true);
		}
	}

	class OP_STATIC_GET_FIELD implements ByteCode {
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			// TODO Auto-generated method stub

		}
	}

	class OP_STATIC_PUT_FIELD implements ByteCode {
		/** 
		 * @Title: func 
		 * @Description: iput v0,v2, Test2.i6:I // field@0002
		 * Stores v0 into field@0002 (entry #2 in the field id table). 
		 * The instance is referenced by v2
		 * @param vm
		 * @param inst 
		 * @see fu.hao.trust.dvm.ByteCode#func(fu.hao.trust.dvm.DalvikVM, patdroid.dalvik.Instruction) 
		 */
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			FieldInfo fieldInfo = (FieldInfo) inst.extra;
			DVMObject obj = (DVMObject) vm.regs[inst.r0].data;
			if (!obj.getType().isConvertibleTo(fieldInfo.getFieldType())) {
				Log.err(TAG, "Type inconsistent! " + inst);
			}
			obj.setField(fieldInfo, vm.regs[inst.r1].data);
		}
	}
	
	class OP_INSTANCE_GET_FIELD implements ByteCode {
		/** 
		 * @Title: func 
		 * @Description: iget v0, v1, Test2.i6:I // field@0003
		 * Reads field@0003 into v0 (entry #3 in the field id table). 
		 * The instance is referenced by v1.
		 * @param vm
		 * @param inst 
		 * @see fu.hao.trust.dvm.ByteCode#func(fu.hao.trust.dvm.DalvikVM, patdroid.dalvik.Instruction) 
		 */
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			FieldInfo fieldInfo = (FieldInfo) inst.extra;
			DVMObject obj = (DVMObject) vm.regs[inst.r0].data;
			if (!obj.getType().isConvertibleTo(fieldInfo.getFieldType())) {
				Log.err(TAG, "Type inconsistent! " + inst);
			}
			vm.regs[inst.r1].type = fieldInfo.getFieldType();
			vm.regs[inst.r1].data = obj.getFieldObj(fieldInfo);
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

	class OP_GOTO implements ByteCode {
		/**
		 * @Title: func
		 * @Description: goto 0005 // -0010 Jumps to current position-16 words
		 *               (hex 10). 0005 is the label of the target instruction.
		 * @param vm
		 * @param inst
		 * @see fu.hao.trust.dvm.ByteCode#func(fu.hao.trust.dvm.DalvikVM,
		 *      patdroid.dalvik.Instruction)
		 */
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			jump(vm, inst, false);
			Log.debug(TAG, "goto done: " + inst);
		}
	}

	class OP_SWITCH implements ByteCode {
		/**
		 * @Title: func
		 * @Description: packed-switch v2, 000c // +000c Execute a packed switch
		 *               according to the switch argument in v2. The position of
		 *               the index table is at current instruction+0CH words.
		 *               The table looks like the following: 0001 // Table type:
		 *               packed switch table 0300 // number of elements 0000
		 *               0000 // element base 0500 0000 0: 00000005 // case 0:
		 *               +00000005 0700 0000 1: 00000007 // case 1: +00000007
		 *               0900 0000 2: 00000009 // case 2: +00000009
		 * @param vm
		 * @param inst
		 * @see fu.hao.trust.dvm.ByteCode#func(fu.hao.trust.dvm.DalvikVM,
		 *      patdroid.dalvik.Instruction)
		 */
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			// extra: Map<Integer, Integer> resolveSwitchTable
			// map.put(key, destIndex);
			if (inst.extra == null) {
				Log.err(TAG, "invalid switch " + inst);
				return;
			}

			// seems <int, int> is enough
			@SuppressWarnings("unchecked")
			Map<Integer, Integer> switchTable = (Map<Integer, Integer>) inst.extra;
			Object data = vm.regs[inst.r0];

			for (int key : switchTable.keySet()) {
				if (data.equals(key)) {
					jump(vm, inst, false);
					return;
				}
			}

			jump(vm, inst, true);
		}
	}

	/**
	 * @Title: func
	 * @Description: helper func for cmp
	 * @param vm
	 * @param inst
	 * @see fu.hao.trust.dvm.ByteCode#func(fu.hao.trust.dvm.DalvikVM,
	 *      patdroid.dalvik.Instruction)
	 */
	private PrimitiveInfo[] OP_CMP(DalvikVM vm, Instruction inst, boolean flagZ) {
		simple_dvm_register r0 = vm.regs[inst.r0], r1 = vm.regs[inst.r1];
		if (!r0.type.equals(r1.type)) {
			Log.err(TAG, "incosistent type " + inst);
			return null;
		}

		PrimitiveInfo op1 = (PrimitiveInfo) vm.regs[inst.r0].data;
		PrimitiveInfo op2;
		if (flagZ) {
			op2 = new PrimitiveInfo(0);
		} else {
			op2 = (PrimitiveInfo) vm.regs[inst.r1].data;
		}
		simple_dvm_register rdst = vm.regs[inst.rdst];
		rdst.type = ClassInfo.primitiveInt;

		PrimitiveInfo[] res = new PrimitiveInfo[2];
		res[0] = op1;
		res[1] = op2;

		return res;
	}

	/**
	 * @Title: jump
	 * @Author: hao
	 * @Description: GOTO
	 * @param @param vm
	 * @param @param inst
	 * @param @param seq
	 * @return void
	 * @throws
	 */
	private void jump(DalvikVM vm, Instruction inst, boolean seq) {
		if (seq) {
			vm.curr_jvm_stack.pc++;
			vm.pc++;
		} else {
			if (inst.extra == null) {
				Log.err(TAG, "unresolve dest address in goto: " + inst);
				return;
			}
			vm.curr_jvm_stack.pc = (int) inst.extra;
			vm.pc = vm.curr_jvm_stack.pc;
		}
	}
	
	private static final Set<ClassInfo> primitives;
	static {
		HashSet<ClassInfo> h = new HashSet<ClassInfo>();
		h.add(ClassInfo.primitiveWide);
		h.add(ClassInfo.primitiveVoid);
		h.add(ClassInfo.primitiveLong);
		h.add(ClassInfo.primitiveBoolean);
		h.add(ClassInfo.primitiveByte);
		h.add(ClassInfo.primitiveInt);
		h.add(ClassInfo.primitiveShort);
		h.add(ClassInfo.primitiveChar);
		h.add(ClassInfo.primitiveDouble);
		h.add(ClassInfo.primitiveFloat);
		primitives = Collections.unmodifiableSet(h);
	}

	static Map<Integer, ByteCode> byteCodes = new HashMap<>();
	static Map<Integer, ByteCode> auxByteCodes = new HashMap<>();

	/**
	 * interpret auxiliary opcodes
	 */
	Interpreter() {

		byteCodes.put(0x06, new OP_GOTO());
		// byteCodes.put(0x07, new OP_CMP());
		// byteCodes.put(0x08, new OP_CMP());
		byteCodes.put(0x0E, new OP_SWITCH());

		//
		auxByteCodes.put(0x01, new OP_MOVE_REG());
		auxByteCodes.put(0x02, new OP_MOV_CONST());
		auxByteCodes.put(0x03, new OP_RETURN_VOID());
		auxByteCodes.put(0x04, new OP_RETURN_SOMETHING());
		auxByteCodes.put(0x05, new OP_MONITOR_ENTER());
		auxByteCodes.put(0x06, new OP_MONITOR_EXIT());
		auxByteCodes.put(0x07, new OP_SP_ARGUMENTS());
		auxByteCodes.put(0x08, new OP_NEW_INSTANCE());
		auxByteCodes.put(0x09, new OP_NEW_ARRAY());
		auxByteCodes.put(0x0A, new OP_NEW_FILLED_ARRAY());
		auxByteCodes.put(0x0B, new OP_INVOKE_DIRECT());
		auxByteCodes.put(0x0C, new OP_INVOKE_SUPER());
		auxByteCodes.put(0x0D, new OP_INVOKE_VIRTUAL());
		auxByteCodes.put(0x0E, new OP_INVOKE_STATIC());
		auxByteCodes.put(0x0F, new OP_INVOKE_INTERFACE());
		auxByteCodes.put(0x10, new OP_A_INSTANCEOF());
		auxByteCodes.put(0x11, new OP_A_ARRAY_LENGTH());
		auxByteCodes.put(0x12, new OP_A_CHECKCAST());
		auxByteCodes.put(0x13, new OP_A_NOT());
		auxByteCodes.put(0x14, new OP_A_NEG());
		auxByteCodes.put(0x15, new OP_MOV_RESULT());
		auxByteCodes.put(0x16, new OP_MOV_EXCEPTION());
		auxByteCodes.put(0x17, new OP_A_CAST());
		auxByteCodes.put(0x18, new OP_IF_EQ());
		auxByteCodes.put(0x19, new OP_IF_NE());
		auxByteCodes.put(0x1A, new OP_IF_LT());
		auxByteCodes.put(0x1B, new OP_IF_GE());
		auxByteCodes.put(0x1C, new OP_IF_GT());
		auxByteCodes.put(0x1D, new OP_IF_LE());
		auxByteCodes.put(0x1E, new OP_IF_EQZ());
		auxByteCodes.put(0x1F, new OP_IF_NEZ());
		auxByteCodes.put(0x20, new OP_IF_LTZ());
		auxByteCodes.put(0x21, new OP_IF_GEZ());
		auxByteCodes.put(0x22, new OP_IF_GTZ());
		auxByteCodes.put(0x23, new OP_IF_LEZ());
		auxByteCodes.put(0x24, new OP_ARRAY_GET());
		auxByteCodes.put(0x25, new OP_ARRAY_PUT());
		auxByteCodes.put(0x26, new OP_A_ADD());
		auxByteCodes.put(0x27, new OP_A_SUB());
		auxByteCodes.put(0x28, new OP_A_MUL());
		auxByteCodes.put(0x29, new OP_A_DIV());
		auxByteCodes.put(0x2A, new OP_A_REM());
		auxByteCodes.put(0x2B, new OP_A_AND());
		auxByteCodes.put(0x2C, new OP_A_OR());
		auxByteCodes.put(0x2D, new OP_A_XOR());
		auxByteCodes.put(0x2E, new OP_A_SHL());
		auxByteCodes.put(0x2F, new OP_A_SHR());
		auxByteCodes.put(0x30, new OP_A_USHR());
		auxByteCodes.put(0x31, new OP_CMP_LONG());
		auxByteCodes.put(0x32, new OP_CMP_LESS());
		auxByteCodes.put(0x33, new OP_CMP_GREATER());
		auxByteCodes.put(0x34, new OP_STATIC_GET_FIELD());
		auxByteCodes.put(0x35, new OP_STATIC_PUT_FIELD());
		auxByteCodes.put(0x36, new OP_INSTANCE_GET_FIELD());
		auxByteCodes.put(0x37, new OP_INSTANCE_PUT_FIELD());
		auxByteCodes.put(0x38, new OP_EXCEPTION_TRYCATCH());
		auxByteCodes.put(0x39, new OP_EXCEPTION_THROW());
	}
}
