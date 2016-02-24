package fu.hao.trust.analysis;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import patdroid.core.ClassInfo;
import patdroid.core.FieldInfo;
import patdroid.core.MethodInfo;
import patdroid.core.PrimitiveInfo;
import patdroid.dalvik.Instruction;
import patdroid.util.Pair;
import fu.hao.trust.dvm.DVMClass;
import fu.hao.trust.dvm.DVMObject;
import fu.hao.trust.dvm.DalvikVM;
import fu.hao.trust.dvm.DalvikVM.simple_dvm_register;
import fu.hao.trust.utils.Log;
import fu.hao.trust.utils.SrcSinkParser;

public class Taint extends Plugin {
	// The nested class to implement singleton
	private static class SingletonHolder {
		private static final Taint instance = new Taint();
	}

	// Get THE instance
	public static final Taint v() {
		return SingletonHolder.instance;
	}

	private final String TAG = getClass().toString();
	static Map<Integer, Rule> auxByteCodes = new HashMap<>();
	static Map<Integer, Rule> byteCodes = new HashMap<>();

	static Set<String> sources;
	static Set<String> sinks;

	class TAINT_OP_MOVE_REG implements Rule {
		/**
		 * @Title: flow
		 * @Description: x = y
		 * @param inst
		 * @param in
		 * @return
		 * @see fu.hao.trust.analysis.Rule#flow(patdroid.dalvik.Instruction,
		 *      java.util.Set)
		 */
		@Override
		public Set<Object> flow(DalvikVM vm, Instruction inst, Set<Object> in) {
			// TODO array
			Set<Object> out = new HashSet<>(in);
			if (in.contains(vm.getReg(inst.r0))) {
				out.add(vm.getReg(inst.rdst));
				out.add(vm.getReg(inst.rdst).getData());
			} else {
				if (out.contains(vm.getReg(inst.rdst))) {
					out.remove(vm.getReg(inst.rdst));
				}
			}

			return out;
		}
	}

	class TAINT_OP_MOV_CONST implements Rule {
		/**
		 * @Title: flow
		 * @Description: x = y
		 * @param inst
		 * @param in
		 * @return
		 * @see fu.hao.trust.analysis.Rule#flow(patdroid.dalvik.Instruction,
		 *      java.util.Set)
		 */
		@Override
		public Set<Object> flow(DalvikVM vm, Instruction inst, Set<Object> in) {
			Set<Object> out = new HashSet<>(in);
			if (in.contains(inst.extra)) {
				out.add(vm.getReg(inst.rdst));
				out.add(vm.getReg(inst.rdst).getData());
			} else {
				if (out.contains(vm.getReg(inst.rdst))) {
					out.remove(vm.getReg(inst.rdst));
				}
			}

			return out;
		}
	}

	class TAINT_OP_SP_ARGUMENTS implements Rule {
		@Override
		public Set<Object> flow(DalvikVM vm, Instruction inst, Set<Object> in) {
			int[] args = (int[]) inst.extra;
			int[] callingCtx = vm.getContext();
			Set<Object> out = new HashSet<>(in);
			if (vm.getContext() != null) {
				for (int i = 0; i < vm.getContext().length; i++) {
					if (in.contains(vm.getReg(callingCtx[i]))) {
						out.add(vm.getReg(args[i]));
						out.add(vm.getReg(args[i]).getData());
					} else {
						if (out.contains(vm.getReg(callingCtx[i]))) {
							out.remove(vm.getReg(callingCtx[i]));
						}
					}
				}
			}

			return out;
		}
	}

	class TAINT_OP_NEW_INSTANCE implements Rule {
		/**
		 * @Title: func
		 * @Description: x = new T()
		 * @param vm
		 * @param inst
		 * @see fu.hao.trust.dvm.ByteCode#func(fu.hao.trust.dvm.DalvikVM,
		 *      patdroid.dalvik.Instruction)
		 */
		@Override
		public Set<Object> flow(DalvikVM vm, Instruction inst, Set<Object> in) {
			Set<Object> out = new HashSet<>(in);
			if (in.contains(inst.rdst)) {
				out.add(vm.getReg(inst.rdst));
				out.add(vm.getReg(inst.rdst).getData());
			} else {
				if (out.contains(vm.getReg(inst.rdst))) {
					out.remove(vm.getReg(inst.rdst));
				}
			}

			return out;
		}
	}

	class TAINT_OP_NEW_ARRAY implements Rule {
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
		public Set<Object> flow(DalvikVM vm, Instruction inst, Set<Object> in) {
			Set<Object> out = new HashSet<>(in);
			return out;
		}
	}

	class TAINT_OP_NEW_FILLED_ARRAY implements Rule {
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
		public Set<Object> flow(DalvikVM vm, Instruction inst, Set<Object> in) {
			// TODO Auto-generated method stub
			Set<Object> out = new HashSet<>(in);
			return out;
		}
	}

	class TAINT_OP_INVOKE implements Rule {
		@Override
		public Set<Object> flow(DalvikVM vm, Instruction inst, Set<Object> in) {
			Object[] extra = (Object[]) inst.extra;
			MethodInfo mi = (MethodInfo) extra[0];
			// The register index referred by args
			int[] args = (int[]) extra[1];

			Set<Object> out = new HashSet<>(in);

			// source must be a reflection call;
			if (method == null) {
				return out;
			}

			StringBuilder signature = new StringBuilder("<");
			signature.append(mi.myClass);
			signature.append(": ");
			signature.append(mi.returnType + " ");
			signature.append(mi.name + "(");
			for (int i = 0; i < mi.paramTypes.length - 1; i++) {
				ClassInfo paramType = mi.paramTypes[i];
				signature.append(paramType + ",");
			}
			if (mi.paramTypes.length > 0) {
				signature.append(mi.paramTypes[mi.paramTypes.length - 1]);
			}
			signature.append(")>");
			String sootSignature = signature.toString();

			if (sinks.contains(sootSignature)) {
				for (int i = 0; i < args.length; i++) {
					if (in.contains(vm.getReg(args[i]))) {
						Log.warn(TAG, "found a sink " + sootSignature
								+ " leaking data [" + vm.getReg(args[i]).getData() + "]!!!");
					}
				}

				return out;
			}

			if (mi.isConstructor()) {
				if (sources.contains(sootSignature)) {
					Log.warn(TAG, "found a src!");
					out.add(vm.getReg(args[0]));
					out.add(vm.getReg(args[0]).getData());
				} else {
					Log.debug(TAG, "not a src. " + signature);
					if (out.contains(vm.getReg(args[0]))) {
						out.remove(vm.getReg(args[0]));
					}
				}

				return out;
			}

			if (sources.contains(sootSignature)) {
				Log.warn(TAG, "found a taint invokation!");
				out.add(vm.getReturnReg());
				out.add(vm.getReturnReg().getData());
			} else {
				Log.debug(TAG, "not a taint call: " + signature);

			}

			if (vm.getReturnReg().getData() != null) {
				for (int i = 0; i < args.length; i++) {
					if (in.contains(vm.getReg(args[i]))) {
						Log.warn(TAG, "found a taint invokation!");
						out.add(vm.getReturnReg());
						out.add(vm.getReturnReg().getData());
						break;
					}
				}
			}

			if (out.size() == in.size()) {
				if (out.contains(vm.getReturnReg())) {
					out.remove(vm.getReturnReg());
				}
			}

			return out;
		}
	}

	class TAINT_OP_A_INSTANCEOF implements Rule {
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
		public Set<Object> flow(DalvikVM vm, Instruction inst, Set<Object> in) {
			// TODO
			Set<Object> out = new HashSet<>(in);
			return out;
		}
	}

	class TAINT_OP_A_ARRAY_LENGTH implements Rule {
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
		public Set<Object> flow(DalvikVM vm, Instruction inst, Set<Object> in) {
			// TODO
			Set<Object> out = new HashSet<>(in);
			return out;
		}
	}

	class TAINT_OP_A_CHECKCAST implements Rule {
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
		public Set<Object> flow(DalvikVM vm, Instruction inst, Set<Object> in) {
			// TODO long-to-int
			Set<Object> out = new HashSet<>(in);
			return out;
		}
	}

	class TAINT_OP_MOV_RESULT implements Rule {
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
		public Set<Object> flow(DalvikVM vm, Instruction inst, Set<Object> in) {
			Set<Object> out = new HashSet<>(in);
			if (in.contains(vm.getReturnReg())) {
				out.add(vm.getReg(inst.rdst));
				out.add(vm.getReg(inst.rdst).getData());
			} else {
				if (in.contains(vm.getReg(inst.rdst))) {
					in.remove(inst.rdst);
				}
			}
			return out;
		}
	}

	class TAINT_OP_MOV_EXCEPTION implements Rule {
		@Override
		public Set<Object> flow(DalvikVM vm, Instruction inst, Set<Object> in) {
			// TODO Auto-generated method stub
			Set<Object> out = new HashSet<>(in);
			return out;
		}
	}

	class TAINT_OP_A_CAST implements Rule {
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
		public Set<Object> flow(DalvikVM vm, Instruction inst, Set<Object> in) {
			Set<Object> out = new HashSet<>(in);
			if (in.contains(vm.getReg(inst.r0))) {
				out.add(vm.getReg(inst.rdst));
				out.add(vm.getReg(inst.rdst).getData());
			} else {
				if (in.contains(vm.getReg(inst.rdst))) {
					in.remove(inst.rdst);
				}
			}
			return out;
		}
	}

	class TAINT_OP_CMP implements Rule {
		/**
		 * @Title: func
		 * @Description: helper func for cmp
		 * @param vm
		 * @param inst
		 * @see fu.hao.trust.dvm.ByteCode#func(fu.hao.trust.dvm.DalvikVM,
		 *      patdroid.dalvik.Instruction)
		 */
		public Set<Object> flow(DalvikVM vm, Instruction inst, Set<Object> in) {
			Set<Object> out = new HashSet<>(in);
			if (in.contains(vm.getReg(inst.r0))) {
				out.add(vm.getReg(inst.rdst));
				out.add(vm.getReg(inst.rdst).getData());
			} else {
				if (inst.r1 != -1 && in.contains(vm.getReg(inst.r1))) {
					out.add(vm.getReg(inst.rdst));
					out.add(vm.getReg(inst.rdst).getData());
				} else {
					if (in.contains(vm.getReg(inst.rdst))) {
						in.remove(inst.rdst);
					}
				}
			}

			return out;
		}
	}

	class TAINT_OP_IF implements Rule {
		/**
		 * @Title: func
		 * @Description: helper func for if
		 * @param vm
		 * @param inst
		 * @see fu.hao.trust.dvm.ByteCode#func(fu.hao.trust.dvm.DalvikVM,
		 *      patdroid.dalvik.Instruction)
		 */
		public Set<Object> flow(DalvikVM vm, Instruction inst, Set<Object> in) {
			Set<Object> out = new HashSet<>(in);
			if (in.contains(vm.getReg(inst.r0)) && inst.r1 != -1) {
				out.add(vm.getReg(inst.r1));
				out.add(vm.getReg(inst.r1).getData());
			}

			if (inst.r1 != -1 && in.contains(vm.getReg(inst.r1))) {
				out.add(vm.getReg(inst.r0));
				out.add(vm.getReg(inst.r0).getData());
			}

			return out;
		}
	}

	class TAINT_OP_ARRAY_GET implements Rule {
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
		public Set<Object> flow(DalvikVM vm, Instruction inst, Set<Object> in) {
			// array reg
			// Object[] array = (Object[]) vm.getReg(inst.r0).getData();
			Object array = vm.getReg(inst.r0).getData();
			// index reg
			int index = ((PrimitiveInfo) vm.getReg(inst.r1).getData())
					.intValue();

			Set<Object> out = new HashSet<>(in);
			if (in.contains(array) || in.contains(Array.get(array, index))) {
				out.add(vm.getReg(inst.rdst));
				out.add(vm.getReg(inst.rdst).getData());
			}
			return out;
		}
	}

	class TAINT_OP_ARRAY_PUT implements Rule {
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
		public Set<Object> flow(DalvikVM vm, Instruction inst, Set<Object> in) {
			// TODO taint all?
			// dest reg
			simple_dvm_register rdst = vm.getReg(inst.rdst);

			Set<Object> out = new HashSet<>(in);
			if (in.contains(rdst)) {
				// array reg
				Object[] array = (Object[]) vm.getReg(inst.r0).getData();
				// index reg
				int index = ((PrimitiveInfo) vm.getReg(inst.r1).getData())
						.intValue();
				out.add(array[index]);
				out.add(array[index]);
			}
			return out;
		}
	}

	class TAINT_OP_ARITHETIC implements Rule {
		/**
		 * @Title: func
		 * @Description: add-int/2addr v0,v1 Adds v1 to v0.
		 * @param vm
		 * @param inst
		 * @see fu.hao.trust.dvm.ByteCode#func(fu.hao.trust.dvm.DalvikVM,
		 *      patdroid.dalvik.Instruction)
		 */
		@Override
		public Set<Object> flow(DalvikVM vm, Instruction inst, Set<Object> in) {
			Set<Object> out = new HashSet<>(in);
			if (in.contains(vm.getReg(inst.r0))) {
				out.add(vm.getReg(inst.rdst));
				out.add(vm.getReg(inst.rdst).getData());
			} else {
				if (inst.r1 != -1 && in.contains(vm.getReg(inst.r1))) {
					out.add(vm.getReg(inst.rdst));
					out.add(vm.getReg(inst.rdst).getData());
				} else {
					if (in.contains(vm.getReg(inst.rdst))) {
						in.remove(inst.rdst);
					}
				}
			}

			return out;
		}
	}

	class TAINT_OP_STATIC_GET_FIELD implements Rule {
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
		public Set<Object> flow(DalvikVM vm, Instruction inst, Set<Object> in) {
			@SuppressWarnings("unchecked")
			Pair<ClassInfo, String> pair = (Pair<ClassInfo, String>) inst.extra;
			Set<Object> out = new HashSet<>(in);

			try {
				Class<?> clazz = Class.forName(pair.first.toString());
				Field field = clazz.getDeclaredField(pair.second.toString());
				// TODO only support static field now
				if (in.contains(field.get(clazz))) {
					out.add(vm.getReg(inst.r0));
				} else if (in.contains(vm.getReg(inst.r0))) {
					out.remove(vm.getReg(inst.r0));
				}

			} catch (Exception e) {
				FieldInfo statFieldInfo = new FieldInfo(pair.first, pair.second);
				Log.debug(TAG, "sget " + statFieldInfo.getFieldType());

				DVMClass dvmClass = vm.getClass(statFieldInfo.getFieldType());
				Log.debug(TAG, "sget " + dvmClass);
				Log.debug(TAG, statFieldInfo.toString());
				if (in.contains(dvmClass.getStatField(statFieldInfo))) {
					out.add(vm.getReg(inst.r0));
				} else if (in.contains(vm.getReg(inst.r0))) {
					out.remove(vm.getReg(inst.r0));
				}
			}

			return out;
		}
	}

	class TAINT_OP_STATIC_PUT_FIELD implements Rule {
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
		public Set<Object> flow(DalvikVM vm, Instruction inst, Set<Object> in) {
			Set<Object> out = new HashSet<>(in);
			if (in.contains(vm.getReg(inst.r0).getData())) {
				out.add(vm.getReg(inst.r0));
				@SuppressWarnings("unchecked")
				Pair<ClassInfo, String> pair = (Pair<ClassInfo, String>) inst.extra;
				FieldInfo statFieldInfo = new FieldInfo(pair.first, pair.second);
				DVMClass dvmClass = vm.getClass(statFieldInfo.getFieldType());
				out.add(dvmClass.getStatField(statFieldInfo));
			}

			return out;
		}
	}

	class TAINT_OP_INSTANCE_GET_FIELD implements Rule {
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
		public Set<Object> flow(DalvikVM vm, Instruction inst, Set<Object> in) {
			Set<Object> out = new HashSet<>(in);
			if (in.contains(vm.getReg(inst.r0))) {
				Object obj = vm.getReg(inst.r0).getData();
				if (obj instanceof DVMObject) {
					out.add(vm.getReg(inst.r1));
				} else {
					// TODO reflection get field
				}
			} else if (in.contains(vm.getReg(inst.r1))) {
				out.remove(vm.getReg(inst.r1));
			}

			return out;
		}
	}

	class TAINT_OP_INSTANCE_PUT_FIELD implements Rule {
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
		public Set<Object> flow(DalvikVM vm, Instruction inst, Set<Object> in) {
			Set<Object> out = new HashSet<>(in);
			if (in.contains(vm.getReg(inst.r0))) {
				Object obj = vm.getReg(inst.r0).getData();
				out.add(obj);
			}

			return out;
		}
	}

	Taint() {
		currtRes = new HashSet<>();
		SrcSinkParser parser;
		try {
			parser = SrcSinkParser.fromFile("SourcesAndSinks.txt");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		sources = parser.getSrcStrs();
		sinks = parser.getSinkStrs();

		Log.debug(TAG, "srcs: " + sources);

		byteCodes.put(0x07, new TAINT_OP_CMP());
		byteCodes.put(0x08, new TAINT_OP_IF());
		byteCodes.put(0x0C, new TAINT_OP_INVOKE());

		auxByteCodes.put(0x01, new TAINT_OP_MOVE_REG());
		auxByteCodes.put(0x02, new TAINT_OP_MOV_CONST());
		// auxByteCodes.put(0x03, new TAINT_OP_RETURN_VOID());
		// auxByteCodes.put(0x04, new TAINT_OP_RETURN_SOMETHING());
		// auxByteCodes.put(0x05, new TAINT_OP_MONITOR_ENTER());
		// auxByteCodes.put(0x06, new TAINT_OP_MONITOR_EXIT());
		auxByteCodes.put(0x07, new TAINT_OP_SP_ARGUMENTS());
		auxByteCodes.put(0x08, new TAINT_OP_NEW_INSTANCE());
		// auxByteCodes.put(0x09, new TAINT_OP_NEW_ARRAY());
		// auxByteCodes.put(0x0A, new TAINT_OP_NEW_FILLED_ARRAY());
		/*
		 * auxByteCodes.put(0x0B, new TAINT_OP_INVOKE_DIRECT());
		 * auxByteCodes.put(0x0C, new TAINT_OP_INVOKE_SUPER());
		 * auxByteCodes.put(0x0D, new TAINT_OP_INVOKE_VIRTUAL());
		 * auxByteCodes.put(0x0E, new TAINT_OP_INVOKE_STATIC());
		 * auxByteCodes.put(0x0F, new TAINT_OP_INVOKE_INTERFACE()); //
		 * auxByteCodes.put(0x10, new TAINT_OP_A_INSTANCEOF()); //
		 * auxByteCodes.put(0x11, new TAINT_OP_A_ARRAY_LENGTH()); //
		 * auxByteCodes.put(0x12, new TAINT_OP_A_CHECKCAST());
		 * auxByteCodes.put(0x13, new TAINT_OP_A_NOT()); auxByteCodes.put(0x14,
		 * new TAINT_OP_A_NEG());
		 */
		auxByteCodes.put(0x15, new TAINT_OP_MOV_RESULT());
		// auxByteCodes.put(0x16, new TAINT_OP_MOV_EXCEPTION());
		/*
		 * auxByteCodes.put(0x17, new TAINT_OP_A_CAST()); auxByteCodes.put(0x18,
		 * new TAINT_OP_IF_EQ()); auxByteCodes.put(0x19, new TAINT_OP_IF_NE());
		 * auxByteCodes.put(0x1A, new TAINT_OP_IF_LT()); auxByteCodes.put(0x1B,
		 * new TAINT_OP_IF_GE()); auxByteCodes.put(0x1C, new TAINT_OP_IF_GT());
		 * auxByteCodes.put(0x1D, new TAINT_OP_IF_LE()); auxByteCodes.put(0x1E,
		 * new TAINT_OP_IF_EQZ()); auxByteCodes.put(0x1F, new
		 * TAINT_OP_IF_NEZ()); auxByteCodes.put(0x20, new TAINT_OP_IF_LTZ());
		 * auxByteCodes.put(0x21, new TAINT_OP_IF_GEZ()); auxByteCodes.put(0x22,
		 * new TAINT_OP_IF_GTZ()); auxByteCodes.put(0x23, new
		 * TAINT_OP_IF_LEZ());
		 */
		auxByteCodes.put(0x24, new TAINT_OP_ARRAY_GET());
		auxByteCodes.put(0x25, new TAINT_OP_ARRAY_PUT());
		/*
		 * auxByteCodes.put(0x26, new TAINT_OP_A_ADD()); auxByteCodes.put(0x27,
		 * new TAINT_OP_A_SUB()); auxByteCodes.put(0x28, new TAINT_OP_A_MUL());
		 * auxByteCodes.put(0x29, new TAINT_OP_A_DIV()); auxByteCodes.put(0x2A,
		 * new TAINT_OP_A_REM()); auxByteCodes.put(0x2B, new TAINT_OP_A_AND());
		 * auxByteCodes.put(0x2C, new TAINT_OP_A_OR()); auxByteCodes.put(0x2D,
		 * new TAINT_OP_A_XOR()); auxByteCodes.put(0x2E, new TAINT_OP_A_SHL());
		 * auxByteCodes.put(0x2F, new TAINT_OP_A_SHR()); auxByteCodes.put(0x30,
		 * new TAINT_OP_A_USHR()); auxByteCodes.put(0x31, new
		 * TAINT_OP_CMP_LONG()); auxByteCodes.put(0x32, new
		 * TAINT_OP_CMP_LESS()); auxByteCodes.put(0x33, new
		 * TAINT_OP_CMP_GREATER());
		 */
		auxByteCodes.put(0x34, new TAINT_OP_STATIC_GET_FIELD());
		auxByteCodes.put(0x35, new TAINT_OP_STATIC_PUT_FIELD());
		auxByteCodes.put(0x36, new TAINT_OP_INSTANCE_GET_FIELD());
		auxByteCodes.put(0x37, new TAINT_OP_INSTANCE_PUT_FIELD());
		// auxByteCodes.put(0x38, new TAINT_OP_EXCEPTION_TRYCATCH());
		// auxByteCodes.put(0x39, new TAINT_OP_EXCEPTION_THROW());
	}

	@Override
	public Set<Object> runAnalysis(DalvikVM vm, Instruction inst, Set<Object> in) {
		Set<Object> out;
		if (byteCodes.containsKey((int) inst.opcode)) {
			out = byteCodes.get((int) inst.opcode).flow(vm, inst, in);
		} else if (auxByteCodes.containsKey((int) inst.opcode_aux)) {
			out = auxByteCodes.get((int) inst.opcode_aux).flow(vm, inst, in);
		} else {
			Log.debug(TAG, "not a taint op " + inst);
			out = new HashSet<>(in);
		}

		currtRes = out;
		return out;
	}

	@Override
	public Set<Object> getCurrRes() {
		return currtRes;
	}

}
