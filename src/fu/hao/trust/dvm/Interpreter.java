package fu.hao.trust.dvm;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import fu.hao.trust.dvm.DalvikVM.Register;
import fu.hao.trust.dvm.DalvikVM.StackFrame;
import fu.hao.trust.solver.BiDirVar;
import fu.hao.trust.solver.Unknown;
import fu.hao.trust.utils.Log;
import patdroid.core.ClassInfo;
import patdroid.core.FieldInfo;
import patdroid.core.MethodInfo;
import patdroid.core.PrimitiveInfo;
import patdroid.dalvik.Instruction;
import patdroid.util.Pair;

public class Interpreter {
	boolean running = false;

	// The nested class to implement singleton
	private static class SingletonHolder {
		private static final Interpreter instance = new Interpreter();
	}

	// Get THE instance
	public static final Interpreter v() {
		return SingletonHolder.instance;
	}

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
			vm.getReg(inst.rdst).copy(vm.getReg(inst.r0));
			Log.debug(getClass().toString(), "mov " + vm.getReg(inst.r0).data
					+ " to " + vm.getReg(inst.rdst).data);
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
			Log.debug(TAG, "Mov const begin " + inst);
			vm.getReg(inst.rdst).copy(inst.type, inst.extra);
			Log.debug(
					TAG,
					"data: " + vm.getReg(inst.rdst).data + " "
							+ vm.getReg(inst.rdst).data.getClass());
			jump(vm, inst, true);
		}
	}

	class OP_RETURN_VOID implements ByteCode {
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			Log.debug(getClass().toString(), "Return void");
			vm.backCallCtx(null);
			jump(vm, inst, true);
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
			vm.retValReg.data = vm.getReg(inst.r0).data;
			vm.retValReg.type = ClassInfo
					.findOrCreateClass(vm.getReg(inst.r0).data.getClass());
			Log.debug(TAG, "return data: " + vm.retValReg.data);
			vm.backCallCtx(vm.getReg(inst.r0));
			jump(vm, inst, true);
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
		/**
		 * @Title: func
		 * @Description: Set parameters for the new call based on the given
		 *               arguments.
		 * @param vm
		 * @param inst
		 * @see fu.hao.trust.dvm.ByteCode#func(fu.hao.trust.dvm.DalvikVM,
		 *      patdroid.dalvik.Instruction)
		 */
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			int[] params = (int[]) inst.extra;

			if (vm.getContext() == null) {
				Log.warn(TAG, "null ctx!");
				MethodInfo currMethod = vm.getCurrStackFrame().method;
				int startParam = 0;
				if (!currMethod.isStatic()) {
					startParam = 1;
					vm.getReg(params[0]).data = vm.getCurrStackFrame().thisObj;
					vm.getReg(params[0]).type = currMethod.myClass;
					Log.debug(TAG, "args: " + vm.getReg(params[0]).data);
				}

				int i = startParam;
				for (ClassInfo paramClass : currMethod.paramTypes) {
					if (paramClass.isPrimitive()) {
						// FIXME
						vm.getReg(params[i]).data = new PrimitiveInfo(42);
						vm.getReg(params[i]).type = paramClass;
					} else if (paramClass.isArray()) {
						// FIXME
					} else {
						vm.getReg(params[i]).data = new DVMObject(vm,
								paramClass);
						vm.getReg(params[i]).type = paramClass;
					}
					Log.debug(TAG, "args: " + vm.getReg(params[i]).data);
					i++;
				}
			} else {
				if (params.length != vm.getContext().length) {
					Log.err(TAG, "invalid ctx for invocation!");
					return;
				}

				int i = 0;
				for (Register argReg : vm.getContext()) {
					vm.getReg(params[i]).copy(argReg);
					Log.debug(TAG, "params: " + vm.getReg(params[i]).data);
					i++;
				}
			}
			jump(vm, inst, true);
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

			Log.debug(TAG, "" + vm.getReg(inst.rdst));

			vm.getReg(inst.rdst).type = inst.type;
			try {
				Class.forName(inst.type.toString());
				vm.getReg(inst.rdst).data = null;
			} catch (ClassNotFoundException e) {
				// Do not need to handle reflection type,
				// since <init> invocation will replace the newObj
				Object newObj = new DVMObject(vm, inst.type);
				Log.debug(TAG, "begin new object of " + inst.type + "created.");
				vm.getReg(inst.rdst).data = newObj;
				Log.debug(TAG, "new object of " + inst.type + "created.");
			}
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
			vm.getReg(inst.rdst).type = inst.type;
			int count = Integer.parseInt(vm.getReg(inst.r0).data.toString());

			Object[] newArray;
			if (inst.type.isPrimitive()) {
				newArray = new PrimitiveInfo[count];
			} else {
				newArray = new Object[count];
			}

			vm.getReg(inst.rdst).data = newArray;
			Log.debug(TAG, "a new array of " + inst.type + " in size " + count
					+ " created.");
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
			invocation(vm, inst);
		}
	}

	class OP_INVOKE_SUPER implements ByteCode {
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			invocation(vm, inst);
		}
	}

	class OP_INVOKE_VIRTUAL implements ByteCode {
		/**
		 * @Title: func
		 * @Description: invoke-virtual { v4, v0, v1, v2, v3},
		 *               Test2.method5:(IIII)V // method@0006 Invokes the 6th
		 *               method in the method table with the following
		 *               arguments: v4 is the "this" instance, v0, v1, v2, and
		 *               v3 are the method parameters. The method has 5
		 *               arguments (4 MSB bits of the second byte)5
		 * @param vm
		 * @param inst
		 * @see fu.hao.trust.dvm.ByteCode#func(fu.hao.trust.dvm.DalvikVM,
		 *      patdroid.dalvik.Instruction)
		 */
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			invocation(vm, inst);
		}
	}

	class OP_INVOKE_STATIC implements ByteCode {
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			Object[] extra = (Object[]) inst.extra;
			MethodInfo mi = (MethodInfo) extra[0];
			// The register index referred by args
			int[] args = (int[]) extra[1];
			try {
				// If applicable, directly use reflection to run the method,
				// the method is inside java.lang
				Class<?> clazz = Class.forName(mi.myClass.toString());
				Log.debug(TAG, "reflction " + clazz);
				@SuppressWarnings("rawtypes")
				Class[] argsClass = new Class[mi.paramTypes.length];
				Method method;
				// TODO what is static (no this) but have multiple params
				// static invocation
				if (args.length == 0) {
					method = clazz.getDeclaredMethod(mi.name);
					vm.retValReg.data = method.invoke(null);
				} else {
					Object[] params = new Object[args.length - 1];
					// start from 0 since no "this"
					for (int i = 0; i < args.length; i++) {
						if (vm.getReg(args[i]).data == null) {
							continue;
						}
						if (mi.paramTypes[i].isPrimitive()) {
							Object primitive = resolvePrimitive((PrimitiveInfo) vm
									.getReg(args[i]).data);
							params[i] = primitive;
							Class<?> argClass = primClasses.get(primitive
									.getClass());
							argsClass[i] = argClass;
						} else {
							// TODO use classloader to check exists or not
							String argClass = mi.paramTypes[i].toString();
							argsClass[i] = Class.forName(argClass);
							params[i] = vm.getReg(args[i]).data;
						}
					}
					method = clazz.getDeclaredMethod(mi.name, argsClass);
					vm.retValReg.data = method.invoke(null, params);
				}

				if (vm.retValReg.data != null) {
					vm.retValReg.type = ClassInfo
							.findOrCreateClass(vm.retValReg.data.getClass());
					Log.debug(TAG, "return data: " + vm.retValReg.data + " "
							+ vm.retValReg.data.getClass());
				}
				Log.msg(TAG, "reflction invocation " + method);
				vm.plugin.method = method;
				jump(vm, inst, true);
			} catch (java.lang.ClassNotFoundException e) {
				vm.plugin.method = null;
				invocation(vm, mi, inst, args);
			} catch (Exception e) {
				e.printStackTrace();
				Log.warn(TAG, "error in reflection");
				jump(vm, inst, true);
			}

		}
	}

	class OP_INVOKE_INTERFACE implements ByteCode {
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			invocation(vm, inst);
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
			if (inst.type.equals(vm.getReg(inst.rdst))) {
				vm.getReg(inst.r0).data = 99;
				Log.debug(TAG, "same type when instanceof");
			} else {
				vm.getReg(inst.r0).data = 0;
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
			Object array = vm.getReg(inst.r0).data;
			if (array.getClass().isArray()) {
				vm.getReg(inst.rdst).data = new PrimitiveInfo(
						Array.getLength(array));
				Log.debug(TAG, "len " + Array.getLength(array));
				vm.getReg(inst.rdst).type = ClassInfo.primitiveInt;
			} else {
				Log.err(TAG, "not an array");
			}

			jump(vm, inst, true);
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
			// TODO long-to-int
			if (vm.getReg(inst.rdst).type != null
					&& !inst.type.isConvertibleTo(vm.getReg(inst.rdst).type)) {
				Log.warn(TAG, "not consistent type when cast!");
			}

			jump(vm, inst, true);
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

			if (!(vm.retValReg.data instanceof BiDirVar) && vm.retValReg.type != null && vm.retValReg.type.isPrimitive()) {
				vm.retValReg.data = PrimitiveInfo.fromObject(vm.retValReg.data);
			}

			// type checking before moving?
			vm.getReg(inst.rdst).copy(vm.retValReg);
			Log.bb(
					TAG,
					"Result data: " + vm.getReturnReg().getData());
			Log.bb(
					TAG,
					"Result data: " + vm.getReg(inst.rdst).data + ", type: "
							+ vm.getReg(inst.rdst).type + ", to "
							+ vm.getReg(inst.rdst));
			jump(vm, inst, true);
		}
	}

	class OP_MOV_EXCEPTION implements ByteCode {
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			vm.getReg(inst.rdst).copy(vm.getCurrStackFrame().exceptReg);
			jump(vm, inst, true);
		}
	}

	class OP_A_CAST implements ByteCode {
		/**
		 * @Title: func
		 * @Description: int-to-long v6, v0 Converts an integer in v0 into a
		 *               long in v6,v7.
		 * @param vm
		 * @param inst
		 * @see fu.hao.trust.dvm.ByteCode#func(fu.hao.trust.dvm.DalvikVM,
		 *      patdroid.dalvik.Instruction)
		 */
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			Log.debug(TAG, "cast data " + vm.getReg(inst.r0).data);
			if (!(vm.getReg(inst.r0).data instanceof PrimitiveInfo)) {
				vm.getReg(inst.r0).data = PrimitiveInfo.fromObject(vm
						.getReg(inst.r0).data);
			}
			PrimitiveInfo primitive = (PrimitiveInfo) vm.getReg(inst.r0).data;
			vm.getReg(inst.rdst).data = primitive.castTo(inst.type);
			vm.getReg(inst.rdst).type = inst.type;
			jump(vm, inst, true);
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
				Log.debug(TAG, "not equ: " + inst + " " + op1.intValue() + " "
						+ op2.intValue());
			} else {
				jump(vm, inst, true);
				Log.debug(TAG, "equ: " + inst + " " + op1.intValue() + " "
						+ op2.intValue());
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
		 * @Description: (œÊ JavaDoc)
		 * @param vm
		 * @param inst
		 * @see fu.hao.trust.dvm.ByteCode#func(fu.hao.trust.dvm.DalvikVM,
		 *      patdroid.dalvik.Instruction)
		 */
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			if (vm.getReg(inst.r0) != null
					&& !vm.getReg(inst.r0).type.isPrimitive()) {
				jump(vm, inst, true);
				return;
			} else if (vm.getReg(inst.r0) == null) {
				jump(vm, inst, false);
				return;
			}

			PrimitiveInfo[] res = OP_CMP(vm, inst, true);
			PrimitiveInfo op1 = res[0];
			PrimitiveInfo op2 = res[1];
			if (op1.isBoolean() && op1.booleanValue() == false
					|| !op1.isBoolean() && op1.equals(op2)) {
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
		 * @Description: (œÊ JavaDoc)
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
			if (op1.isBoolean() && op1.booleanValue() == true
					|| !op1.isBoolean() && !op1.equals(op2)) {
				jump(vm, inst, false);
				Log.debug(TAG, "Not equ: " + op1);
			} else {
				jump(vm, inst, true);
				Log.debug(TAG, "Equ: " + inst);
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
			Object array = vm.getReg(inst.r0).data;
			if (array.getClass().isArray()) {
				// dest reg
				Register rdst = vm.getReg(inst.rdst);
				// array reg
				// Object[] array = (Object[]) vm.getReg(inst.r0).data;
				// index reg
				int index = ((PrimitiveInfo) vm.getReg(inst.r1).data)
						.intValue();

				rdst.type = vm.getReg(inst.r0).type.getElementClass();
				Object element = Array.get(array, index);
				// if (element.getClass().isPrimitive()) {
				Log.debug(TAG, "elem: " + element + " at " + index + " of "
						+ array);
				if (rdst.type.isPrimitive()) {
					rdst.data = PrimitiveInfo.fromObject(element);
				} else {
					rdst.data = Array.get(vm.getReg(inst.r0).data, index);// array[index];
				}
			}
			jump(vm, inst, true);
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
			Register rdst = vm.getReg(inst.rdst);
			// array reg
			Object[] array = (Object[]) vm.getReg(inst.r0).data;
			// index reg
			int index = ((PrimitiveInfo) vm.getReg(inst.r1).data).intValue();
			if (!rdst.type.isConvertibleTo(vm.getReg(inst.r0).type
					.getElementClass())) {
				Log.err(TAG, "inconsistent type " + inst);
				return;
			}
			Log.debug(TAG, "data: " + rdst.data + " array: " + array);
			array[index] = rdst.data;
			jump(vm, inst, true);
		}
	}

	class OP_A_ADD implements ByteCode {
		/**
		 * @Title: func
		 * @Description: add-int/2addr v0,v1 Adds v1 to v0.
		 * @param vm
		 * @param inst
		 * @see fu.hao.trust.dvm.ByteCode#func(fu.hao.trust.dvm.DalvikVM,
		 *      patdroid.dalvik.Instruction)
		 */
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			Log.debug(TAG, "data " + vm.getReg(inst.r0).data);
			if (vm.getReg(inst.r0).data instanceof Unknown) {
				Log.warn(TAG, "Unknown primitive data found.");
				jump(vm, inst, true);
				return;
			}
			PrimitiveInfo op0 = (PrimitiveInfo) vm.getReg(inst.r0).data;
			PrimitiveInfo op1;
			if (inst.r1 != -1) {
				op1 = (PrimitiveInfo) vm.getReg(inst.r1).data;
			} else {
				op1 = (PrimitiveInfo) inst.extra;
			}

			if (op0 == null || op1 == null) {
				Log.warn(TAG, "Unknown primitive data found.");
				jump(vm, inst, true);
				return;
			}
			Register rdst = vm.getReg(inst.rdst);
			if (op1.isInteger()) {
				rdst.type = ClassInfo.primitiveInt;
				int res = op0.intValue() + op1.intValue();
				rdst.data = new PrimitiveInfo(res);
			} else if (op1.isLong()) {
				rdst.type = ClassInfo.primitiveLong;
				long res = op0.longValue() + op1.longValue();
				rdst.data = new PrimitiveInfo(res);
			} else if (op1.isFloat()) {
				rdst.type = ClassInfo.primitiveFloat;
				float res = op0.floatValue() + op1.floatValue();
				rdst.data = new PrimitiveInfo(res);
			} else if (op1.isDouble()) {
				rdst.type = ClassInfo.primitiveFloat;
				double res = op0.floatValue() + op1.floatValue();
				rdst.data = new PrimitiveInfo(res);
			}
			jump(vm, inst, true);
		}
	}

	class OP_A_SUB implements ByteCode {
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			PrimitiveInfo op0 = (PrimitiveInfo) vm.getReg(inst.r0).data;
			PrimitiveInfo op1;
			if (inst.r1 != -1) {
				op1 = (PrimitiveInfo) vm.getReg(inst.r1).data;
			} else {
				op1 = (PrimitiveInfo) inst.extra;
			}
			Register rdst = vm.getReg(inst.rdst);
			if (op1.isInteger()) {
				rdst.type = ClassInfo.primitiveInt;
				int res = op0.intValue() - op1.intValue();
				rdst.data = new PrimitiveInfo(res);
			} else if (op1.isLong()) {
				rdst.type = ClassInfo.primitiveLong;
				long res = op0.longValue() - op1.longValue();
				rdst.data = new PrimitiveInfo(res);
			} else if (op1.isFloat()) {
				rdst.type = ClassInfo.primitiveFloat;
				float res = op0.floatValue() - op1.floatValue();
				rdst.data = new PrimitiveInfo(res);
			} else if (op1.isDouble()) {
				rdst.type = ClassInfo.primitiveFloat;
				double res = op0.floatValue() - op1.floatValue();
				rdst.data = new PrimitiveInfo(res);
			}
			jump(vm, inst, true);
		}
	}

	class OP_A_MUL implements ByteCode {
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			PrimitiveInfo op0 = (PrimitiveInfo) vm.getReg(inst.r0).data;
			PrimitiveInfo op1;
			if (inst.r1 != -1) {
				op1 = (PrimitiveInfo) vm.getReg(inst.r1).data;
			} else {
				op1 = (PrimitiveInfo) inst.extra;
			}
			Register rdst = vm.getReg(inst.rdst);

			Object obj = resolvePrimitive(op1);
			Log.debug(TAG, "" + obj.getClass());
			if (op1.isInteger()) {
				rdst.type = ClassInfo.primitiveInt;
				int res = op0.intValue() * op1.intValue();
				rdst.data = new PrimitiveInfo(res);
			} else if (op1.isLong()) {
				Log.debug(TAG, "here mul long ");
				rdst.type = ClassInfo.primitiveLong;
				double res = op0.longValue() * op1.longValue();
				Log.debug(TAG, "" + res);
				rdst.data = new PrimitiveInfo(op0.longValue() * op1.longValue());
			} else if (op1.isFloat()) {
				rdst.type = ClassInfo.primitiveFloat;
				float res = op0.floatValue() * op1.floatValue();
				rdst.data = new PrimitiveInfo(res);
			} else if (op1.isDouble()) {
				Log.debug(TAG, "here mul ");
				rdst.type = ClassInfo.primitiveFloat;
				double res = op0.floatValue() * op1.floatValue();
				rdst.data = new PrimitiveInfo(res);
			}
			Log.debug(TAG, "end mul ");
			jump(vm, inst, true);
		}
	}

	class OP_A_DIV implements ByteCode {
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			PrimitiveInfo op0 = (PrimitiveInfo) vm.getReg(inst.r0).data;
			PrimitiveInfo op1;
			if (inst.r1 != -1) {
				op1 = (PrimitiveInfo) vm.getReg(inst.r1).data;
			} else {
				op1 = (PrimitiveInfo) inst.extra;
			}
			Register rdst = vm.getReg(inst.rdst);
			if (op1.isInteger()) {
				rdst.type = ClassInfo.primitiveInt;
				int res = op0.intValue() / op1.intValue();
				rdst.data = new PrimitiveInfo(res);
			} else if (op1.isLong()) {
				rdst.type = ClassInfo.primitiveLong;
				long res = op0.longValue() / op1.longValue();
				rdst.data = new PrimitiveInfo(res);
			} else if (op1.isFloat()) {
				rdst.type = ClassInfo.primitiveFloat;
				float res = op0.floatValue() / op1.floatValue();
				rdst.data = new PrimitiveInfo(res);
			} else if (op1.isDouble()) {
				rdst.type = ClassInfo.primitiveFloat;
				double res = op0.floatValue() / op1.floatValue();
				rdst.data = new PrimitiveInfo(res);
			}
			jump(vm, inst, true);
		}
	}

	class OP_A_REM implements ByteCode {
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			try {
				PrimitiveInfo op0 = (PrimitiveInfo) vm.getReg(inst.r0).data;
				PrimitiveInfo op1;
				if (inst.r1 != -1) {
					op1 = (PrimitiveInfo) vm.getReg(inst.r1).data;
				} else {
					op1 = (PrimitiveInfo) inst.extra;
				}
				Register rdst = vm.getReg(inst.rdst);

				if (op1.isInteger()) {
					rdst.type = ClassInfo.primitiveInt;
					int res = op0.intValue() % op1.intValue();
					rdst.data = new PrimitiveInfo(res);
				} else if (op1.isLong()) {
					rdst.type = ClassInfo.primitiveLong;
					long res = op0.longValue() % op1.longValue();
					rdst.data = new PrimitiveInfo(res);
				} else if (op1.isFloat()) {
					rdst.type = ClassInfo.primitiveFloat;
					float res = op0.floatValue() % op1.floatValue();
					rdst.data = new PrimitiveInfo(res);
				} else if (op1.isDouble()) {
					rdst.type = ClassInfo.primitiveFloat;
					double res = op0.floatValue() % op1.floatValue();
					rdst.data = new PrimitiveInfo(res);
				}

			} catch (java.lang.ClassCastException e) {
				Unknown op = null;
				if (vm.getReg(inst.r0).data instanceof Unknown) {
					op = (Unknown) vm.getReg(inst.r0).data;
					op.addLastArith(inst);
				}

				if (inst.r1 != -1 && vm.getReg(inst.r1).data instanceof Unknown) {
					op = (Unknown) vm.getReg(inst.r0).data;
					op.addLastArith(inst);
				}

				Log.warn(TAG, "unknown found! " + op);
			}
			jump(vm, inst, true);
		}
	}

	class OP_A_AND implements ByteCode {
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			PrimitiveInfo op0 = (PrimitiveInfo) vm.getReg(inst.r0).data;
			PrimitiveInfo op1;
			if (inst.r1 != -1) {
				op1 = (PrimitiveInfo) vm.getReg(inst.r1).data;
			} else {
				op1 = (PrimitiveInfo) inst.extra;
			}
			Register rdst = vm.getReg(inst.rdst);
			if (op1.isInteger()) {
				rdst.type = ClassInfo.primitiveInt;
				int res = op0.intValue() & op1.intValue();
				rdst.data = new PrimitiveInfo(res);
			} else if (op1.isLong()) {
				rdst.type = ClassInfo.primitiveLong;
				long res = op0.longValue() & op1.longValue();
				rdst.data = new PrimitiveInfo(res);
			} else {
				Log.err(TAG, "invalid type! " + inst);
			}
			jump(vm, inst, true);
		}
	}

	class OP_A_NOT implements ByteCode {
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			// FIXME not sure correct
			PrimitiveInfo op0 = (PrimitiveInfo) vm.getReg(inst.r0).data;
			Register rdst = vm.getReg(inst.rdst);
			if (op0.isInteger()) {
				rdst.type = ClassInfo.primitiveInt;
				int res = ~op0.intValue();
				rdst.data = new PrimitiveInfo(res);
			} else if (op0.isLong()) {
				rdst.type = ClassInfo.primitiveLong;
				long res = ~op0.longValue();
				rdst.data = new PrimitiveInfo(res);
			}
			jump(vm, inst, true);
		}
	}

	class OP_A_NEG implements ByteCode {
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			PrimitiveInfo op0 = (PrimitiveInfo) vm.getReg(inst.r0).data;
			Register rdst = vm.getReg(inst.rdst);
			if (op0.isInteger()) {
				rdst.type = ClassInfo.primitiveInt;
				int res = -op0.intValue();
				rdst.data = new PrimitiveInfo(res);
			} else if (op0.isLong()) {
				rdst.type = ClassInfo.primitiveLong;
				long res = -op0.longValue();
				rdst.data = new PrimitiveInfo(res);
			} else if (op0.isFloat()) {
				rdst.type = ClassInfo.primitiveFloat;
				float res = -op0.floatValue();
				rdst.data = new PrimitiveInfo(res);
			} else if (op0.isDouble()) {
				rdst.type = ClassInfo.primitiveFloat;
				double res = -op0.floatValue();
				rdst.data = new PrimitiveInfo(res);
			}
			jump(vm, inst, true);
		}
	}

	class OP_A_XOR implements ByteCode {
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			PrimitiveInfo op0 = (PrimitiveInfo) vm.getReg(inst.r0).data;
			PrimitiveInfo op1;
			if (inst.r1 != -1) {
				op1 = (PrimitiveInfo) vm.getReg(inst.r1).data;
			} else {
				op1 = (PrimitiveInfo) inst.extra;
			}
			Register rdst = vm.getReg(inst.rdst);
			if (op1.isInteger()) {
				rdst.type = ClassInfo.primitiveInt;
				int res = op0.intValue() ^ op1.intValue();
				rdst.data = new PrimitiveInfo(res);
			} else if (op1.isLong()) {
				rdst.type = ClassInfo.primitiveLong;
				long res = op0.longValue() ^ op1.longValue();
				rdst.data = new PrimitiveInfo(res);
			}
			jump(vm, inst, true);
		}
	}

	class OP_A_OR implements ByteCode {
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			PrimitiveInfo op0 = (PrimitiveInfo) vm.getReg(inst.r0).data;
			PrimitiveInfo op1;
			if (inst.r1 != -1) {
				op1 = (PrimitiveInfo) vm.getReg(inst.r1).data;
			} else {
				op1 = (PrimitiveInfo) inst.extra;
			}
			Register rdst = vm.getReg(inst.rdst);
			if (op1.isInteger()) {
				rdst.type = ClassInfo.primitiveInt;
				int res = op0.intValue() | op1.intValue();
				rdst.data = new PrimitiveInfo(res);
			} else if (op1.isLong()) {
				rdst.type = ClassInfo.primitiveLong;
				long res = op0.longValue() | op1.longValue();
				rdst.data = new PrimitiveInfo(res);
			}
			jump(vm, inst, true);
		}
	}

	class OP_A_SHL implements ByteCode {
		/**
		 * @Title: func
		 * @Description: shl-int v2, v0, v1 Shift v0 left by the positions
		 *               specified by v1 and store the result in v2.
		 * @param vm
		 * @param inst
		 * @see fu.hao.trust.dvm.ByteCode#func(fu.hao.trust.dvm.DalvikVM,
		 *      patdroid.dalvik.Instruction)
		 */
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			PrimitiveInfo op0 = (PrimitiveInfo) vm.getReg(inst.r0).data;
			PrimitiveInfo op1;
			if (inst.r1 != -1) {
				op1 = (PrimitiveInfo) vm.getReg(inst.r1).data;
			} else {
				op1 = (PrimitiveInfo) inst.extra;
			}
			Register rdst = vm.getReg(inst.rdst);
			if (op0.isInteger()) {
				rdst.type = ClassInfo.primitiveInt;
				int res = op0.intValue() << op1.intValue();
				rdst.data = new PrimitiveInfo(res);
			} else if (op0.isLong()) {
				rdst.type = ClassInfo.primitiveLong;
				long res;
				if (op1.isInteger()) {
					res = op0.longValue() << op1.intValue();
				} else {
					res = op0.longValue() << op1.longValue();
				}
				rdst.data = new PrimitiveInfo(res);
			}
			jump(vm, inst, true);
		}
	}

	class OP_A_SHR implements ByteCode {
		/**
		 * @Title: func
		 * @Description: shl-int v2, v0, v1 Shift v0 right by the positions
		 *               specified by v1 and store the result in v2.
		 * @param vm
		 * @param inst
		 * @see fu.hao.trust.dvm.ByteCode#func(fu.hao.trust.dvm.DalvikVM,
		 *      patdroid.dalvik.Instruction)
		 */
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			PrimitiveInfo op0 = (PrimitiveInfo) vm.getReg(inst.r0).data;
			PrimitiveInfo op1;
			if (inst.r1 != -1) {
				op1 = (PrimitiveInfo) vm.getReg(inst.r1).data;
			} else {
				op1 = (PrimitiveInfo) inst.extra;
			}
			Register rdst = vm.getReg(inst.rdst);
			if (op0.isInteger()) {
				rdst.type = ClassInfo.primitiveInt;
				int res = op0.intValue() >> op1.intValue();
				rdst.data = new PrimitiveInfo(res);
			} else if (op0.isLong()) {
				rdst.type = ClassInfo.primitiveLong;
				long res;
				if (op1.isInteger()) {
					res = op0.longValue() >> op1.intValue();
				} else {
					res = op0.longValue() >> op1.longValue();
				}
				rdst.data = new PrimitiveInfo(res);
			}
			jump(vm, inst, true);
		}
	}

	class OP_A_USHR implements ByteCode {
		/**
		 * @Title: func
		 * @Description: ushr-int/2addr v0, v1 Unsigned shift v0 by the
		 *               positions specified by v1.
		 * @param vm
		 * @param inst
		 * @see fu.hao.trust.dvm.ByteCode#func(fu.hao.trust.dvm.DalvikVM,
		 *      patdroid.dalvik.Instruction)
		 */
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			PrimitiveInfo op0 = (PrimitiveInfo) vm.getReg(inst.r0).data;
			PrimitiveInfo op1;
			if (inst.r1 != -1) {
				op1 = (PrimitiveInfo) vm.getReg(inst.r1).data;
			} else {
				op1 = (PrimitiveInfo) inst.extra;
			}
			Register rdst = vm.getReg(inst.rdst);
			if (op0.isInteger()) {
				rdst.type = ClassInfo.primitiveInt;
				int res = op0.intValue() >>> op1.intValue();
				rdst.data = new PrimitiveInfo(res);
			} else if (op0.isLong()) {
				rdst.type = ClassInfo.primitiveLong;
				long res;
				if (op1.isInteger()) {
					res = op0.longValue() >>> op1.intValue();
				} else {
					res = op0.longValue() >>> op1.longValue();
				}
				rdst.data = new PrimitiveInfo(res);
			}
			jump(vm, inst, true);

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
			Register rdst = vm.getReg(inst.rdst);
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
			Register r0 = vm.getReg(inst.r0), r1 = vm.getReg(inst.r1);
			if (!r0.type.equals(r1.type)) {
				Log.err(TAG, "incosistent type " + inst);
				return;
			}

			PrimitiveInfo op1 = (PrimitiveInfo) vm.getReg(inst.r0).data;
			PrimitiveInfo op2 = (PrimitiveInfo) vm.getReg(inst.r1).data;
			Register rdst = vm.getReg(inst.rdst);
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
			Register r0 = vm.getReg(inst.r0), r1 = vm.getReg(inst.r1);
			if (!r0.type.equals(r1.type)) {
				Log.err(TAG, "incosistent type " + inst);
				return;
			}

			PrimitiveInfo op1 = (PrimitiveInfo) vm.getReg(inst.r0).data;
			PrimitiveInfo op2 = (PrimitiveInfo) vm.getReg(inst.r1).data;
			Register rdst = vm.getReg(inst.rdst);
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
		/**
		 * @Title: func
		 * @Description: sget v0, Test3.is1:I // field@0007 Reads field@0007
		 *               (entry #7 in the field id table) into v0.
		 * @param vm
		 * @param inst
		 * @see fu.hao.trust.dvm.ByteCode#func(fu.hao.trust.dvm.DalvikVM,
		 *      patdroid.dalvik.Instruction)
		 */
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			final String TAG = getClass().toString();
			@SuppressWarnings("unchecked")
			Pair<ClassInfo, String> pair = (Pair<ClassInfo, String>) inst.extra;

			try {
				Class<?> clazz = Class.forName(pair.first.toString());
				Field field = clazz.getDeclaredField(pair.second.toString());
				// TODO only support static field now
				vm.getReg(inst.r0).data = field.get(clazz);
				vm.getReg(inst.r0).type = ClassInfo.findOrCreateClass(vm
						.getReg(inst.r0).data.getClass());
				Log.debug(TAG, "refleciton " + vm.getReg(inst.r0).data);
			} catch (Exception e) {
				ClassInfo owner = pair.first;
				String fieldName = pair.second;
				// FieldInfo statFieldInfo = new FieldInfo(pair.first,
				// pair.second);
				// Log.debug(TAG, "sget " + statFieldInfo.getFieldType());

				DVMClass dvmClass = vm.getClass(owner);
				vm.getReg(inst.r0).data = dvmClass.getStatField(fieldName);
				ClassInfo fieldType = owner.getStaticFieldType(fieldName);
				vm.getReg(inst.r0).type = fieldType;
				Log.debug(TAG, "sget " + vm.getReg(inst.r0).data + ", from "
						+ dvmClass);
				Log.debug(
						TAG,
						"expect sget " + fieldName + ", a "
								+ vm.getReg(inst.r0).type + " from " + owner);
			}

			jump(vm, inst, true);
		}
	}

	class OP_STATIC_PUT_FIELD implements ByteCode {
		/**
		 * @Title: func
		 * @Description: sput v0, Test2.i5:I // field@0001 Stores v0 into
		 *               field@0001 (entry #1 in the field id table).
		 * @param vm
		 * @param inst
		 * @see fu.hao.trust.dvm.ByteCode#func(fu.hao.trust.dvm.DalvikVM,
		 *      patdroid.dalvik.Instruction)
		 */
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			final String TAG = getClass().toString();
			// owner and field.getName
			@SuppressWarnings("unchecked")
			Pair<ClassInfo, String> pair = (Pair<ClassInfo, String>) inst.extra;
			ClassInfo owner = pair.first;
			String fieldName = pair.second;

			ClassInfo fieldType = owner.getStaticFieldType(fieldName);
			DVMClass dvmClass = vm.getClass(owner);
			if (!vm.getReg(inst.r0).type.isConvertibleTo(fieldType)) {
				Log.warn(TAG, "Type inconsistent! " + vm.getReg(inst.r0).type
						+ " " + fieldType);
			}
			dvmClass.setStatField(fieldName, vm.getReg(inst.r0).data);
			Log.debug(TAG, "expect sput " + fieldName + " from " + owner);
			Log.debug(TAG, "real sput " + vm.getReg(inst.r0).data + " from "
					+ dvmClass);
			jump(vm, inst, true);
		}
	}

	class OP_INSTANCE_GET_FIELD implements ByteCode {
		/**
		 * @Title: func
		 * @Description: iget v0, v1, Test2.i6:I // field@0003 Reads field@0003
		 *               into v0 (entry #3 in the field id table). The instance
		 *               is referenced by v1.
		 * @param vm
		 * @param inst
		 * @see fu.hao.trust.dvm.ByteCode#func(fu.hao.trust.dvm.DalvikVM,
		 *      patdroid.dalvik.Instruction)
		 */
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			final String TAG = getClass().toString();
			FieldInfo fieldInfo = (FieldInfo) inst.extra;
			Object obj = vm.getReg(inst.r0).data;
			Log.bb(TAG, "obj " + obj);
			Log.bb(TAG, "fieldinfo " + fieldInfo);
			if (obj instanceof DVMObject) {
				DVMObject dvmObj = (DVMObject) obj;
				vm.getReg(inst.r1).type = fieldInfo.getFieldType();

				if (dvmObj.getFieldObj(fieldInfo) == null) {
					if (fieldInfo.fieldName.equals("this$0")) {
						dvmObj.setField(fieldInfo, vm.callbackOwner);
					} else {
						dvmObj.setField(fieldInfo,
								new Unknown(fieldInfo.getFieldType()));
					}
				}

				vm.getReg(inst.r1).data = dvmObj.getFieldObj(fieldInfo);

				Log.debug(TAG, "Get data: " + vm.getReg(inst.r1).data);
			} else {
				// TODO reflection set field
			}

			jump(vm, inst, true);
		}
	}

	class OP_INSTANCE_PUT_FIELD implements ByteCode {
		/**
		 * @Title: func
		 * @Description: iput v0,v2, Test2.i6:I // field@0002 Stores v0 into
		 *               field@0002 (entry #2 in the field id table). The
		 *               instance is referenced by v2
		 * @param vm
		 * @param inst
		 * @see fu.hao.trust.dvm.ByteCode#func(fu.hao.trust.dvm.DalvikVM,
		 *      patdroid.dalvik.Instruction)
		 */
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			final String TAG = getClass().toString();
			FieldInfo fieldInfo = (FieldInfo) inst.extra;
			Log.bb(TAG, "field " + fieldInfo);
			Object obj = vm.getReg(inst.r0).data;
			if (obj instanceof DVMObject) {
				DVMObject dvmObj = (DVMObject) obj;
				dvmObj.setField(fieldInfo, vm.getReg(inst.r1).data);
				Log.msg(TAG, "Put field " + dvmObj.getFieldObj(fieldInfo)
						+ " to the field of " + dvmObj);
			} else {
				// TODO reflection set field
			}
			jump(vm, inst, true);
		}
	}

	class OP_EXCEPTION_TRYCATCH implements ByteCode {
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			// TODO Auto-generated method stub
			jump(vm, inst, true);
		}
	}

	class OP_EXCEPTION_THROW implements ByteCode {
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			vm.getCurrStackFrame().exceptReg = vm.getReg(inst.r0);
			jump(vm, inst, true);
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
			Object data = vm.getReg(inst.r0);

			for (int key : switchTable.keySet()) {
				if (data.equals(key)) {
					jump(vm, inst, false);
					return;
				}
			}

			jump(vm, inst, true);
		}
	}

	class OP_HALT implements ByteCode {
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
			jump(vm, inst, true);
			Log.debug(TAG, "cannot resolve the invocation");
		}
	}

	class OP_CMP implements ByteCode {
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
			// check whether contains an unknown var
			Register r0 = vm.getReg(inst.r0);
			Register r1 = null;
			if (inst.r1 != -1) {
				r1 = vm.getReg(inst.r1);
			}

			BiDirVar u0;
			Log.debug(TAG, "r0 data " + r0.data);
			if (r0.data == null) {
				Log.warn(TAG, "Null operator found!");
				r0.data = new Unknown(r0.type);
			}

			if (r0.data instanceof BiDirVar) {
				u0 = (BiDirVar) r0.data;

				u0.addConstriant(vm, inst);

				if (vm.unknownBranches.contains(inst)) {
					Log.warn(TAG, "I am here");
					jump(vm, inst, true);
					// vm.states.pop();
					return;
				}

				vm.unknownBranches.push(inst);
				vm.storeState();
				if (r1 != null && r1.data instanceof Unknown) {
					// TODO
				}

				jump(vm, inst, false);

				Log.warn(TAG, "BiDir branch " + inst);
				// TODO add constraint inconsistency check to rm unreachable
				// code
				return;
			} else {
				auxByteCodes.get((int) inst.opcode_aux).func(vm, inst);
			}
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
		Register r0 = vm.getReg(inst.r0);
		/*
		 * if (!r0.type.equals(r1.type)) { Log.err(TAG, "inconsistent type " +
		 * inst); return null; }
		 */
		if (!(r0.data instanceof PrimitiveInfo)) {
			r0.data = PrimitiveInfo.fromObject(r0.data);
		}

		PrimitiveInfo op1 = (PrimitiveInfo) r0.data;
		PrimitiveInfo op2;
		if (flagZ) {
			op2 = new PrimitiveInfo(0);
		} else {
			op2 = (PrimitiveInfo) vm.getReg(inst.r1).data;
		}

		if (inst.rdst != -1) {
			Register rdst = vm.getReg(inst.rdst);
			rdst.type = ClassInfo.primitiveInt;
		}
		PrimitiveInfo[] res = new PrimitiveInfo[2];
		res[0] = op1;
		res[1] = op2;

		Log.debug(TAG, "ops: " + op1 + " " + op2);
		return res;
	}

	/**
	 * @Title: jump
	 * @Author: hao
	 * @Description: GOTO
	 * @param
	 * @param
	 * @param
	 * @return void
	 * @throws
	 */
	public void jump(DalvikVM vm, Instruction inst, boolean seq) {
		if (seq) {
			if (vm.getCurrStackFrame() == null) {
				vm.pc = Integer.MAX_VALUE;
				return;
			}
			vm.pc++;
			vm.getCurrStackFrame().pc++;
		} else {
			if (inst.extra == null) {
				Log.err(TAG, "unresolve dest address in goto: " + inst);
				return;
			}
			vm.getCurrStackFrame().pc = (int) inst.extra;
			vm.pc = vm.getCurrStackFrame().pc;
		}
	}

	/**
	 * @Title: invocation
	 * @Author: Hao Fu
	 * @Description: invocation helper
	 * @param @param vm
	 * @param @param mi
	 * @return void
	 * @throws
	 */
	public void invocation(DalvikVM vm, Instruction inst) {
		vm.retValReg.type = null;
		vm.retValReg.data = null;

		Object[] extra = (Object[]) inst.extra;
		MethodInfo mi = (MethodInfo) extra[0];
		// The register index referred by args
		int[] args = (int[]) extra[1];
		final String TAG = getClass().toString();
		Object obj = null;
		Method method = null;
		try {
			// If applicable, directly use reflection to run the method,
			// the method is inside java.lang
			// Class<?> clazz = Class.forName(mi.myClass.toString());
			Log.bb(TAG, "arg0 obj: " + vm.getReg(args[0]).data);
			Class<?> clazz;
			// if (vm.getReg(args[0]).data == null) {
			clazz = Class.forName(mi.myClass.toString());
			// } else {
			// clazz = vm.getReg(args[0]).data.getClass();
			// }

			@SuppressWarnings("rawtypes")
			Class[] argsClass = new Class[mi.paramTypes.length];
			Object[] params = new Object[args.length - 1];

			if (mi.isConstructor()) {
				// use DvmObject to replace java.lang.Object
				if (!mi.myClass.toString().equals("java.lang.Object")) {
					// clazz = Class.forName(mi.myClass.toString());
					if (args.length == 1) {
						vm.getReg(args[0]).data = clazz.newInstance();
						vm.getReg(args[0]).type = mi.returnType;
						Log.debug(TAG, "Init instance: "
								+ vm.getReg(args[0]).data);
					} else {
						getParams(vm, mi, args, argsClass, params);
						// Overwrite previous declared dvmObj
						vm.getReg(args[0]).data = clazz.getConstructor(
								argsClass).newInstance(params);
						vm.getReg(args[0]).type = mi.myClass;
						Log.debug(
								TAG,
								"Init instance: " + vm.getReg(args[0]).data
										+ " "
										+ vm.getReg(args[0]).data.getClass());
					}
				}
			} else {

				Log.debug(TAG, "Reflction class: " + clazz);

				obj = vm.getReg(args[0]).data;
				
				if (obj instanceof BiDirVar) {
					BiDirVar bidirVar = (BiDirVar) obj;
					obj = bidirVar.getValue();
				}
				
				if (obj == null || obj instanceof DVMObject
						&& !DVMObject.class.equals(clazz)) {
					obj = clazz.newInstance();
				}		

				// clazz = obj.getClass();
				vm.retValReg.type = mi.returnType;
				if (args.length == 1) {
					method = clazz.getDeclaredMethod(mi.name);
					vm.retValReg.data = method.invoke(obj);
				} else {
					getParams(vm, mi, args, argsClass, params);
					method = clazz.getDeclaredMethod(mi.name, argsClass);
					Log.debug(TAG, "Caller obj: " + obj + ", from class: "
							+ obj.getClass().toString());
					// handle return val
					vm.retValReg.data = method.invoke(obj, params);
				}
				if (vm.retValReg.data != null) {
					Log.debug(TAG, "Return data: " + vm.retValReg.data + " ,"
							+ vm.retValReg.data.getClass());
				}
				Log.msg(TAG, "Reflction invocation " + method);

			}

			vm.plugin.method = method;
			jump(vm, inst, true);
		} catch (java.lang.IllegalArgumentException e) {
			e.printStackTrace();
			Log.warn(
					TAG,
					"obj " + obj + " not an instance of "
							+ method.getDeclaringClass());
			jump(vm, inst, true);
		} catch (java.lang.NullPointerException e) {
			e.printStackTrace();
			Log.err(TAG, " null pointer ");
		} catch (java.lang.ClassNotFoundException e) {
			vm.plugin.method = null;
			Log.debug(TAG, "not a reflction invocation " + mi);
			invocation(vm, mi, inst, args);
		} catch (Exception e) {
			e.printStackTrace();
			Log.warn(TAG, "Error in reflection");
			jump(vm, inst, true);
		}

	}

	/**
	 * @Title: getParams
	 * @Author: Hao Fu
	 * @Description: Transform from ClassInfo to Class
	 * @param vm
	 * @param mi
	 * @param args
	 * @param argsClass
	 * @param params
	 * @param @throws ClassNotFoundException
	 * @return void
	 * @throws
	 */
	private void getParams(DalvikVM vm, MethodInfo mi, int[] args,
			Class<?>[] argsClass, Object[] params)
			throws ClassNotFoundException {		
		// Start from 1 to ignore "this"
		for (int i = 1; i < args.length; i++) {
			if (mi.paramTypes[i - 1].isPrimitive()) {
				Log.debug(TAG, "expected para type: " + mi.paramTypes[i - 1]);
				Object primitive = resolvePrimitive((PrimitiveInfo) vm
						.getReg(args[i]).data);
				Class<?> argClass = primClasses.get(primitive.getClass());
				params[i - 1] = primitive;
				// Because of a bug in PATDroid.
				if (mi.paramTypes[i - 1].equals(ClassInfo.primitiveBoolean)
						&& primitive instanceof Integer) {
					if (primitive.equals(1)) {
						params[i - 1] = true;
					} else if (primitive.equals(0)) {
						params[i - 1] = false;
					}
					argClass = boolean.class;
				}

				Log.debug(TAG, "Real para " + argClass);
				argsClass[i - 1] = argClass;
			} else {
				String argClass = mi.paramTypes[i - 1].toString();
				argsClass[i - 1] = Class.forName(argClass);
				Object argData = vm.getReg(args[i]).data;

				if (argData == null) {
					Log.warn(
							TAG,
							"Null in the " + i + "th arg, is "
									+ vm.getReg(args[i]));
					params[i - 1] = null;
				} else if (matchType(argData.getClass(), argsClass[i - 1])) {
					params[i - 1] = argData;
					// argData.getClass().getInterfaces()
				} else {
					// FIXME null
					Log.warn(TAG, "Mismatch type! " + "real para type: "
							+ argData.getClass() + ", expected para type: "
							+ argsClass[i - 1]);
					if (argData instanceof BiDirVar) {
						BiDirVar bidirVar = (BiDirVar) argData;
						params[i - 1] = bidirVar.getValue();
						Log.debug(TAG, "Param " + params[i - 1]);
					} else {
						params[i - 1] = null;
					}
				}
			}
		}
	}		

	public boolean matchType(Class<?> real, Class<?> expected) {
		if (expected.equals(real) || expected.equals(real.getSuperclass())) {
			return true;
		}

		for (Class<?> interf : real.getInterfaces()) {
			if (interf.equals(expected)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * @Title: invocation
	 * @Author: Hao Fu
	 * @Description: Run the non-reflectable method.
	 * @param @param vm
	 * @param @param mi
	 * @return void
	 * @throws
	 */
	public void invocation(DalvikVM vm, MethodInfo mi, Instruction inst,
			int[] args) {
		// Create a new stack frame and push it to the stack.
		if (!mi.isStatic()) {
			if (vm.getReg(args[0]).data instanceof Unknown
					|| vm.getReg(args[0]).data == null) {
				Log.warn(TAG, "NULL Invocator!");
				vm.retValReg.data = null;
				vm.retValReg.type = null;
				jump(vm, inst, true);
				return;
			}
			DVMObject thisObj = (DVMObject) vm.getReg(args[0]).data;
			if (((int) inst.opcode_aux != 0x0C) && !mi.isConstructor()
					&& thisObj.getType().getSuperClass().equals(mi.myClass)) {
				mi = thisObj.getType().findMethodsHere(mi.name)[0];
			}
		}

		if (args != null) {
			vm.setContext(args);
		}

		StackFrame stackFrame = vm.newStackFrame(mi);
		vm.stack.add(stackFrame);
	}

	public void runMethod(DalvikVM vm, MethodInfo mi) {
		// Create a new stack frame and push it to the stack.
		StackFrame stackFrame = vm.newStackFrame(mi);
		vm.stack.add(stackFrame);
		if (mi.isStatic()) {
			stackFrame.thisObj = null;
		} else {
			if (chainThisObj == null) {
				Log.msg(TAG, "New chain obj");
				stackFrame.thisObj = new DVMObject(vm, mi.myClass);
				MethodInfo constructor = mi.myClass.getDefaultConstructor();
				if (constructor != null) {
					StackFrame construct = vm.newStackFrame(constructor);
					construct.thisObj = stackFrame.thisObj;
					vm.stack.add(construct);
				}
				chainThisObj = stackFrame.thisObj;
			} else {
				stackFrame.thisObj = chainThisObj;
			}
		}

		if (!running) {
			Log.msg(TAG, "RUN BEGIN " + mi);
			run(vm);
		}
	}

	/**
	 * @fieldName: chainThisObj
	 * @fieldType: DVMObject
	 * @Description: To help run chain methods.
	 */
	DVMObject chainThisObj = null;

	public void run(DalvikVM vm) {
		Log.msg(TAG, "RUN BEGIN");
		running = true;
		String mname = vm.getCurrStackFrame().method.name;
		while (vm.getCurrStackFrame() != null
				&& vm.pc < vm.getCurrStackFrame().method.insns.length) {
			Instruction insns = vm.getCurrStackFrame().method.insns[vm.pc];
			insns.setLoc(vm.getCurrStackFrame().method);
			exec(vm, insns);
		}

		running = false;
		Log.msg(TAG, "RUN DONE! The last one is " + mname);
	}

	static Map<Integer, ByteCode> byteCodes = new HashMap<>();
	static Map<Integer, ByteCode> auxByteCodes = new HashMap<>();

	@SuppressWarnings("rawtypes")
	static Map<Class, Class> primClasses = new HashMap<>();

	/**
	 * interpret auxiliary opcodes
	 */
	Interpreter() {
		// Integer.class is not as same as int.class for reflection
		primClasses.put(Integer.class, int.class);
		primClasses.put(Long.class, long.class);
		primClasses.put(Float.class, float.class);
		primClasses.put(Boolean.class, boolean.class);
		primClasses.put(Double.class, double.class);
		primClasses.put(Byte.class, byte.class);
		primClasses.put(Character.class, char.class);

		byteCodes.put(0x06, new OP_GOTO());
		byteCodes.put(0x08, new OP_CMP());
		byteCodes.put(0x0E, new OP_SWITCH());
		byteCodes.put(0x0F, new OP_HALT());

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

	public void exec(DalvikVM vm, Instruction inst) {	
		Log.debug(TAG, vm.pc + " " + inst + " at "
				+ vm.getCurrStackFrame().method);
		Log.bb(TAG, "opcode: " + inst.opcode + " " + inst.opcode_aux);

		if (byteCodes.containsKey((int) inst.opcode)) {
			byteCodes.get((int) inst.opcode).func(vm, inst);
		} else if (auxByteCodes.containsKey((int) inst.opcode_aux)) {
			auxByteCodes.get((int) inst.opcode_aux).func(vm, inst);
		} else {
			Log.err(TAG, "unsupported opcode " + inst);
		}

		if (vm.plugin != null && vm.getCurrStackFrame() != null) {
			vm.plugin.runAnalysis(vm, inst, vm.plugin.getCurrRes());
			vm.getCurrStackFrame().pluginRes = new HashMap<>(vm.plugin.currtRes);
			if (vm.plugin.interested != null && vm.plugin.interested.contains(inst)) {
				vm.plugin.interested.remove(inst);
				Log.msg(TAG, "HERE found " + inst);
			}
			Log.debug(TAG, "Tainted set: " + vm.plugin.currtRes);
		}
	}

	/**
	 * @Title: resolvePrimitive
	 * @Author: hao
	 * @Description: get true java obj representation of primitive
	 * @param @param op1
	 * @param @return
	 * @return Object
	 * @throws
	 */
	public Object resolvePrimitive(PrimitiveInfo op1) {
		if (op1.isBoolean()) {
			return new Boolean(op1.booleanValue());
		} else if (op1.isInteger()) {
			return new Integer(op1.intValue());
		} else if (op1.isLong()) {
			return new Long(op1.longValue());
		} else if (op1.isFloat()) {
			return new Float(op1.floatValue());
		} else if (op1.isDouble()) {
			return new Double(op1.doubleValue());
		} else if (op1.isChar()) {
			return new Character(op1.charValue());
		}

		return null;
	}

}