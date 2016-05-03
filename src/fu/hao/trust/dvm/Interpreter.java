package fu.hao.trust.dvm;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import android.ShouldBeReplaced;
import android.content.Intent;
import fu.hao.trust.analysis.Plugin;
import fu.hao.trust.dvm.DalvikVM.Register;
import fu.hao.trust.dvm.DalvikVM.StackFrame;
import fu.hao.trust.data.MultiValueVar;
import fu.hao.trust.data.Results;
import fu.hao.trust.data.SymbolicVar;
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

	// Pass the execution of the instruction.
	boolean pass;

	// The nested class to implement singleton
	private static class SingletonHolder {
		private static final Interpreter instance = new Interpreter();
	}

	// Get THE instance
	public static final Interpreter v() {
		return SingletonHolder.instance;
	}

	private final String TAG = getClass().getSimpleName();

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
			Log.debug(
					getClass().toString(),
					"mov " + vm.getReg(inst.r0).getData() + " to "
							+ vm.getReg(inst.rdst).getData());
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
			vm.getReg(inst.rdst).setValue(inst.extra, inst.type);
			Log.debug(TAG, "data: " + vm.getReg(inst.rdst).getData() + " "
					+ vm.getReg(inst.rdst).getData().getClass());
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
				Log.err(TAG, "Return error");
			}

			Object data = vm.getReg(inst.r0).isUsed() ? vm.getReg(inst.r0)
					.getData() : null;
			ClassInfo type = vm.getReg(inst.r0).isUsed() ? vm.getReg(inst.r0)
					.getType() : null;
			// the caller stack of this invocation
			vm.getReturnReg().setValue(data, type);
			Log.bb(TAG, "Return data: " + vm.getReturnReg().getData());
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
				Log.warn(TAG, "NULL CallingCtx!");
				MethodInfo currMethod = vm.getCurrStackFrame().method;
				int startParam = 0;
				if (!currMethod.isStatic()) {
					startParam = 1;
					vm.getReg(params[0]).setValue(
							vm.getCurrStackFrame().getThisObj(),
							currMethod.myClass);
					vm.getCurrStackFrame().setThisReg(vm.getReg(params[0]));
					Log.debug(TAG, "args: " + vm.getReg(params[0]).getData());
				}

				int i = startParam;
				Log.bb(TAG, "Start param from " + i);
				for (ClassInfo paramClass : currMethod.paramTypes) {
					if (paramClass.isPrimitive()) {
						vm.getReg(params[i]).setValue(new Unknown(paramClass),
								paramClass);
					} else if (paramClass.isArray()) {
						// FIXME
					} else {
						try {
							Class<?> clazz = Class.forName(paramClass.fullName);
							vm.getReg(params[i]).setValue(clazz.newInstance(),
									paramClass);
						} catch (ClassNotFoundException e) {
							vm.getReg(params[i]).setValue(
									new DVMObject(vm, paramClass), paramClass);
						} catch (InstantiationException e) {
							vm.getReg(params[i]).setValue(null, paramClass);
							e.printStackTrace();
						} catch (IllegalAccessException e) {
							vm.getReg(params[i]).setValue(null, paramClass);
							e.printStackTrace();
						}
					}
					Log.debug(TAG,
							"args: "
									+ (vm.getReg(params[i]).isUsed() ? vm
											.getReg(params[i]).getData()
											: "Not avail"));
					i++;
				}
			} else {
				if (params.length != vm.getContext().length) {
					Log.err(TAG, "invalid ctx for invocation!");
					return;
				}
				MethodInfo currMethod = vm.getCurrStackFrame().method;

				int i = 0;
				for (Register argReg : vm.getContext()) {
					vm.getReg(params[i]).copy(argReg);
					Log.debug(TAG,
							"params: "
									+ (vm.getReg(params[i]).isUsed() ? vm
											.getReg(params[i]).getData()
											: "Empty!"));
					i++;
				}

				if (!currMethod.isStatic()) {
					vm.getCurrStackFrame().setThisObj(
							(DVMObject) vm.getReg(params[0]).getData());
					vm.getCurrStackFrame().setThisReg(vm.getReg(params[0]));
				}
			}
			jump(vm, inst, true);
		}
	}

	class OP_NEW_INSTANCE implements ByteCode {
		private final String TAG = getClass().getSimpleName();

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

			try {
				Class.forName(inst.type.toString());
				// vm.getReg(inst.rdst).setValue("Well, I will be born soon..",
				// inst.type);
				vm.getReg(inst.rdst).reset();
				// vm.getAssigned()[0] = -1; // not consider assigned here
				Log.bb(TAG, "Reflecable instance.");
			} catch (ClassNotFoundException e) {
				// Do not need to handle reflection type,
				// since <init> invocation will replace the newObj
				Object newObj = new DVMObject(vm, inst.type);
				Log.debug(TAG, "begin new object of " + inst.type + "created.");
				vm.getReg(inst.rdst).setValue(newObj, inst.type);
				Log.debug(TAG, "new object of " + inst.type + "created.");
			}
			jump(vm, inst, true);
		}
	}

	class OP_NEW_ARRAY implements ByteCode {
		private final String TAG = getClass().getSimpleName();

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
			int count;

			if (!vm.getReg(inst.r0).isUsed()
					|| vm.getReg(inst.r0).getData() == null
					|| vm.getReg(inst.r0).getData() instanceof Unknown) {
				count = 42;
				Log.warn(TAG, "Incorrect size!");
			} else {
				count = Integer.parseInt(vm.getReg(inst.r0).getData()
						.toString());
			}

			Object[] newArray;
			if (inst.type.isPrimitive()) {
				newArray = new PrimitiveInfo[count];
			} else {
				newArray = new Object[count];
			}

			vm.getReg(inst.rdst).setValue(newArray, inst.type);
			Log.debug(TAG, "A new array of " + inst.type + " in size " + count
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

			vm.getReturnReg().reset();
			try {
				// If applicable, directly use reflection to run the method,
				// the method is inside java.lang
				Class<?> clazz = Class.forName(mi.myClass.toString());
				Log.debug(TAG, "Reflction " + clazz);
				@SuppressWarnings("rawtypes")
				Class[] argsClass = new Class[mi.paramTypes.length];
				Method method;
				// TODO what is static (no this) but have multiple params
				// static invocation
				if (args.length == 0) {
					method = clazz.getDeclaredMethod(mi.name);
					vm.getReturnReg().setValue(method.invoke(null),
							mi.returnType);
				} else {
					Object[] params = new Object[args.length];
					// start from 0 since no "this"
					for (int i = 0; i < args.length; i++) {
						if (vm.getReg(args[i]).getData() == null) {
							continue;
						}
						if (mi.paramTypes[i].isPrimitive()) {
							Object primitive = resolvePrimitive(
									(PrimitiveInfo) vm.getReg(args[i])
											.getData(), mi.paramTypes[i]);
							params[i] = primitive;
							Class<?> argClass = primClasses.get(primitive
									.getClass());
							argsClass[i] = argClass;
						} else {
							// TODO use classLoader to check exists or not
							String argClass = mi.paramTypes[i].toString();
							argsClass[i] = Class.forName(argClass);
							params[i] = vm.getReg(args[i]).getData();
						}
					}
					method = clazz.getDeclaredMethod(mi.name, argsClass);
					vm.getReturnReg().setValue(method.invoke(null, params),
							mi.returnType);
				}

				Log.msg(TAG, "reflction invocation " + method);
				vm.setReflectMethod(method);
				jump(vm, inst, true);
			} catch (java.lang.ClassNotFoundException e) {
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
			if (inst.type.isConvertibleTo(vm.getReg(inst.rdst).getType())) {
				vm.getReg(inst.r0).setValue(42, ClassInfo.primitiveInt);
				Log.debug(TAG, "same type when instanceof");
			} else {
				vm.getReg(inst.r0).setValue(0, ClassInfo.primitiveInt);
				Log.debug(TAG, "NOT same type when instanceof");
			}

		}
	}

	class OP_A_ARRAY_LENGTH implements ByteCode {
		private final String TAG = getClass().getSimpleName();

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
			Object array = vm.getReg(inst.r0).getData();
			if (array != null && array.getClass().isArray()) {
				vm.getReg(inst.rdst).setValue(
						new PrimitiveInfo(Array.getLength(array)),
						ClassInfo.primitiveInt);
				Log.debug(TAG, "Array len " + Array.getLength(array));
			} else {
				Log.warn(TAG, "Not an array");
				vm.getReg(inst.rdst).setValue(
						new Unknown(ClassInfo.primitiveInt),
						ClassInfo.primitiveInt);
			}
		}
	}

	class OP_A_CHECKCAST implements ByteCode {
		private final String TAG = getClass().getSimpleName();

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
			if (vm.getReg(inst.rdst).getType() != null
					&& !inst.type.isConvertibleTo(vm.getReg(inst.rdst)
							.getType())) {
				if (vm.getReg(inst.rdst).getType().toString()
						.equals("java.lang.Object")
						|| inst.type.equals("java.lang.Object")) {
					Log.warn(TAG, "False alarm from PATDroid: Correct type");
				} else {
					Log.warn(TAG, "Not consistent type when cast! " + inst.type
							+ ", " + vm.getReg(inst.rdst).getType());
				}
			}
		}
	}

	class OP_MOV_RESULT implements ByteCode {
		private final String TAG = getClass().getSimpleName();

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
				Log.err(TAG, "Cannot identify res type!");
			}
			if (!vm.getReturnReg().isUsed()) {
				vm.getReturnReg().setValue(new Unknown(inst.type), inst.type);
			} else if (vm.getReturnReg().getData() instanceof SymbolicVar
					&& vm.getReturnReg().getType().isPrimitive()) {
				SymbolicVar var = (SymbolicVar) vm.getReturnReg().getData();
				var.setValue(PrimitiveInfo.fromObject(var.getValue()));
			} else {
				if (vm.getReturnReg().isUsed()
						&& vm.getReturnReg().getType() != null
						&& vm.getReturnReg().getType().isPrimitive()) {
					vm.getReturnReg().setValue(
							PrimitiveInfo.fromObject(vm.getReturnReg()
									.getData()), vm.getReturnReg().getType());
				}
			}

			vm.getReg(inst.rdst).copy(vm.getReturnReg());
			Log.bb(TAG, "Result data: " + vm.getReturnReg().getData());
			Log.debug(TAG, "Result data: " + vm.getReg(inst.rdst).getData()
					+ ", type: " + vm.getReg(inst.rdst).getType() + ", to "
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
		private final String TAG = getClass().getSimpleName();

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

			if (vm.getReg(inst.r0).getData() == null) {
				Log.warn(TAG, "ERROR: Null!");
				return;
			}

			Log.debug(TAG, "cast data " + vm.getReg(inst.r0).getData());
			if (!(vm.getReg(inst.r0).getData() instanceof PrimitiveInfo)) {
				vm.getReg(inst.r0).setValue(
						PrimitiveInfo.fromObject(vm.getReg(inst.r0).getData()),
						vm.getReg(inst.r0).getType());
			}
			PrimitiveInfo primitive = (PrimitiveInfo) vm.getReg(inst.r0)
					.getData();
			vm.getReg(inst.rdst).setValue(primitive.castTo(inst.type),
					inst.type);
		}
	}

	class OP_IF_EQ implements ByteCode {
		private final String TAG = getClass().getSimpleName();

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
		private final String TAG = getClass().getSimpleName();

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
		private final String TAG = getClass().getSimpleName();

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
		 * @Description: (𨃨 JavaDoc)
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
		 * @Description: (𨃨 JavaDoc)
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
			Object array = vm.getReg(inst.r0).isUsed() ? vm.getReg(inst.r0)
					.getData() : null;
			ClassInfo type = (ClassInfo) inst.extra;
			jump(vm, inst, true);

			if (array == null || !array.getClass().isArray()) {
				Object[] objs = new Object[42];

				for (int i = 0; i < objs.length; i++) {
					objs[i] = new Unknown(type);
				}
				ClassInfo atype = ClassInfo.primitiveVoid;
				if (vm.getReg(inst.r0).isUsed()
						&& vm.getReg(inst.r0).getType() != null) {
					atype = vm.getReg(inst.r0).getType();
				}

				vm.getReg(inst.r0).setValue(objs, atype);
				array = objs;
			}

			Log.bb(TAG, array);

			if (array.getClass().isArray()) {
				// dest reg
				Register rdst = vm.getReg(inst.rdst);
				// array reg
				// Object[] array = (Object[]) vm.getReg(inst.r0).getData();
				// index reg

				if (vm.getReg(inst.r1).isUsed()
						&& vm.getReg(inst.r1).getData() instanceof PrimitiveInfo) {
					int index = ((PrimitiveInfo) vm.getReg(inst.r1).getData())
							.intValue();
					type = inst.type;
					Object element = Array.get(array, index);
					// if (element.getClass().isPrimitive()) {
					Log.debug(TAG, "Elem: " + element + " at " + index + " of "
							+ array);
					if (type != null && type.isPrimitive()) {
						if (element instanceof PrimitiveInfo) {
							rdst.setValue(element, inst.type);
						} else {
							rdst.setValue(PrimitiveInfo.fromObject(element),
									inst.type);
						}
					} else {
						rdst.setValue(element, inst.type);// array[index];
					}
				} else {
					rdst.setValue(new Unknown(type), inst.type);
				}
			}

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
			jump(vm, inst, true);
			// dest reg
			Register rdst = vm.getReg(inst.rdst);

			if (rdst.getType() == null) {
				rdst.setValue(rdst.getData(), inst.type);
			}

			if (!vm.getReg(inst.r0).isUsed()
					|| !(vm.getReg(inst.r0).getData() instanceof Array)) {
				Object[] objs = new Object[42];
				for (int i = 0; i < objs.length; i++) {
					objs[i] = new Unknown(inst.type);
				}
				vm.getReg(inst.r0).setValue(objs, ClassInfo.primitiveVoid);
			}
			// array reg
			Object[] array = (Object[]) vm.getReg(inst.r0).getData();
			// index reg
			int index = ((PrimitiveInfo) vm.getReg(inst.r1).getData())
					.intValue();
			if (!rdst.getType().isConvertibleTo(inst.type)) {
				Log.warn(TAG, "inconsistent type " + inst);
				return;
			}
			Log.debug(TAG, "data: " + rdst.getData() + " array: " + array);
			array[index] = rdst.getData();
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
			Log.debug(TAG, "data " + vm.getReg(inst.r0).getData());
			if (vm.getReg(inst.r0).getData() instanceof Unknown) {
				Log.warn(TAG, "Unknown primitive data found.");
				return;
			}
			PrimitiveInfo op0 = (PrimitiveInfo) vm.getReg(inst.r0).getData();
			PrimitiveInfo op1;
			if (inst.r1 != -1) {
				op1 = (PrimitiveInfo) vm.getReg(inst.r1).getData();
			} else {
				op1 = (PrimitiveInfo) inst.extra;
			}

			if (op0 == null || op1 == null) {
				Log.warn(TAG, "Unknown primitive data found.");
				return;
			}
			Register rdst = vm.getReg(inst.rdst);
			ClassInfo type;
			if (op1.isInteger()) {
				type = ClassInfo.primitiveInt;
				int res = op0.intValue() + op1.intValue();
				rdst.setValue(new PrimitiveInfo(res), type);
			} else if (op1.isLong()) {
				type = ClassInfo.primitiveLong;
				long res = op0.longValue() + op1.longValue();
				rdst.setValue(new PrimitiveInfo(res), type);
			} else if (op1.isFloat()) {
				type = ClassInfo.primitiveFloat;
				float res = op0.floatValue() + op1.floatValue();
				rdst.setValue(new PrimitiveInfo(res), type);
			} else if (op1.isDouble()) {
				type = ClassInfo.primitiveFloat;
				double res = op0.floatValue() + op1.floatValue();
				rdst.setValue(new PrimitiveInfo(res), type);
			}
		}
	}

	class OP_A_SUB implements ByteCode {
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			PrimitiveInfo op0 = (PrimitiveInfo) vm.getReg(inst.r0).getData();
			PrimitiveInfo op1;
			if (inst.r1 != -1) {
				op1 = (PrimitiveInfo) vm.getReg(inst.r1).getData();
			} else {
				op1 = (PrimitiveInfo) inst.extra;
			}

			Register rdst = vm.getReg(inst.rdst);
			ClassInfo type;
			if (op1.isInteger()) {
				type = ClassInfo.primitiveInt;
				int res = op0.intValue() - op1.intValue();
				rdst.setValue(new PrimitiveInfo(res), type);
			} else if (op1.isLong()) {
				type = ClassInfo.primitiveLong;
				long res = op0.longValue() - op1.longValue();
				rdst.setValue(new PrimitiveInfo(res), type);
			} else if (op1.isFloat()) {
				type = ClassInfo.primitiveFloat;
				float res = op0.floatValue() - op1.floatValue();
				rdst.setValue(new PrimitiveInfo(res), type);
			} else if (op1.isDouble()) {
				type = ClassInfo.primitiveFloat;
				double res = op0.floatValue() - op1.floatValue();
				rdst.setValue(new PrimitiveInfo(res), type);
			}
		}
	}

	class OP_A_MUL implements ByteCode {
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			PrimitiveInfo op0 = (PrimitiveInfo) vm.getReg(inst.r0).getData();
			PrimitiveInfo op1;
			if (inst.r1 != -1) {
				op1 = (PrimitiveInfo) vm.getReg(inst.r1).getData();
			} else {
				op1 = (PrimitiveInfo) inst.extra;
			}
			Register rdst = vm.getReg(inst.rdst);

			Object obj = resolvePrimitive(op1, op1.getKind());
			ClassInfo type;
			Log.debug(TAG, "" + obj.getClass());
			if (op1.isInteger()) {
				type = ClassInfo.primitiveInt;
				int res = op0.intValue() * op1.intValue();
				rdst.setValue(new PrimitiveInfo(res), type);
			} else if (op1.isLong()) {
				Log.debug(TAG, "here mul long ");
				type = ClassInfo.primitiveLong;
				double res = op0.longValue() * op1.longValue();
				Log.debug(TAG, "" + res);
				rdst.setValue(
						new PrimitiveInfo(op0.longValue() * op1.longValue()),
						type);
			} else if (op1.isFloat()) {
				type = ClassInfo.primitiveFloat;
				float res = op0.floatValue() * op1.floatValue();
				rdst.setValue(new PrimitiveInfo(res), type);
			} else if (op1.isDouble()) {
				Log.debug(TAG, "here mul ");
				type = ClassInfo.primitiveFloat;
				double res = op0.floatValue() * op1.floatValue();
				rdst.setValue(new PrimitiveInfo(res), type);
			}
			Log.debug(TAG, "end mul ");
		}
	}

	class OP_A_DIV implements ByteCode {
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			PrimitiveInfo op0 = (PrimitiveInfo) vm.getReg(inst.r0).getData();
			PrimitiveInfo op1;
			if (inst.r1 != -1) {
				op1 = (PrimitiveInfo) vm.getReg(inst.r1).getData();
			} else {
				op1 = (PrimitiveInfo) inst.extra;
			}
			Register rdst = vm.getReg(inst.rdst);
			ClassInfo type;
			if (op1.isInteger()) {
				type = ClassInfo.primitiveInt;
				int res = op0.intValue() / op1.intValue();
				rdst.setValue(new PrimitiveInfo(res), type);
			} else if (op1.isLong()) {
				type = ClassInfo.primitiveLong;
				long res = op0.longValue() / op1.longValue();
				rdst.setValue(new PrimitiveInfo(res), type);
			} else if (op1.isFloat()) {
				type = ClassInfo.primitiveFloat;
				float res = op0.floatValue() / op1.floatValue();
				rdst.setValue(new PrimitiveInfo(res), type);
			} else if (op1.isDouble()) {
				type = ClassInfo.primitiveFloat;
				double res = op0.floatValue() / op1.floatValue();
				rdst.setValue(new PrimitiveInfo(res), type);
			}
		}
	}

	class OP_A_REM implements ByteCode {
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			PrimitiveInfo op0 = (PrimitiveInfo) vm.getReg(inst.r0).getData();
			PrimitiveInfo op1;
			if (inst.r1 != -1) {
				op1 = (PrimitiveInfo) vm.getReg(inst.r1).getData();
			} else {
				op1 = (PrimitiveInfo) inst.extra;
			}
			Register rdst = vm.getReg(inst.rdst);
			ClassInfo type;
			if (op1.isInteger()) {
				type = ClassInfo.primitiveInt;
				int res = op0.intValue() % op1.intValue();
				rdst.setValue(new PrimitiveInfo(res), type);
			} else if (op1.isLong()) {
				type = ClassInfo.primitiveLong;
				long res = op0.longValue() % op1.longValue();
				rdst.setValue(new PrimitiveInfo(res), type);
			} else if (op1.isFloat()) {
				type = ClassInfo.primitiveFloat;
				float res = op0.floatValue() % op1.floatValue();
				rdst.setValue(new PrimitiveInfo(res), type);
			} else if (op1.isDouble()) {
				type = ClassInfo.primitiveFloat;
				double res = op0.floatValue() % op1.floatValue();
				rdst.setValue(new PrimitiveInfo(res), type);
			}

		}
	}

	class OP_A_AND implements ByteCode {
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			PrimitiveInfo op0 = (PrimitiveInfo) vm.getReg(inst.r0).getData();
			PrimitiveInfo op1;
			if (inst.r1 != -1) {
				op1 = (PrimitiveInfo) vm.getReg(inst.r1).getData();
			} else {
				op1 = (PrimitiveInfo) inst.extra;
			}
			Register rdst = vm.getReg(inst.rdst);
			ClassInfo type;
			if (op1.isInteger()) {
				type = ClassInfo.primitiveInt;
				int res = op0.intValue() & op1.intValue();
				rdst.setValue(new PrimitiveInfo(res), type);
			} else if (op1.isLong()) {
				type = ClassInfo.primitiveLong;
				long res = op0.longValue() & op1.longValue();
				rdst.setValue(new PrimitiveInfo(res), type);
			} else {
				Log.err(TAG, "invalid type! " + inst);
			}
		}
	}

	class OP_A_NOT implements ByteCode {
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			// FIXME not sure correct
			PrimitiveInfo op0 = (PrimitiveInfo) vm.getReg(inst.r0).getData();
			Register rdst = vm.getReg(inst.rdst);
			ClassInfo type;
			if (op0.isInteger()) {
				type = ClassInfo.primitiveInt;
				int res = ~op0.intValue();
				rdst.setValue(new PrimitiveInfo(res), type);
			} else if (op0.isLong()) {
				type = ClassInfo.primitiveLong;
				long res = ~op0.longValue();
				rdst.setValue(new PrimitiveInfo(res), type);
			}
		}
	}

	class OP_A_NEG implements ByteCode {
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			PrimitiveInfo op0 = (PrimitiveInfo) vm.getReg(inst.r0).getData();
			Register rdst = vm.getReg(inst.rdst);
			ClassInfo type;
			if (op0.isInteger()) {
				type = ClassInfo.primitiveInt;
				int res = -op0.intValue();
				rdst.setValue(new PrimitiveInfo(res), type);
			} else if (op0.isLong()) {
				type = ClassInfo.primitiveLong;
				long res = -op0.longValue();
				rdst.setValue(new PrimitiveInfo(res), type);
			} else if (op0.isFloat()) {
				type = ClassInfo.primitiveFloat;
				float res = -op0.floatValue();
				rdst.setValue(new PrimitiveInfo(res), type);
			} else if (op0.isDouble()) {
				type = ClassInfo.primitiveFloat;
				double res = -op0.floatValue();
				rdst.setValue(new PrimitiveInfo(res), type);
			}
		}
	}

	class OP_A_XOR implements ByteCode {
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			PrimitiveInfo op0 = (PrimitiveInfo) vm.getReg(inst.r0).getData();
			PrimitiveInfo op1;
			if (inst.r1 != -1) {
				op1 = (PrimitiveInfo) vm.getReg(inst.r1).getData();
			} else {
				op1 = (PrimitiveInfo) inst.extra;
			}
			Register rdst = vm.getReg(inst.rdst);
			ClassInfo type;
			if (op1.isInteger()) {
				type = ClassInfo.primitiveInt;
				int res = op0.intValue() ^ op1.intValue();
				rdst.setValue(new PrimitiveInfo(res), type);
			} else if (op1.isLong()) {
				type = ClassInfo.primitiveLong;
				long res = op0.longValue() ^ op1.longValue();
				rdst.setValue(new PrimitiveInfo(res), type);
			}
		}
	}

	class OP_A_OR implements ByteCode {
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			PrimitiveInfo op0 = (PrimitiveInfo) vm.getReg(inst.r0).getData();
			PrimitiveInfo op1;
			if (inst.r1 != -1) {
				op1 = (PrimitiveInfo) vm.getReg(inst.r1).getData();
			} else {
				op1 = (PrimitiveInfo) inst.extra;
			}
			Register rdst = vm.getReg(inst.rdst);
			ClassInfo type;
			if (op1.isInteger()) {
				type = ClassInfo.primitiveInt;
				int res = op0.intValue() | op1.intValue();
				rdst.setValue(new PrimitiveInfo(res), type);
			} else if (op1.isLong()) {
				type = ClassInfo.primitiveLong;
				long res = op0.longValue() | op1.longValue();
				rdst.setValue(new PrimitiveInfo(res), type);
			}
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
			PrimitiveInfo op0 = (PrimitiveInfo) vm.getReg(inst.r0).getData();
			PrimitiveInfo op1;
			if (inst.r1 != -1) {
				op1 = (PrimitiveInfo) vm.getReg(inst.r1).getData();
			} else {
				op1 = (PrimitiveInfo) inst.extra;
			}
			Register rdst = vm.getReg(inst.rdst);
			ClassInfo type;
			if (op0.isInteger()) {
				type = ClassInfo.primitiveInt;
				int res = op0.intValue() << op1.intValue();
				rdst.setValue(new PrimitiveInfo(res), type);
			} else if (op0.isLong()) {
				type = ClassInfo.primitiveLong;
				long res;
				if (op1.isInteger()) {
					res = op0.longValue() << op1.intValue();
				} else {
					res = op0.longValue() << op1.longValue();
				}
				rdst.setValue(new PrimitiveInfo(res), type);
			}
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
			PrimitiveInfo op0 = (PrimitiveInfo) vm.getReg(inst.r0).getData();
			PrimitiveInfo op1;
			if (inst.r1 != -1) {
				op1 = (PrimitiveInfo) vm.getReg(inst.r1).getData();
			} else {
				op1 = (PrimitiveInfo) inst.extra;
			}
			Register rdst = vm.getReg(inst.rdst);
			ClassInfo type;
			if (op0.isInteger()) {
				type = ClassInfo.primitiveInt;
				int res = op0.intValue() >> op1.intValue();
				rdst.setValue(new PrimitiveInfo(res), type);
			} else if (op0.isLong()) {
				type = ClassInfo.primitiveLong;
				long res;
				if (op1.isInteger()) {
					res = op0.longValue() >> op1.intValue();
				} else {
					res = op0.longValue() >> op1.longValue();
				}
				rdst.setValue(new PrimitiveInfo(res), type);
			}
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
			PrimitiveInfo op0 = (PrimitiveInfo) vm.getReg(inst.r0).getData();
			PrimitiveInfo op1;
			if (inst.r1 != -1) {
				op1 = (PrimitiveInfo) vm.getReg(inst.r1).getData();
			} else {
				op1 = (PrimitiveInfo) inst.extra;
			}
			Register rdst = vm.getReg(inst.rdst);
			ClassInfo type;
			if (op0.isInteger()) {
				type = ClassInfo.primitiveInt;
				int res = op0.intValue() >>> op1.intValue();
				rdst.setValue(new PrimitiveInfo(res), type);
			} else if (op0.isLong()) {
				type = ClassInfo.primitiveLong;
				long res;
				if (op1.isInteger()) {
					res = op0.longValue() >>> op1.intValue();
				} else {
					res = op0.longValue() >>> op1.longValue();
				}
				rdst.setValue(new PrimitiveInfo(res), type);
			}
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
			ClassInfo type = ClassInfo.primitiveInt;
			jump(vm, inst, true);

			if (op1 == null || op2 == null) {
				Log.warn(TAG, "CMP_Long error! null value");
				return;
			}

			if (op1.longValue() > op2.longValue()) {
				rdst.setValue(new PrimitiveInfo(1), type);
			} else if (op1.longValue() == op2.longValue()) {
				rdst.setValue(new PrimitiveInfo(0), type);
			} else {
				rdst.setValue(new PrimitiveInfo(-1), type);
			}

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
			if (!r0.getType().equals(r1.getType())) {
				Log.err(TAG, "incosistent type " + inst);
				return;
			}

			PrimitiveInfo op1 = (PrimitiveInfo) vm.getReg(inst.r0).getData();
			PrimitiveInfo op2 = (PrimitiveInfo) vm.getReg(inst.r1).getData();
			Register rdst = vm.getReg(inst.rdst);
			ClassInfo type = ClassInfo.primitiveInt;
			// FIXME NaN handling
			if (r0.getType().equals(ClassInfo.primitiveFloat)) {
				if (op1.floatValue() > op2.floatValue()) {
					rdst.setValue(new PrimitiveInfo(1), type);
				} else if (op1.floatValue() == op2.floatValue()) {
					rdst.setValue(new PrimitiveInfo(0), type);
				} else {
					rdst.setValue(new PrimitiveInfo(-1), type);
				}
			} else if (r0.getType().equals(ClassInfo.primitiveDouble)) {
				if (op1.doubleValue() > op2.doubleValue()) {
					rdst.setValue(new PrimitiveInfo(1), type);
				} else if (op1.doubleValue() == op2.doubleValue()) {
					rdst.setValue(new PrimitiveInfo(0), type);
				} else {
					rdst.setValue(new PrimitiveInfo(-1), type);
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
			if (!r0.getType().equals(r1.getType())) {
				Log.err(TAG, "incosistent type " + inst);
				return;
			}

			PrimitiveInfo op1 = (PrimitiveInfo) vm.getReg(inst.r0).getData();
			PrimitiveInfo op2 = (PrimitiveInfo) vm.getReg(inst.r1).getData();
			Register rdst = vm.getReg(inst.rdst);
			ClassInfo type = ClassInfo.primitiveInt;
			// FIXME NaN handling
			if (r0.getType().equals(ClassInfo.primitiveFloat)) {
				if (op1.floatValue() > op2.floatValue()) {
					rdst.setValue(new PrimitiveInfo(1), type);
				} else if (op1.floatValue() == op2.floatValue()) {
					rdst.setValue(new PrimitiveInfo(0), type);
				} else {
					rdst.setValue(new PrimitiveInfo(-1), type);
				}
			} else if (r0.getType().equals(ClassInfo.primitiveDouble)) {
				if (op1.doubleValue() > op2.doubleValue()) {
					rdst.setValue(new PrimitiveInfo(1), type);
				} else if (op1.doubleValue() == op2.doubleValue()) {
					rdst.setValue(new PrimitiveInfo(0), type);
				} else {
					rdst.setValue(new PrimitiveInfo(-1), type);
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
			final String TAG = getClass().getSimpleName();
			@SuppressWarnings("unchecked")
			Pair<ClassInfo, String> pair = (Pair<ClassInfo, String>) inst.extra;

			try {
				Class<?> clazz = Class.forName(pair.first.toString());
				Field field = clazz.getDeclaredField(pair.second.toString());
				// TODO only support static field now
				ClassInfo type = ClassInfo.findOrCreateClass(vm.getReg(inst.r0)
						.getData().getClass());
				vm.getReg(inst.r0).setValue(field.get(clazz), type);
				Log.debug(TAG, "Refleciton " + vm.getReg(inst.r0).getData());
			} catch (Exception e) {
				ClassInfo owner = pair.first;
				String fieldName = pair.second;
				// FieldInfo statFieldInfo = new FieldInfo(pair.first,
				// pair.second);
				// Log.debug(TAG, "sget " + statFieldInfo.getFieldType());
				DVMClass dvmClass = vm.getClass(owner);
				ClassInfo fieldType = owner.getStaticFieldType(fieldName);
				vm.getReg(inst.r0).setValue(dvmClass.getStatField(fieldName),
						fieldType);
				Log.debug(TAG, "Sget " + vm.getReg(inst.r0).getData()
						+ ", from " + dvmClass);
				Log.debug(TAG,
						"Expect sget " + fieldName + ", a "
								+ vm.getReg(inst.r0).getType() + " from "
								+ owner);
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
			if (vm.getReg(inst.r0).getType() instanceof ClassInfo
					&& !vm.getReg(inst.r0).getType().isConvertibleTo(fieldType)) {
				Log.warn(TAG, "Type inconsistent! "
						+ vm.getReg(inst.r0).getType() + " " + fieldType);
			}
			vm.getAssigned()[0] = dvmClass;
			vm.getAssigned()[1] = fieldName;
			vm.getAssigned()[2] = vm.getReg(inst.r0).getData();
			dvmClass.setStatField(fieldName, vm.getReg(inst.r0).getData());
			Log.debug(TAG, "expect sput " + fieldName + " from " + owner);
			Log.debug(TAG, "real sput " + vm.getReg(inst.r0).getData()
					+ " from " + dvmClass);
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
			Object obj = vm.getReg(inst.r0).getData();
			Log.bb(TAG, "obj " + obj);
			Log.bb(TAG, "fieldinfo " + fieldInfo);
			if (obj instanceof DVMObject) {
				DVMObject dvmObj = (DVMObject) obj;
				ClassInfo type = fieldInfo.getFieldType();

				if (dvmObj.getFieldObj(fieldInfo) == null) {
					if (fieldInfo.fieldName.equals("this$0")) {
						dvmObj.setField(fieldInfo, vm.callbackOwner);
					} else {
						dvmObj.setField(fieldInfo,
								new Unknown(fieldInfo.getFieldType()));
					}
				}

				vm.getReg(inst.r1)
						.setValue(dvmObj.getFieldObj(fieldInfo), type);

				Log.debug(TAG, "Get data: " + vm.getReg(inst.r1).getData());
			} else {
				Log.err(TAG, "obj is not a DVMObject!");
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
			Object obj = vm.getReg(inst.r0).getData();
			Log.bb(TAG, "Target obj " + obj);
			if (obj instanceof DVMObject) {
				DVMObject dvmObj = (DVMObject) obj;
				vm.getAssigned()[0] = dvmObj;
				vm.getAssigned()[1] = fieldInfo;
				vm.getAssigned()[2] = vm.getReg(inst.r1).getData();
				dvmObj.setField(fieldInfo, vm.getReg(inst.r1).getData());
				Log.msg(TAG, "Put data " + dvmObj.getFieldObj(fieldInfo)
						+ " to the field of " + dvmObj);
			} else {
				Log.err(TAG, "obj is not a DVMObject!");
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
		 * @Description: (�? JavaDoc)
		 * @param vm
		 * @param inst
		 * @see fu.hao.trust.dvm.ByteCode#func(fu.hao.trust.dvm.DalvikVM,
		 *      patdroid.dalvik.Instruction)
		 */
		@Override
		public void func(DalvikVM vm, Instruction inst) {
			vm.getReturnReg().reset();
			jump(vm, inst, true);
			Log.msg(TAG, "Cannot resolve the invocation");
		}
	}

	class OP_CMP implements ByteCode {
		private final String TAG = getClass().getSimpleName();

		/**
		 * @Title: func
		 * @Description: (�? JavaDoc)
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

			SymbolicVar u0 = null, u1 = null;
			Log.debug(TAG, "r0 data " + (r0.isUsed() ? r0.getData() : null));
			if (r0.getData() == null) {
				Log.warn(TAG, "Null operator found!");
				r0.setValue(new Unknown(r0.getType()), r0.getType());
			}

			if (inst.r1 != -1 && r1.getData() == null) {
				Log.warn(TAG, "Null operator found!");
				r1.setValue(new Unknown(r1.getType()), r1.getType());
			}

			if (r0.getData() instanceof SymbolicVar) {
				u0 = (SymbolicVar) r0.getData();
				// TODO
				// u0.addConstriant(vm, inst);
			}

			if (inst.r1 != -1 && r1.getData() instanceof SymbolicVar) {
				u1 = (SymbolicVar) r1.getData();
				// TODO
				// u1.addConstriant(vm, inst);
			}

			if (u0 == null && u1 == null) {
				auxByteCodes.get((int) inst.opcode_aux).func(vm, inst);
			}
		}
	}

	class OP_ARITHETIC implements ByteCode {
		private final String TAG = getClass().getSimpleName();

		@Override
		public void func(DalvikVM vm, Instruction inst) {
			jump(vm, inst, true);

			// If operands contains instance of Unknown, directly set the res
			// reg (r0) as unknowon
			Unknown op = null;

			if (inst.r0 != -1
					&& vm.getReg(inst.r0).getData() instanceof Unknown) {
				op = (Unknown) vm.getReg(inst.r0).getData();
				op.addLastArith(inst);
			} else if (inst.r1 != -1
					&& vm.getReg(inst.r1).getData() instanceof Unknown) {
				op = (Unknown) vm.getReg(inst.r1).getData();
				op.addLastArith(inst);
				ClassInfo type = vm.getReg(inst.r1).getType();
				op = new Unknown(type);
				op.addLastArith(inst);
				vm.getReg(inst.r0).setValue(op, type);
				Log.warn(TAG, "Unknown found! " + op);
			} else if (inst.r0 != -1 && vm.getReg(inst.r0).getData() == null
					|| inst.r1 != -1 && vm.getReg(inst.r1).getData() == null) {
				ClassInfo type = vm.getReg(inst.r1).getType();
				op = new Unknown(type);
				op.addLastArith(inst);
				vm.getReg(inst.r0).setValue(op, type);
			} else {
				auxByteCodes.get((int) inst.opcode_aux).func(vm, inst);
			}

			if (inst.opcode_aux == Instruction.OP_A_ARRAY_LENGTH) {
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

		PrimitiveInfo op1;

		if (r0.getData() == null) {
			op1 = new PrimitiveInfo(0);
		} else {
			if (r0.getData() instanceof PrimitiveInfo) {
				op1 = (PrimitiveInfo) r0.getData();
			} else {
				op1 = new PrimitiveInfo(42);
			}
		}

		PrimitiveInfo op2;
		if (flagZ) {
			op2 = new PrimitiveInfo(0);
		} else {
			op2 = (PrimitiveInfo) vm.getReg(inst.r1).getData();
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
				return;
			} else {
				vm.setPC(vm.getPC() + 1);
			}
		} else {
			if (inst.extra == null) {
				Log.err(TAG, "unresolve dest address in goto: " + inst);
			}
			vm.setPC((int) inst.extra);
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
		vm.getReturnReg().reset();

		Object[] extra = (Object[]) inst.extra;
		MethodInfo mi = (MethodInfo) extra[0];
		// The register index referred by args
		int[] args = (int[]) extra[1];
		final String TAG = getClass().getSimpleName();
		Object thisInstance = null;
		Method method = null;
		retrieveIntent(vm, mi, args);
		try {
			// If applicable, directly use reflection to run the method,
			// the method is inside java.lang
			// Class<?> clazz = Class.forName(mi.myClass.toString());
			if (vm.getReg(args[0]).isUsed()) {
				Log.msg(TAG, "arg0 obj: " + vm.getReg(args[0]).getData());
			} else {
				Log.warn(TAG, "arg0 is null!");
			}
			Class<?> clazz;
			clazz = Class.forName(mi.myClass.toString());

			@SuppressWarnings("rawtypes")
			Class[] argsClass = new Class[mi.paramTypes.length];
			Object[] params = new Object[args.length - 1];
			boolean normalArg = true;

			if (noInvokeList.contains(mi.name)
					|| noInvokeList.contains(mi.myClass.fullName)) {
				normalArg = false;
			}

			// If args contains a symbolic var, directly set the return val as a
			// symbolic var.
			if (mi.isConstructor()) {
				// use DvmObject to replace java.lang.Object
				if (!mi.myClass.toString().equals("java.lang.Object")) {
					// clazz = Class.forName(mi.myClass.toString());
					if (args.length == 1) {
						vm.getReg(args[0]).setValue(clazz.newInstance(),
								mi.returnType);
						Log.debug(TAG, "Init instance: "
								+ vm.getReg(args[0]).getData() + ", "
								+ vm.getReg(args[0]).getData().getClass());
					} else {
						boolean narg = getParams(vm, mi, args, argsClass,
								params);
						if (normalArg) {
							normalArg = narg;
						}
						Object instance;
						if (!normalArg) {
							instance = new Unknown(mi.returnType);
						} else {
							instance = clazz.getConstructor(argsClass)
									.newInstance(params);
						}
						// Overwrite previous declared dvmObj
						vm.getReg(args[0]).setValue(instance, mi.myClass);
						Log.debug(TAG, "Init instance: "
								+ vm.getReg(args[0]).getData() + " "
								+ vm.getReg(args[0]).getData().getClass());
					}
				}
			} else {
				Log.debug(TAG, "Reflction class: " + clazz);
				thisInstance = vm.getReg(args[0]).isUsed() ? vm.getReg(args[0])
						.getData() : null;
				if (thisInstance instanceof MultiValueVar) {
					// FIXME Herustic, should really handle loop
					if (isNoInvoke2(mi)) {
						Log.msg(TAG, "Found noInvoke2 " + mi);
						normalArg = false;
					}

					if (thisInstance instanceof SymbolicVar) {
						thisInstance = ((SymbolicVar) thisInstance).getValue();
					} else {
						thisInstance = ((MultiValueVar) thisInstance)
								.getLastVal();
					}
				}

				if (normalArg && thisInstance == null
						|| thisInstance instanceof DVMObject
						&& !DVMObject.class.equals(clazz)) {
					thisInstance = clazz.newInstance();
				}

				// clazz = obj.getClass();
				if (args.length == 1) {
					method = clazz.getDeclaredMethod(mi.name);
					// When method is a memeber of noInvoke, do not really
					// invoke it
					if (!normalArg) {
						vm.getReturnReg().setValue(new Unknown(mi.returnType),
								mi.returnType);
						Log.warn(TAG, "Found noInvokeMethod " + method);
					} else {
						vm.getReturnReg().setValue(method.invoke(thisInstance),
								mi.returnType);
					}
				} else {
					boolean narg = getParams(vm, mi, args, argsClass, params);
					if (normalArg) {
						normalArg = narg;
					}
					method = clazz.getDeclaredMethod(mi.name, argsClass);
					// handle return val
					if (normalArg) {
						Log.debug(TAG, "Caller obj: " + thisInstance
								+ ", from class: "
								+ thisInstance.getClass().toString());
						vm.getReturnReg().setValue(
								method.invoke(thisInstance, params),
								mi.returnType);
					} else {
						vm.getReturnReg().setValue(new Unknown(mi.returnType),
								mi.returnType);
					}
				}
				if (vm.getReturnReg().getData() != null) {
					Log.debug(TAG, "Return data: "
							+ vm.getReturnReg().getData() + " ,"
							+ vm.getReturnReg().getData().getClass());
				}
				Log.msg(TAG, "Reflction invocation " + method);
			}

			if (!normalArg) {
				Log.warn(TAG, "Abnormal args, contain abstract value.");
			}
			vm.setReflectMethod(method);

			if (vm.getReturnReg().isUsed()
					&& vm.getReturnReg().getData() instanceof Unknown
					&& mi.returnType.toString().contains("String")) {
				((Unknown) vm.getReturnReg().getData())
						.addConcreteVal("Unknown");
			}

			if (vm.getReturnReg().isUsed()
					&& vm.getReturnReg().getData() instanceof ShouldBeReplaced) {
				vm.getReturnReg().setValue(new DVMObject(vm, mi.returnType),
						mi.returnType);
			}
			jump(vm, inst, true);
		} catch (java.lang.IllegalArgumentException e) {
			e.printStackTrace();
			jump(vm, inst, true);
		} catch (java.lang.NullPointerException e) {
			e.printStackTrace();
			Log.err(TAG, " null pointer ");
		} catch (java.lang.ClassNotFoundException
				| java.lang.NoClassDefFoundError e) {
			Log.debug(TAG, "Not a reflction invocation " + mi);
			invocation(vm, mi, inst, args);
		} catch (java.lang.InstantiationException e) {
			Log.warn(TAG, e.getMessage());
			vm.getReg(args[0]).setValue(new Unknown(mi.myClass), mi.myClass);
			jump(vm, inst, true);
		} catch (Exception e) {
			e.printStackTrace();

			Log.warn(TAG, "Error in reflection: " + e.getMessage());
			jump(vm, inst, true);
		}

	}

	private boolean isNoInvoke2(MethodInfo mi) {
		for (String mname : noInvokeList2) {
			if (mi.toString().contains(mname)) {
				return true;
			}
		}

		return false;
	}

	private void retrieveIntent(DalvikVM vm, MethodInfo mi, int[] args) {
		if (mi.toString().contains("sendBroadcast")) {
			Log.bb(TAG, "sendBroadCast found");
			for (int i = 1; i < args.length; i++) {
				if (vm.getReg(args[i]).isUsed()
						&& vm.getReg(args[i]).getData() instanceof Intent) {
					Log.warn(TAG, "Intent found!"
							+ vm.getReg(args[i]).getData());
					Results.addIntent((Intent) vm.getReg(args[i]).getData());
				}
			}
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
	private boolean getParams(DalvikVM vm, MethodInfo mi, int[] args,
			Class<?>[] argsClass, Object[] params)
			throws ClassNotFoundException {
		// Start from 1 to ignore "this"
		for (int i = 1; i < args.length; i++) {
			Object data = vm.getReg(args[i]).isUsed() ? vm.getReg(args[i])
					.getData() : null;
			Log.bb(TAG, "arg" + i + "@reg" + args[i] + ": " + data);
			if (mi.paramTypes[i - 1].isPrimitive()) {
				Log.debug(TAG, "Expected para type: " + mi.paramTypes[i - 1]);
				Object primitive = resolvePrimitive(data, mi.paramTypes[i - 1]);
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
				Object argData = vm.getReg(args[i]).getData();

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
					Log.warn(TAG, "Mismatch type! arg " + i
							+ ", real para type: " + argData.getClass()
							+ ", expected para type: " + argsClass[i - 1]);
					if (argData instanceof SymbolicVar) {
						SymbolicVar bidirVar = (SymbolicVar) argData;
						params[i - 1] = bidirVar.getValue();
						Log.debug(TAG, "Found symbolic var " + params[i - 1]);
						// To correctly show the URL, depress return val through
						// a list.
						if (isNoInvoke2(mi)) {
							return false;
						}
					} else if (argData instanceof MultiValueVar) {
						params[i - 1] = null;
						return false;
					}
				}
			}
		}

		return true;
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
	 * @param vm
	 * @param mi
	 * @return void
	 * @throws
	 */
	public void invocation(DalvikVM vm, MethodInfo mi, Instruction inst,
			int[] args) {
		vm.getReturnReg().reset();

		if (noInvokeList.contains(mi.name)
				|| noInvokeList.contains(mi.myClass.fullName)
				|| mi.myClass.fullName.startsWith("android.support")) {
			vm.getReturnReg().setValue(new Unknown(mi.returnType),
					mi.returnType);
			Log.warn(TAG, "Found noInvokeMethod " + mi);
			jump(vm, inst, true);
			return;
		}

		// Create a new stack frame and push it to the stack.
		if (!mi.isStatic()) {
			if (vm.getReg(args[0]).getData() instanceof Unknown
					|| vm.getReg(args[0]).getData() == null) {
				Log.warn(TAG, "NULL \"THIS\" INSTANCE!");
				jump(vm, inst, true);
				return;
			}

			DVMObject thisObj = (DVMObject) vm.getReg(args[0]).getData();
			if (((int) inst.opcode_aux != 0x0C) && !mi.isConstructor()
					&& thisObj.getType().getSuperClass().equals(mi.myClass)) {
				mi = thisObj.getType().findMethodsHere(mi.name)[0];
			}
		}

		if (args != null) {
			vm.setCallContext(args);
		}

		vm.newStackFrame(mi);
	}

	public void runMethod(DalvikVM vm, MethodInfo mi) {
		// Create a new stack frame and push it to the stack.
		StackFrame stackFrame = vm.newStackFrame(mi);
		if (mi.isStatic()) {
			Log.bb(TAG, "Entry method is static!");
			stackFrame.setThisObj(null);
		} else {
			if (vm.getChainThisObj() == null) {
				Log.msg(TAG, "New chain obj");
				stackFrame.setThisObj(new DVMObject(vm, mi.myClass));
				vm.setChainThisObj(stackFrame.getThisObj());
			} else {
				stackFrame.setThisObj(vm.getChainThisObj());
			}
		}

		if (!running) {
			Log.msg(TAG, "RUN BEGIN " + mi);
			run(vm);
		}
	}

	public void run(DalvikVM vm) {
		Log.msg(TAG, "RUN BEGIN");
		running = true;
		String mname = vm.getCurrStackFrame().method.name;
		if (vm.getPC() < 0) {
			vm.setPC(0);
		}
		while (vm.getCurrStackFrame() != null
				&& vm.getCurrStackFrame().method != null
				&& vm.getPC() < vm.getCurrStackFrame().method.insns.length) {
			if (vm.getPC() < 0) {
				vm.setPC(0);
			}
			Instruction insns = vm.getCurrStackFrame().method.insns[vm.getPC()];
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

	// The API calls that creates
	Set<String> libObjList = new HashSet<>();

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
		byteCodes.put(0x0D, new OP_ARITHETIC());
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

		noInvokeList = new HashSet<>();
		noInvokeList.add("connect");
		noInvokeList.add("getResponseCode");
		noInvokeList.add("getInputStream");
		noInvokeList.add("java.io.FileInputStream");
		noInvokeList.add("java.io.InputStreamReader");
		noInvokeList.add("java.io.BufferedReader");
		noInvokeList.add("java.io.File");
		noInvokeList.add("android.support");

		noInvokeList2 = new HashSet<>();
		noInvokeList2.add("equals");
		noInvokeList2.add("split");
		noInvokeList2.add("index");
		noInvokeList2.add("substring");
		noInvokeList2.add("trim");
		// noInvokeList2.add("append");
		// noInvokeList2.add("toString");
	}

	public void exec(DalvikVM vm, Instruction inst) {
		Log.msg(TAG, "\n");
		vm.setNowPC(vm.getPC());
		inst.setIndex(vm.getNowPC());
		Log.msg(TAG, inst + " at " + vm.getCurrStackFrame().method);
		// Reset
		vm.getAssigned()[0] = -1;
		vm.setReflectMethod(null);
		if (!vm.getPluginManager().isEmpty() && vm.getCurrStackFrame() != null) {
			vm.getPluginManager().preprossing(vm, inst);
		}

		if (!pass) {
			if (byteCodes.containsKey((int) inst.opcode)) {
				byteCodes.get((int) inst.opcode).func(vm, inst);
			} else if (auxByteCodes.containsKey((int) inst.opcode_aux)) {
				auxByteCodes.get((int) inst.opcode_aux).func(vm, inst);
			} else {
				Log.err(TAG, "Unsupported opcode " + inst);
			}

			if (!vm.getPluginManager().isEmpty()
					&& vm.getCurrStackFrame() != null) {
				vm.getPluginManager().runAnalysis(vm, inst);

				if (inst.opcode != Instruction.OP_SP_ARGUMENTS
						&& vm.getCurrStackFrame().getThisObj() != null
						&& !vm.getCurrStackFrame().method.isStatic()
						&& vm.getCurrStackFrame().getThisReg().getData() != vm
								.getCurrStackFrame().getThisObj()) {
					Log.err(TAG, "Empty this obj!");
				}

				if (inst.opcode == Instruction.OP_RETURN) {
					if (vm.getCurrStackFrame() != null) {
						// Add new tainted heap objs into the caller's res
						for (Plugin plugin : vm.getPluginManager().getPlugins()) {
							Map<String, Map<Object, Instruction>> callerRes = vm
									.getCurrStackFrame().getPluginRes()
									.get(plugin);
							for (String tag : plugin.getCurrtRes().keySet()) {
								Map<Object, Instruction> cres = plugin
										.getCurrtRes().get(tag);
								Map<Object, Instruction> nres = callerRes
										.get(tag);
								for (Object obj : cres.keySet()) {
									if (!(obj instanceof Register)
											|| obj == vm.getReturnReg()) {
										nres.put(obj, cres.get(obj));
									}
								}
							}
						}

						vm.getPluginManager().setCurrRes(
								vm.getCurrStackFrame().getPluginRes());
					}
				} else {
					vm.getCurrStackFrame().pluginRes = vm.getPluginManager()
							.cloneCurrtRes();
				}

				vm.getPluginManager().printResults();
			}
		} else {
			pass = false;
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
	public Object resolvePrimitive(Object op, ClassInfo type) {
		if (op instanceof Unknown
				&& ((Unknown) op).getType() != ClassInfo.primitiveVoid) {
			type = ((Unknown) op).getType();
			Log.bb(TAG, "Unknown type " + type);
		}

		PrimitiveInfo op1 = null;
		if (op instanceof PrimitiveInfo) {
			op1 = (PrimitiveInfo) op;
		}
		if (type.equals(ClassInfo.primitiveChar) || op1 != null && op1.isChar()) {
			return new Character(op1 == null ? 0 : op1.charValue());
		} else if (type.equals(ClassInfo.primitiveBoolean) || op1 != null
				&& op1.isBoolean()) {
			return new Boolean(op1 == null ? false : op1.booleanValue());
		} else if (type.equals(ClassInfo.primitiveInt) || op1 != null
				&& op1.isInteger()) {
			return new Integer(op1 == null ? 0 : op1.intValue());
		} else if (type.equals(ClassInfo.primitiveLong) || op1 != null
				&& op1.isLong()) {
			return new Long(op1 == null ? 0 : op1.longValue());
		} else if (type.equals(ClassInfo.primitiveFloat) || op1 != null
				&& op1.isFloat()) {
			return new Float(op1 == null ? 0 : op1.floatValue());
		} else if (type.equals(ClassInfo.primitiveDouble) || op1 != null
				&& op1.isDouble()) {
			return new Double(op1 == null ? 0 : op1.doubleValue());
		}

		return null;
	}

	// Directly do not invoke.
	Set<String> noInvokeList;
	// Do not invoke when args contain MultiValueVar
	Set<String> noInvokeList2;

}
