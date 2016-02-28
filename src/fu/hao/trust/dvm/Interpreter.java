package fu.hao.trust.dvm;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import fu.hao.trust.dvm.DalvikVM.JVM_STACK_FRAME;
import fu.hao.trust.dvm.DalvikVM.Register;
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
			vm.regs[inst.rdst].copy(vm.regs[inst.r0]);
			Log.debug(getClass().toString(), "mov " + vm.regs[inst.r0].data
					+ " to " + vm.regs[inst.rdst].data);
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
			Log.debug(TAG, "Mov const begin " + inst);
			vm.regs[inst.rdst].copy(inst.type, inst.extra);
			Log.debug(TAG, "data: " + vm.regs[inst.rdst].data + " "
					+ vm.regs[inst.rdst].data.getClass());
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
			// jump(vm, inst, true);
			if (vm.curr_jvm_stack != null) {
				vm.pc = vm.curr_jvm_stack.pc;
				Log.debug(TAG, "pc " + vm.pc + " " + vm.curr_jvm_stack.pc);
			} else {
				vm.pc = Integer.MAX_VALUE;
				// backtrace to last unknown branch
				vm.restoreState();
				Log.warn(TAG, "Backtrace begin!!!");
			}
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
			JVM_STACK_FRAME caller = vm.curr_jvm_stack.prev_stack;
			vm.return_val_reg.data = vm.regs[inst.r0].data;
			vm.return_val_reg.type = ClassInfo
					.findOrCreateClass(vm.regs[inst.r0].data.getClass());
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
		/**
		 * @Title: func
		 * @Description: set arguments for the new invocation
		 * @param vm
		 * @param inst
		 * @see fu.hao.trust.dvm.ByteCode#func(fu.hao.trust.dvm.DalvikVM,
		 *      patdroid.dalvik.Instruction)
		 */
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			int[] args = (int[]) inst.extra;

			if (vm.getContext() == null) {
				Log.warn(TAG, "null ctx!");
				MethodInfo currMethod = vm.curr_jvm_stack.method;
				int startParam = 0;
				if (!currMethod.isStatic()) {
					startParam = 1;
					vm.regs[args[0]].data = vm.thisObj;
					vm.regs[args[0]].type = currMethod.myClass;
					Log.debug(TAG, "args: " + vm.regs[args[0]].data);
				}

				int i = startParam;
				for (ClassInfo paramClass : currMethod.paramTypes) {
					if (paramClass.isPrimitive()) {
						// FIXME
						vm.regs[args[i]].data = new PrimitiveInfo(42);
						vm.regs[args[i]].type = paramClass;
					} else if (paramClass.isArray()) {
						// FIXME
					} else {
						vm.regs[args[i]].data = new DVMObject(vm, paramClass);
						vm.regs[args[i]].type = paramClass;
					}
					Log.debug(TAG, "args: " + vm.regs[args[i]].data);
					i++;
				}
			} else {
				if (args.length != vm.getContext().length) {
					Log.err(TAG, "invalid ctx for invocation!");
					return;
				}
				for (int i = 0; i < vm.getContext().length; i++) {
					vm.regs[args[i]].copy(vm.regs[vm.getContext()[i]]);
					Log.debug(TAG, "params: " + vm.regs[args[i]].data);
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

			Log.debug(TAG, "" + vm.regs[inst.rdst]);

			vm.regs[inst.rdst].type = inst.type;
			try {
				Class.forName(inst.type.toString());
				vm.regs[inst.rdst].data = null;
			} catch (ClassNotFoundException e) {
				// Do not need to handle reflection type,
				// since <init> invocation will replace the newObj
				Object newObj = new DVMObject(vm, inst.type);
				Log.debug(TAG, "begin new object of " + inst.type + "created.");
				vm.regs[inst.rdst].data = newObj;
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
			// TODO reflection obj
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
			invocation(vm, inst);
		}
	}

	class OP_INVOKE_SUPER implements ByteCode {
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			// TODO Auto-generated method stub
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
				Object[] params = new Object[args.length - 1];

				if (mi.isConstructor()) {
					if (args.length == 1) {
						vm.regs[args[0]].data = clazz.newInstance();
						return;
					}

					getParams(vm, mi, args, argsClass, params);
					// overwrite previous declared dvmObj
					vm.regs[args[0]].data = clazz.getConstructor(argsClass)
							.newInstance(params);
					vm.regs[args[0]].type = mi.myClass;
					Log.debug(TAG, "return data: " + vm.regs[args[0]].data
							+ " " + vm.regs[args[0]].data.getClass());
					return;
				}

				Object obj = vm.regs[args[0]].data;
				if (args.length == 1) {
					method = clazz.getDeclaredMethod(mi.name);
					vm.return_val_reg.data = method.invoke(obj);
				} else {
					getParams(vm, mi, args, argsClass, params);
					method = clazz.getDeclaredMethod(mi.name, argsClass);
					Log.debug(TAG, "caller obj: " + obj.getClass().toString());
					// handle return val
					vm.return_val_reg.data = method.invoke(obj, params);
				}
				if (vm.return_val_reg.data != null) {
					vm.return_val_reg.type = ClassInfo
							.findOrCreateClass(vm.return_val_reg.data
									.getClass());
					Log.debug(TAG, "return data: " + vm.return_val_reg.data
							+ " " + vm.return_val_reg.data.getClass());
				}
				Log.msg(TAG, "reflction invocation " + method);
				vm.plugin.method = method;
			} catch (Exception e) {
				vm.plugin.method = null;
				e.printStackTrace();
				Log.debug(TAG, "not reflction invocation " + mi.myClass);
				vm.newStackFrame(mi);
				vm.setContext(new int[args.length]);
				for (int i = 0; i < args.length; i++) {
					vm.getContext()[i] = args[i];
				}
			}

			jump(vm, inst, true);
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
					vm.return_val_reg.data = method.invoke(null);
				} else {
					Object[] params = new Object[args.length - 1];
					// start from 0 since no "this"
					for (int i = 0; i < args.length; i++) {
						if (mi.paramTypes[i].isPrimitive()) {
							Object primitive = resolvePrimitive((PrimitiveInfo) vm.regs[args[i]].data);
							params[i] = primitive;
							Class<?> argClass = primClasses.get(primitive
									.getClass());
							argsClass[i] = argClass;
						} else {
							// TODO use classloader to check exists or not
							String argClass = mi.paramTypes[i].toString();
							argsClass[i] = Class.forName(argClass);
							params[i] = vm.regs[args[i]].data;
						}
					}
					method = clazz.getDeclaredMethod(mi.name, argsClass);
					vm.return_val_reg.data = method.invoke(null, params);
				}

				if (vm.return_val_reg.data != null) {
					vm.return_val_reg.type = ClassInfo
							.findOrCreateClass(vm.return_val_reg.data
									.getClass());
					Log.debug(TAG, "return data: " + vm.return_val_reg.data
							+ " " + vm.return_val_reg.data.getClass());
				}
				Log.msg(TAG, "reflction invocation " + method);
				vm.plugin.method = method;
			} catch (Exception e) {
				vm.plugin.method = null;
				e.printStackTrace();
				Log.debug(TAG, "not a reflction invocation " + mi);
				vm.newStackFrame(mi);
			}

			jump(vm, inst, true);
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
			Object array = vm.regs[inst.r0].data;
			if (array.getClass().isArray()) {
				vm.regs[inst.rdst].data = new PrimitiveInfo(
						Array.getLength(array));
				Log.debug(TAG, "len " + Array.getLength(array));
				vm.regs[inst.rdst].type = ClassInfo.primitiveInt;
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
			if (vm.regs[inst.rdst].type != null
					&& !inst.type.isConvertibleTo(vm.regs[inst.rdst].type)) {
				Log.err(TAG, "not consistent type when cast!");
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

			if (vm.return_val_reg.type != null
					&& vm.return_val_reg.type.isPrimitive()) {
				vm.return_val_reg.data = PrimitiveInfo
						.fromObject(vm.return_val_reg.data);
			}

			// type checking before moving?
			vm.regs[inst.rdst].copy(vm.return_val_reg);
			Log.debug(TAG, "data " + vm.regs[inst.rdst].data + " "
					+ vm.regs[inst.rdst].type + " " + vm.regs[inst.rdst]);
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
			Log.debug(TAG, "cast data " + vm.regs[inst.r0].data);
			if (!(vm.regs[inst.r0].data instanceof PrimitiveInfo)) {
				vm.regs[inst.r0].data = PrimitiveInfo
						.fromObject(vm.regs[inst.r0].data);
			}
			PrimitiveInfo primitive = (PrimitiveInfo) vm.regs[inst.r0].data;
			vm.regs[inst.rdst].data = primitive.castTo(inst.type);
			vm.regs[inst.rdst].type = inst.type;
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
		 * @Description: (�� JavaDoc)
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
			if (op1.equals(op2) || op1.booleanValue() == false) {
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
		 * @Description: (�� JavaDoc)
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
			if (!op1.equals(op2) || op1.booleanValue() == true) {
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
			Object array = vm.regs[inst.r0].data;
			if (array.getClass().isArray()) {
				// dest reg
				Register rdst = vm.regs[inst.rdst];
				// array reg
				// Object[] array = (Object[]) vm.regs[inst.r0].data;
				// index reg
				int index = ((PrimitiveInfo) vm.regs[inst.r1].data).intValue();

				rdst.type = vm.regs[inst.r0].type.getElementClass();
				Object element = Array.get(array, index);
				// if (element.getClass().isPrimitive()) {
				if (rdst.type.isPrimitive()) {
					rdst.data = PrimitiveInfo.fromObject(element);
				} else {
					rdst.data = Array.get(vm.regs[inst.r0].data, index);// array[index];
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
			Register rdst = vm.regs[inst.rdst];
			// array reg
			Object[] array = (Object[]) vm.regs[inst.r0].data;
			// index reg
			int index = ((PrimitiveInfo) vm.regs[inst.r1].data).intValue();
			if (!rdst.type.isConvertibleTo(vm.regs[inst.r0].type
					.getElementClass())) {
				Log.err(TAG, "inconsistent type " + inst);
				return;
			}
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
			Log.debug(TAG, "data " + vm.regs[inst.r0].data);
			if (vm.regs[inst.r0].data instanceof Unknown) {
				Log.warn(TAG, "Unknown primitive data found.");
				jump(vm, inst, true);
				return;
			}
			PrimitiveInfo op0 = (PrimitiveInfo) vm.regs[inst.r0].data;
			PrimitiveInfo op1;
			if (inst.r1 != -1) {
				op1 = (PrimitiveInfo) vm.regs[inst.r1].data;
			} else {
				op1 = (PrimitiveInfo) inst.extra;
			}

			if (op0 == null || op1 == null) {
				Log.warn(TAG, "Unknown primitive data found.");
				jump(vm, inst, true);
				return;
			}
			Register rdst = vm.regs[inst.rdst];
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
			PrimitiveInfo op0 = (PrimitiveInfo) vm.regs[inst.r0].data;
			PrimitiveInfo op1;
			if (inst.r1 != -1) {
				op1 = (PrimitiveInfo) vm.regs[inst.r1].data;
			} else {
				op1 = (PrimitiveInfo) inst.extra;
			}
			Register rdst = vm.regs[inst.rdst];
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
			PrimitiveInfo op0 = (PrimitiveInfo) vm.regs[inst.r0].data;
			PrimitiveInfo op1;
			if (inst.r1 != -1) {
				op1 = (PrimitiveInfo) vm.regs[inst.r1].data;
			} else {
				op1 = (PrimitiveInfo) inst.extra;
			}
			Register rdst = vm.regs[inst.rdst];

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
			PrimitiveInfo op0 = (PrimitiveInfo) vm.regs[inst.r0].data;
			PrimitiveInfo op1;
			if (inst.r1 != -1) {
				op1 = (PrimitiveInfo) vm.regs[inst.r1].data;
			} else {
				op1 = (PrimitiveInfo) inst.extra;
			}
			Register rdst = vm.regs[inst.rdst];
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
				PrimitiveInfo op0 = (PrimitiveInfo) vm.regs[inst.r0].data;
				PrimitiveInfo op1;
				if (inst.r1 != -1) {
					op1 = (PrimitiveInfo) vm.regs[inst.r1].data;
				} else {
					op1 = (PrimitiveInfo) inst.extra;
				}
				Register rdst = vm.regs[inst.rdst];

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
				if (vm.regs[inst.r0].data instanceof Unknown) {
					op = (Unknown) vm.regs[inst.r0].data;
					op.addLastArith(inst);
				}

				if (inst.r1 != -1 && vm.regs[inst.r1].data instanceof Unknown) {
					op = (Unknown) vm.regs[inst.r0].data;
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
			PrimitiveInfo op0 = (PrimitiveInfo) vm.regs[inst.r0].data;
			PrimitiveInfo op1;
			if (inst.r1 != -1) {
				op1 = (PrimitiveInfo) vm.regs[inst.r1].data;
			} else {
				op1 = (PrimitiveInfo) inst.extra;
			}
			Register rdst = vm.regs[inst.rdst];
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
			PrimitiveInfo op0 = (PrimitiveInfo) vm.regs[inst.r0].data;
			Register rdst = vm.regs[inst.rdst];
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
			PrimitiveInfo op0 = (PrimitiveInfo) vm.regs[inst.r0].data;
			Register rdst = vm.regs[inst.rdst];
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
			PrimitiveInfo op0 = (PrimitiveInfo) vm.regs[inst.r0].data;
			PrimitiveInfo op1;
			if (inst.r1 != -1) {
				op1 = (PrimitiveInfo) vm.regs[inst.r1].data;
			} else {
				op1 = (PrimitiveInfo) inst.extra;
			}
			Register rdst = vm.regs[inst.rdst];
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
			PrimitiveInfo op0 = (PrimitiveInfo) vm.regs[inst.r0].data;
			PrimitiveInfo op1;
			if (inst.r1 != -1) {
				op1 = (PrimitiveInfo) vm.regs[inst.r1].data;
			} else {
				op1 = (PrimitiveInfo) inst.extra;
			}
			Register rdst = vm.regs[inst.rdst];
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
			PrimitiveInfo op0 = (PrimitiveInfo) vm.regs[inst.r0].data;
			PrimitiveInfo op1;
			if (inst.r1 != -1) {
				op1 = (PrimitiveInfo) vm.regs[inst.r1].data;
			} else {
				op1 = (PrimitiveInfo) inst.extra;
			}
			Register rdst = vm.regs[inst.rdst];
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
			PrimitiveInfo op0 = (PrimitiveInfo) vm.regs[inst.r0].data;
			PrimitiveInfo op1;
			if (inst.r1 != -1) {
				op1 = (PrimitiveInfo) vm.regs[inst.r1].data;
			} else {
				op1 = (PrimitiveInfo) inst.extra;
			}
			Register rdst = vm.regs[inst.rdst];
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
			PrimitiveInfo op0 = (PrimitiveInfo) vm.regs[inst.r0].data;
			PrimitiveInfo op1;
			if (inst.r1 != -1) {
				op1 = (PrimitiveInfo) vm.regs[inst.r1].data;
			} else {
				op1 = (PrimitiveInfo) inst.extra;
			}
			Register rdst = vm.regs[inst.rdst];
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
			Register rdst = vm.regs[inst.rdst];
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
			Register r0 = vm.regs[inst.r0], r1 = vm.regs[inst.r1];
			if (!r0.type.equals(r1.type)) {
				Log.err(TAG, "incosistent type " + inst);
				return;
			}

			PrimitiveInfo op1 = (PrimitiveInfo) vm.regs[inst.r0].data;
			PrimitiveInfo op2 = (PrimitiveInfo) vm.regs[inst.r1].data;
			Register rdst = vm.regs[inst.rdst];
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
			Register r0 = vm.regs[inst.r0], r1 = vm.regs[inst.r1];
			if (!r0.type.equals(r1.type)) {
				Log.err(TAG, "incosistent type " + inst);
				return;
			}

			PrimitiveInfo op1 = (PrimitiveInfo) vm.regs[inst.r0].data;
			PrimitiveInfo op2 = (PrimitiveInfo) vm.regs[inst.r1].data;
			Register rdst = vm.regs[inst.rdst];
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
			@SuppressWarnings("unchecked")
			Pair<ClassInfo, String> pair = (Pair<ClassInfo, String>) inst.extra;

			try {
				Class<?> clazz = Class.forName(pair.first.toString());
				Field field = clazz.getDeclaredField(pair.second.toString());
				// TODO only support static field now
				vm.regs[inst.r0].data = field.get(clazz);
				vm.regs[inst.r0].type = ClassInfo
						.findOrCreateClass(vm.regs[inst.r0].data.getClass());
				Log.debug(TAG, "refleciton " + vm.regs[inst.r0].data);
			} catch (Exception e) {
				ClassInfo owner = pair.first;
				String fieldName = pair.second;
				// FieldInfo statFieldInfo = new FieldInfo(pair.first,
				// pair.second);
				// Log.debug(TAG, "sget " + statFieldInfo.getFieldType());

				DVMClass dvmClass = vm.getClass(owner);
				Log.debug(TAG, "sget " + fieldName + " from " + dvmClass);
				vm.regs[inst.r0].data = dvmClass.getStatField(fieldName);
				ClassInfo fieldType = owner.getStaticFieldType(fieldName);
				vm.regs[inst.r0].type = fieldType;
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
			// owner and field.getName
			@SuppressWarnings("unchecked")
			Pair<ClassInfo, String> pair = (Pair<ClassInfo, String>) inst.extra;
			ClassInfo owner = pair.first;
			String fieldName = pair.second;
			Log.debug(TAG,
					"field type " + pair.first.getStaticFieldType(pair.second));
			ClassInfo fieldType = owner.getStaticFieldType(fieldName);
			DVMClass dvmClass = vm.getClass(owner);
			Log.debug(TAG, "getDvmClass " + dvmClass);
			if (!vm.regs[inst.r0].type.isConvertibleTo(fieldType)) {
				Log.warn(TAG, "Type inconsistent! " + vm.regs[inst.r0].type
						+ " " + fieldType);
			}
			dvmClass.setStatField(fieldName, vm.regs[inst.r0].data);
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
			FieldInfo fieldInfo = (FieldInfo) inst.extra;
			Object obj = vm.regs[inst.r0].data;
			Log.debug(TAG, "obj " + obj);
			if (obj instanceof DVMObject) {
				DVMObject dvmObj = (DVMObject) obj;
				vm.regs[inst.r1].type = fieldInfo.getFieldType();

				if (dvmObj.getFieldObj(fieldInfo) == null) {
					dvmObj.setField(fieldInfo,
							new Unknown(fieldInfo.getFieldType()));
				}

				vm.regs[inst.r1].data = dvmObj.getFieldObj(fieldInfo);

				Log.debug(TAG, "get data: " + vm.regs[inst.r1].data);
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
			FieldInfo fieldInfo = (FieldInfo) inst.extra;
			Log.debug(TAG, "field " + fieldInfo);
			Object obj = vm.regs[inst.r0].data;
			if (obj instanceof DVMObject) {
				DVMObject dvmObj = (DVMObject) obj;
				dvmObj.setField(fieldInfo, vm.regs[inst.r1].data);
				Log.debug(TAG, "put field" + dvmObj.getFieldObj(fieldInfo));
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
			// TODO Auto-generated method stub
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

	class OP_HALT implements ByteCode {
		/**
		 * @Title: func
		 * @Description: (�� JavaDoc)
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
		 * @Description: (�� JavaDoc)
		 * @param vm
		 * @param inst
		 * @see fu.hao.trust.dvm.ByteCode#func(fu.hao.trust.dvm.DalvikVM,
		 *      patdroid.dalvik.Instruction)
		 */
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			// check whether contains an unknown var 
			Register r0 = vm.regs[inst.r0];
			Register r1 = null;
			if (inst.r1 != -1) {
				r1 = vm.regs[inst.r1];
			}
			
			Unknown u0;
			if (r0.data instanceof Unknown) {
				u0 = (Unknown) r0.data; 
				u0.addConstriant(vm, inst);
				vm.storeState();
				if (r1 != null && r1.data instanceof Unknown) {
					// TODO
				}
				jump(vm, inst, false);
				Log.debug(TAG, "unknown branch");
				// TODO add constraint inconsistency check to rm unreachable code
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
		Register r0 = vm.regs[inst.r0];
		/*
		 * if (!r0.type.equals(r1.type)) { Log.err(TAG, "incosistent type " +
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
			op2 = (PrimitiveInfo) vm.regs[inst.r1].data;
		}

		if (inst.rdst != -1) {
			Register rdst = vm.regs[inst.rdst];
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
	private void jump(DalvikVM vm, Instruction inst, boolean seq) {
		if (seq) {
			if (vm.curr_jvm_stack == null) {
				vm.pc = Integer.MAX_VALUE;
				return;
			}
			vm.pc++;
			vm.curr_jvm_stack.pc++;
		} else {
			if (inst.extra == null) {
				Log.err(TAG, "unresolve dest address in goto: " + inst);
				return;
			}
			vm.curr_jvm_stack.pc = (int) inst.extra;
			vm.pc = vm.curr_jvm_stack.pc;
		}
	}

	/**
	 * @Title: invocation
	 * @Author: hao
	 * @Description: invocation helper
	 * @param @param vm
	 * @param @param mi
	 * @return void
	 * @throws
	 */
	public void invocation(DalvikVM vm, Instruction inst) {
		Object[] extra = (Object[]) inst.extra;
		MethodInfo mi = (MethodInfo) extra[0];
		// The register index referred by args
		int[] args = (int[]) extra[1];

		Object obj = null;
		Method method = null;
		try {
			// If applicable, directly use reflection to run the method,
			// the method is inside java.lang
			// Class<?> clazz = Class.forName(mi.myClass.toString());
			Log.debug(TAG, "" + vm.regs[args[0]].data);
			Class<?> clazz;
			// if (vm.regs[args[0]].data == null) {
			clazz = Class.forName(mi.myClass.toString());
			// } else {
			// clazz = vm.regs[args[0]].data.getClass();
			// }

			@SuppressWarnings("rawtypes")
			Class[] argsClass = new Class[mi.paramTypes.length];
			Object[] params = new Object[args.length - 1];
			
			if (mi.isConstructor()) {
				// use DvmObject to replace java.lang.Object
				if (!mi.myClass.toString().equals("java.lang.Object")) {
					// clazz = Class.forName(mi.myClass.toString());
					if (args.length == 1) {
						vm.regs[args[0]].data = clazz.newInstance();
						vm.regs[args[0]].type = mi.returnType;
						Log.debug(TAG, "new instance: " + vm.regs[args[0]].data);
					} else {

						getParams(vm, mi, args, argsClass, params);
						// overwrite previous declared dvmObj
						vm.regs[args[0]].data = clazz.getConstructor(argsClass)
								.newInstance(params);
						vm.regs[args[0]].type = mi.myClass;
						Log.debug(TAG, "return data: " + vm.regs[args[0]].data
								+ " " + vm.regs[args[0]].data.getClass());
					}
				}
			} else {

				Log.debug(TAG, "reflction class: " + clazz);

				obj = vm.regs[args[0]].data;
				if (obj == null) {
					obj = clazz.newInstance();
				}
				if (args.length == 1) {
					method = clazz.getDeclaredMethod(mi.name);
					vm.return_val_reg.data = method.invoke(obj);
				} else {
					getParams(vm, mi, args, argsClass, params);
					method = clazz.getDeclaredMethod(mi.name, argsClass);
					Log.debug(TAG, "caller obj: " + obj + ", from class: "
							+ obj.getClass().toString());
					// handle return val
					vm.return_val_reg.data = method.invoke(obj, params);
				}
				if (vm.return_val_reg.data != null) {
					vm.return_val_reg.type = ClassInfo
							.findOrCreateClass(vm.return_val_reg.data
									.getClass());
					Log.debug(TAG, "return data: " + vm.return_val_reg.data
							+ " ," + vm.return_val_reg.data.getClass());
				}
				Log.msg(TAG, "reflction invocation " + method);
				vm.plugin.method = method;
			}
			jump(vm, inst, true);
		} catch (java.lang.IllegalArgumentException e) {
			e.printStackTrace();
			Log.err(TAG, "obj " + obj + " " + method.getDeclaringClass());
		} catch (java.lang.NullPointerException e) {
			e.printStackTrace();
			Log.err(TAG, " null pointer ");
		} catch (java.lang.ClassNotFoundException | NoSuchMethodException e) {
			vm.plugin.method = null;
			Log.debug(TAG, "not a reflction invocation " + mi);
			vm.newStackFrame(mi);
			vm.setContext(new int[args.length]);
			for (int i = 0; i < args.length; i++) {
				vm.getContext()[i] = args[i];
				Log.debug(TAG, "arg " + vm.regs[vm.getContext()[i]].data);
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.err(TAG, "error in reflection");
		}

	}

	private void getParams(DalvikVM vm, MethodInfo mi, int[] args,
			Class<?>[] argsClass, Object[] params)
			throws ClassNotFoundException {
		// start from 1 to ignore "this"
		for (int i = 1; i < args.length; i++) {
			if (mi.paramTypes[i - 1].isPrimitive()) {
				Log.debug(TAG, "para type: " + mi.paramTypes[i - 1]);
				Object primitive = resolvePrimitive((PrimitiveInfo) vm.regs[args[i]].data);
				params[i - 1] = primitive;
				Class<?> argClass = primClasses.get(primitive.getClass());
				argsClass[i - 1] = argClass;
			} else {
				String argClass = mi.paramTypes[i - 1].toString();
				argsClass[i - 1] = Class.forName(argClass);
				Object argData = vm.regs[args[i]].data;

				if (argData == null) {
					Log.warn(TAG, "null in the " + i + "th arg, is "
							+ vm.regs[args[i]]);
					params[i - 1] = null;
				} else if (matchType(argData.getClass(), argsClass[i - 1])) {
					params[i - 1] = argData;
					// argData.getClass().getInterfaces()
				} else {
					// FIXME null
					Log.warn(TAG, "mismatch type! " + "real para type: "
							+ argData.getClass() + ", expected para type: "
							+ argsClass[i - 1]);
					params[i - 1] = null;
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

	public void invocation(DalvikVM vm, MethodInfo mi) {
		vm.thisObj = null;
		if (!mi.isStatic() && vm.getContext() == null) {
			vm.thisObj = new DVMObject(vm, mi.myClass);
			ClassInfo superClass = mi.myClass.getSuperClass();
			Class<?> superClazz;
			try {
				superClazz = Class.forName(superClass.toString());
				Log.debug(TAG, superClazz.getName());
				vm.thisObj.setSuperObj(superClazz.newInstance());
			} catch (ClassNotFoundException e) {
				vm.thisObj.setSuperObj(new DVMObject(vm, superClass));
				e.printStackTrace();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (java.lang.NoClassDefFoundError e) {
				e.printStackTrace();
			}

		}

		vm.newStackFrame(mi);
		if (!running) {
			run(vm);
		}
	}

	public void run(DalvikVM vm) {
		running = true;
		while (vm.curr_jvm_stack != null
				&& vm.pc < vm.curr_jvm_stack.method.insns.length) {
			Instruction insns = vm.curr_jvm_stack.method.insns[vm.pc];
			exec(vm, insns);
		}

		running = false;
		Log.msg(TAG, "RUN DONE!");
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
		Log.debug(TAG, "opcode: " + inst.opcode + " " + inst.opcode_aux);
		Log.debug(TAG, vm.pc + " " + inst);

		if (byteCodes.containsKey((int) inst.opcode)) {
			byteCodes.get((int) inst.opcode).func(vm, inst);
		} else if (auxByteCodes.containsKey((int) inst.opcode_aux)) {
			auxByteCodes.get((int) inst.opcode_aux).func(vm, inst);
		} else {
			Log.err(TAG, "unsupported opcode " + inst);
		}

		if (vm.plugin != null) {
			vm.plugin.runAnalysis(vm, inst, vm.plugin.getCurrRes());
			Log.debug(TAG, "tainted set: " + vm.plugin.currtRes);
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
		if (op1.isInteger()) {
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