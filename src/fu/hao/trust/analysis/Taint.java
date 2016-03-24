package fu.hao.trust.analysis;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.HashMap;
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
import fu.hao.trust.dvm.DalvikVM.Register;
import fu.hao.trust.solver.BiDirVar;
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

	private final String TAG = getClass().getSimpleName();
	Map<Integer, Rule> auxByteCodes = new HashMap<>();
	Map<Integer, Rule> byteCodes = new HashMap<>();

	Set<String> sources;
	Set<String> sinks;
	boolean isSrc;

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
		public Map<Object, Instruction> flow(DalvikVM vm, Instruction inst, Map<Object, Instruction> in) {
			// TODO array
			Map<Object, Instruction> out = new HashMap<>(in);
			if (in.containsKey(vm.getReg(inst.r0)) || in.containsKey(vm.getReg(inst.r0).getData())) {
				out.put(vm.getReg(inst.rdst), in.get(vm.getReg(inst.r0)));
				out.put(vm.getReg(inst.rdst).getData(), in.get(vm.getReg(inst.r0)));
			} else {
				if (out.containsKey(vm.getReg(inst.rdst))) {
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
		public Map<Object, Instruction> flow(DalvikVM vm, Instruction inst, Map<Object, Instruction> in) {
			Map<Object, Instruction> out = new HashMap<>(in);
			if (in.containsKey(inst.extra)) {
				out.put(vm.getReg(inst.rdst), in.get(inst.extra));
				out.put(vm.getReg(inst.rdst).getData(), in.get(inst.extra));
			} else {
				if (out.containsKey(vm.getReg(inst.rdst))) {
					out.remove(vm.getReg(inst.rdst));
				}
			}

			return out;
		}
	}

	class TAINT_OP_SP_ARGUMENTS implements Rule {
		@Override
		public Map<Object, Instruction> flow(DalvikVM vm, Instruction inst, Map<Object, Instruction> in) {
			int[] params = (int[]) inst.extra;
			Register[] callingCtx = vm.getContext();
			Map<Object, Instruction> out = new HashMap<>();
			final String TAG = getClass().toString();

			for (Object res : in.keySet()) {
				// Copy non-reg objs
				if (res instanceof Register) {
					continue;
				}
				out.put(res, in.get(res));
				Log.bb(TAG, "Copy " + res + " as tainted.");
			}

			if (callingCtx != null) {
				for (int i = 0; i < callingCtx.length; i++) {
					if (in.containsKey(callingCtx[i])) {
						out.put(vm.getReg(params[i]), in.get(callingCtx[i]));
						out.put(callingCtx[i].getData(), in.get(callingCtx[i]));
						Log.bb(TAG, "Add " + vm.getReg(params[i]) + "as tainted due to " + callingCtx[i]);
					} else if (in.containsKey(callingCtx[i].getData())) {
						out.put(vm.getReg(params[i]), in.get(callingCtx[i].getData()));
						out.put(callingCtx[i].getData(), in.get(callingCtx[i].getData()));
						Log.bb(TAG, "Add " + vm.getReg(params[i]) + "as tainted due to " + callingCtx[i].getData());
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
		public Map<Object, Instruction> flow(DalvikVM vm, Instruction inst, Map<Object, Instruction> in) {
			Map<Object, Instruction> out = new HashMap<>(in);
			final String TAG = getClass().toString();
			Log.bb(TAG, "Rdst " + vm.getReg(inst.rdst));
			
			if (out.containsKey(vm.getReg(inst.rdst))) {
				out.remove(vm.getReg(inst.rdst));
				Log.bb(TAG, "Rm " + vm.getReg(inst.rdst));
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
		public Map<Object, Instruction> flow(DalvikVM vm, Instruction inst, Map<Object, Instruction> in) {
			Map<Object, Instruction> out = new HashMap<>(in);
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
		public Map<Object, Instruction> flow(DalvikVM vm, Instruction inst, Map<Object, Instruction> in) {
			// TODO Auto-generated method stub
			Map<Object, Instruction> out = new HashMap<>(in);
			return out;
		}
	}

	class TAINT_OP_INVOKE implements Rule {
		@Override
		public Map<Object, Instruction> flow(DalvikVM vm, Instruction inst, Map<Object, Instruction> in) {
			isSrc = false;
			Object[] extra = (Object[]) inst.extra;
			MethodInfo mi = (MethodInfo) extra[0];
			// The register index referred by args
			int[] args = (int[]) extra[1];

			Map<Object, Instruction> out = new HashMap<>(in);
			final String TAG = getClass().toString();
			if (mi.isConstructor()) {
				// FIXME
				/*
				if (sources.contains(sootSignature)) {
					Log.warn(TAG, "Found a src!");
					out.add(vm.getReg(args[0]));
					out.add(vm.getReg(args[0]).getData());
				} else {
					Log.debug(TAG, "Not a src. " + signature);
					if (out.contains(vm.getReg(args[0]))) {
						out.remove(vm.getReg(args[0]));
					}
				}*/

				if (!mi.isStatic()) {
					for (int i = 1; i < args.length; i++) {
						if (in.containsKey(vm.getReg(args[i]))) {
							Log.warn(TAG, "Found a tainted init instance!");
							out.put(vm.getReg(args[0]), in.get(vm.getReg(args[i])));
							out.put(vm.getReg(args[0]).getData(), in.get(vm.getReg(args[i])));
							break;
						} else if (in.containsKey(vm.getReg(args[i]).getData())) {
							Log.warn(TAG, "Found a tainted init instance!");
							out.put(vm.getReg(args[0]), in.get(vm.getReg(args[i]).getData()));
							out.put(vm.getReg(args[0]).getData(), in.get(vm.getReg(args[i]).getData()));
							break;
						}
					}
				}

				return out;
			}

			// Must be a reflection call;
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
			
			Log.bb(TAG, sootSignature);

			if (sinks.contains(sootSignature)) {
				Log.bb(TAG, "Found a sink invocation. " + sootSignature);
				for (int i = 0; i < args.length; i++) {
					if (in.containsKey(vm.getReg(args[i])) || in.containsKey(vm.getReg(args[i]).getData())) {
						Log.warn(TAG, "Found a taint sink " + sootSignature
								+ " leaking data ["
								+ vm.getReg(args[i]).getData() + "] at reg " + args[i] + "!!!");
						Map<String, String> res = new HashMap<>();
						res.put(sootSignature, vm.getReg(args[i]).getData()
								.toString());
						Results.results.add(res);
					}
				}

				return out;
			}


			// Decide whether add return value.
			if (sources.contains(sootSignature)) {
				Log.warn(TAG, "Found a tainted return value!");
				out.put(vm.getReturnReg(), inst);
				out.put(vm.getReturnReg().getData(), inst);
				isSrc = true;
			} else {
				Log.debug(TAG, "not a taint call: " + signature);
			}

			if (vm.getReturnReg().getData() != null || vm.getReturnReg().getType() != null) {
				for (int i = 0; i < args.length; i++) {
					if (in.containsKey(vm.getReg(args[i]))) {
						Log.warn(TAG, "Found a tainted return val!");
						out.put(vm.getReturnReg(), in.get(vm.getReg(args[i])));
						out.put(vm.getReturnReg().getData(), in.get(vm.getReg(args[i])));
						break;
					} else if (in.containsKey(vm.getReg(args[i]).getData())) {
						Log.warn(TAG, "Found a tainted return val!");
						out.put(vm.getReturnReg(), in.get(vm.getReg(args[i]).getData()));
						out.put(vm.getReturnReg().getData(), in.get(vm.getReg(args[i]).getData()));
						break;
					}
				}
			}

			if (out.size() == in.size()) {
				if (out.containsKey(vm.getReturnReg())) {
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
		public Map<Object, Instruction> flow(DalvikVM vm, Instruction inst, Map<Object, Instruction> in) {
			// TODO
			Map<Object, Instruction> out = new HashMap<>(in);
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
		public Map<Object, Instruction> flow(DalvikVM vm, Instruction inst, Map<Object, Instruction> in) {
			// TODO
			Map<Object, Instruction> out = new HashMap<>(in);
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
		public Map<Object, Instruction> flow(DalvikVM vm, Instruction inst, Map<Object, Instruction> in) {
			// TODO long-to-int
			Map<Object, Instruction> out = new HashMap<>(in);
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
		public Map<Object, Instruction> flow(DalvikVM vm, Instruction inst, Map<Object, Instruction> in) {
			Map<Object, Instruction> out = new HashMap<>(in);
			if (in.containsKey(vm.getReturnReg())) {
				out.put(vm.getReg(inst.rdst), in.get(vm.getReturnReg()));
				out.put(vm.getReg(inst.rdst).getData(), in.get(vm.getReturnReg()));
			} else if (in.containsKey(vm.getReturnReg().getData())) {
				out.put(vm.getReg(inst.rdst), in.get(vm.getReturnReg().getData()));
				out.put(vm.getReg(inst.rdst).getData(), in.get(vm.getReturnReg().getData()));
			} else {
				if (in.containsKey(vm.getReg(inst.rdst))) {
					in.remove(inst.rdst);
				}
			}
			return out;
		}
	}

	class TAINT_OP_MOV_EXCEPTION implements Rule {
		@Override
		public Map<Object, Instruction> flow(DalvikVM vm, Instruction inst, Map<Object, Instruction> in) {
			// TODO Auto-generated method stub
			Map<Object, Instruction> out = new HashMap<>(in);
			if (in.containsKey(vm.getCurrStackFrame().getExceptReg())) {
				out.put(vm.getReg(inst.rdst), in.get(vm.getCurrStackFrame().getExceptReg()));
			} else {
				if (in.containsKey(vm.getCurrStackFrame().getExceptReg())) {
					in.remove(vm.getCurrStackFrame().getExceptReg());
				}
			}
			return out;
		}
	}
	
	class TAINT_OP_EXCEPTION_THROW implements Rule {
		@Override
		public Map<Object, Instruction> flow(DalvikVM vm, Instruction inst, Map<Object, Instruction> in) {
			// TODO Auto-generated method stub
			Map<Object, Instruction> out = new HashMap<>(in);
			if (in.containsKey(vm.getReg(inst.r0))) {
				out.put(vm.getCurrStackFrame().getExceptReg(), in.get(vm.getReg(inst.r0)));
			} else {
				if (in.containsKey(vm.getCurrStackFrame().getExceptReg())) {
					in.remove(vm.getCurrStackFrame().getExceptReg());
				}
			}
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
		public Map<Object, Instruction> flow(DalvikVM vm, Instruction inst, Map<Object, Instruction> in) {
			Map<Object, Instruction> out = new HashMap<>(in);
			if (in.containsKey(vm.getReg(inst.r0))) {
				out.put(vm.getReg(inst.rdst), in.get(vm.getReg(inst.r0)));
				out.put(vm.getReg(inst.rdst).getData(), in.get(vm.getReg(inst.r0)));
			} else {
				if (in.containsKey(vm.getReg(inst.rdst))) {
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
		public Map<Object, Instruction> flow(DalvikVM vm, Instruction inst, Map<Object, Instruction> in) {
			Map<Object, Instruction> out = new HashMap<>(in);
			if (in.containsKey(vm.getReg(inst.r0))) {
				out.put(vm.getReg(inst.rdst), in.get(vm.getReg(inst.r0)));
				out.put(vm.getReg(inst.rdst).getData(), in.get(vm.getReg(inst.r0)));
			} else if (in.containsKey(vm.getReg(inst.r0).getData())) {
				out.put(vm.getReg(inst.rdst), in.get(vm.getReg(inst.r0).getData()));
				out.put(vm.getReg(inst.rdst).getData(), in.get(vm.getReg(inst.r0).getData()));
			} else {
				if (inst.r1 != -1 && (in.containsKey(vm.getReg(inst.r1)) )) {
					out.put(vm.getReg(inst.rdst), in.get(vm.getReg(inst.r1)));
					out.put(vm.getReg(inst.rdst).getData(), in.get(vm.getReg(inst.r1)));
				} else if (in.containsKey(vm.getReg(inst.r1).getData())) {
					out.put(vm.getReg(inst.rdst), in.get(vm.getReg(inst.r1).getData()));
					out.put(vm.getReg(inst.rdst).getData(), in.get(vm.getReg(inst.r1).getData()));
				}
					else {
				
					if (in.containsKey(vm.getReg(inst.rdst))) {
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
		public Map<Object, Instruction> flow(DalvikVM vm, Instruction inst, Map<Object, Instruction> in) {
			Map<Object, Instruction> out = new HashMap<>(in);
			if (inst.r0 != -1 && inst.r1 != -1 && (in.containsKey(vm.getReg(inst.r0)) )) {
				out.put(vm.getReg(inst.r1), in.get(vm.getReg(inst.r0)));
				out.put(vm.getReg(inst.r1).getData(), in.get(vm.getReg(inst.r0)));
			}

			if (inst.r1 != -1 && (in.containsKey(vm.getReg(inst.r1)))) {
				out.put(vm.getReg(inst.r0), in.get(vm.getReg(inst.r1).getData()));
				out.put(vm.getReg(inst.r0).getData(), in.get(vm.getReg(inst.r1).getData()));
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
		public Map<Object, Instruction> flow(DalvikVM vm, Instruction inst, Map<Object, Instruction> in) {
			Map<Object, Instruction> out = new HashMap<>(in);
			// array reg
			// Object[] array = (Object[]) vm.getReg(inst.r0).getData();
			Object array = vm.getReg(inst.r0).getData();
			// index reg
			PrimitiveInfo pindex;
			if (vm.getReg(inst.r1).getData() instanceof BiDirVar) {
				if (((BiDirVar) vm.getReg(inst.r1).getData()).getValue() instanceof PrimitiveInfo) {
					pindex = (PrimitiveInfo) ((BiDirVar) vm.getReg(inst.r1).getData()).getValue();
				} else {
					Log.warn(TAG, "Array get error! index is not a int.");
					return out;
				}
			} else {
				pindex = (PrimitiveInfo) vm.getReg(inst.r1).getData();
			}
			
			int index = pindex.intValue();
			if (in.containsKey(array)) {
				out.put(vm.getReg(inst.rdst), in.get(array));
				out.put(vm.getReg(inst.rdst).getData(), in.get(array));
			} else if (in.containsKey(Array.get(array, index))) {
				out.put(vm.getReg(inst.rdst), in.get(Array.get(array, index)));
				out.put(vm.getReg(inst.rdst).getData(), in.get(Array.get(array, index)));
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
		public Map<Object, Instruction> flow(DalvikVM vm, Instruction inst, Map<Object, Instruction> in) {
			// TODO taint all?
			// dest reg
			Register rdst = vm.getReg(inst.rdst);

			Map<Object, Instruction> out = new HashMap<>(in);
			if (in.containsKey(rdst)) {
				// array reg
				Object[] array = (Object[]) vm.getReg(inst.r0).getData();
				// index reg
				int index = ((PrimitiveInfo) vm.getReg(inst.r1).getData())
						.intValue();
				out.put(array[index], in.get(rdst));
				out.put(array[index], in.get(rdst));
			} else if (in.containsKey(rdst.getData())) {
				// array reg
				Object[] array = (Object[]) vm.getReg(inst.r0).getData();
				// index reg
				int index = ((PrimitiveInfo) vm.getReg(inst.r1).getData())
						.intValue();
				out.put(array[index], in.get(rdst.getData()));
				out.put(array[index], in.get(rdst.getData()));
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
		public Map<Object, Instruction> flow(DalvikVM vm, Instruction inst, Map<Object, Instruction> in) {
			Map<Object, Instruction> out = new HashMap<>(in);
			if (in.containsKey(vm.getReg(inst.r0))) {
				out.put(vm.getReg(inst.rdst), in.get(vm.getReg(inst.r0)));
				out.put(vm.getReg(inst.rdst).getData(), in.get(vm.getReg(inst.r0)));
			} else {
				if (inst.r1 != -1 && in.containsKey(vm.getReg(inst.r1))) {
					out.put(vm.getReg(inst.rdst), in.get(vm.getReg(inst.r1)));
					out.put(vm.getReg(inst.rdst).getData(), in.get(vm.getReg(inst.r1)));
				} else {
					if (in.containsKey(vm.getReg(inst.rdst))) {
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
		public Map<Object, Instruction> flow(DalvikVM vm, Instruction inst, Map<Object, Instruction> in) {
			@SuppressWarnings("unchecked")
			Pair<ClassInfo, String> pair = (Pair<ClassInfo, String>) inst.extra;
			Map<Object, Instruction> out = new HashMap<>(in);

			try {
				Class<?> clazz = Class.forName(pair.first.toString());
				Field field = clazz.getDeclaredField(pair.second.toString());
				// TODO only support static field now
				if (in.containsKey(field.get(clazz))) {
					out.put(vm.getReg(inst.r0), in.get(field.get(clazz)));
				} else if (in.containsKey(vm.getReg(inst.r0))) {
					out.remove(vm.getReg(inst.r0));
				}

			} catch (Exception e) {
				ClassInfo owner = pair.first;
				String fieldName = pair.second;

				DVMClass dvmClass = vm.getClass(owner);
				if (in.containsKey(dvmClass.getStatField(fieldName))) {
					out.put(vm.getReg(inst.r0), in.get(dvmClass.getStatField(fieldName)));
				} else if (in.containsKey(vm.getReg(inst.r0))) {
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
		public Map<Object, Instruction> flow(DalvikVM vm, Instruction inst, Map<Object, Instruction> in) {
			Map<Object, Instruction> out = new HashMap<>(in);
			@SuppressWarnings("unchecked")
			Pair<ClassInfo, String> pair = (Pair<ClassInfo, String>) inst.extra;
			ClassInfo owner = pair.first;
			String fieldName = pair.second;
			DVMClass dvmClass = vm.getClass(owner);
			if (in.containsKey(vm.getReg(inst.r0))) {
				out.put(dvmClass.getStatField(fieldName), in.get(vm.getReg(inst.r0)));
			} else if (in.containsKey(vm.getReg(inst.r0).getData())) {
				out.put(dvmClass.getStatField(fieldName), in.get(vm.getReg(inst.r0).getData()));
			} else {
				if (in.containsKey(dvmClass.getStatField(fieldName))) {
					out.remove(dvmClass.getStatField(fieldName));
				}
			}

			return out;
		}
	}

	class TAINT_OP_INSTANCE_GET_FIELD implements Rule {
		/**
		 * @Title: func
		 * @Description: iget v0, v1, Test2.i6:I // field@0003 Reads field@0003
		 *               into v1 (entry #3 in the field id table). The instance
		 *               is referenced by v0.
		 * @param vm
		 * @param inst
		 * @see fu.hao.trust.dvm.ByteCode#func(fu.hao.trust.dvm.DalvikVM,
		 *      patdroid.dalvik.Instruction)
		 */
		@Override
		public Map<Object, Instruction> flow(DalvikVM vm, Instruction inst, Map<Object, Instruction> in) {
			Map<Object, Instruction> out = new HashMap<>(in);
			if (!(vm.getReg(inst.r0).getData() instanceof DVMObject)) {
				Log.warn(TAG, "Object not DVMObject, it is " + vm.getReg(inst.r0).getData());
				return out;
			}
			DVMObject obj = (DVMObject) vm.getReg(inst.r0).getData();
			FieldInfo fieldInfo = (FieldInfo) inst.extra;
			Object field = obj.getFieldObj(fieldInfo);
			
			if (in.containsKey(field)) {
					out.put(vm.getReg(inst.r1), in.get(vm.getReg(inst.r0)));
					Log.bb(TAG, "Add " + obj + "as tainted due to field " + field);
			} else if (in.containsKey(vm.getReg(inst.r1))) {
				// value register, has been assigned to new value
				out.remove(vm.getReg(inst.r1));
			}

			return out;
		}
	}

	class TAINT_OP_INSTANCE_PUT_FIELD implements Rule {
		/**
		 * @Title: func
		 * @Description: iput v0,v2, Test2.i6:I // field@0002 Stores v1 into
		 *               field@0002 (entry #2 in the field id table). The
		 *               instance is referenced by v0
		 * @param vm
		 * @param inst
		 * @see fu.hao.trust.dvm.ByteCode#func(fu.hao.trust.dvm.DalvikVM,
		 *      patdroid.dalvik.Instruction)
		 */
		@Override
		public Map<Object, Instruction> flow(DalvikVM vm, Instruction inst, Map<Object, Instruction> in) {
			final String TAG = getClass().toString();
			Map<Object, Instruction> out = new HashMap<>(in);
			DVMObject obj = (DVMObject) vm.getReg(inst.r0).getData();
			FieldInfo fieldInfo = (FieldInfo) inst.extra;
			
			if (in.containsKey(vm.getReg(inst.r1))) {
				out.put(obj.getFieldObj(fieldInfo), in.get(vm.getReg(inst.r1)));
				Log.bb(TAG, "Add " + obj.getFieldObj(fieldInfo) + "as tainted due to " + vm.getReg(inst.r0));
			} else if (in.containsKey(vm.getReg(inst.r1).getData())) {
				out.put(obj.getFieldObj(fieldInfo), in.get(vm.getReg(inst.r1).getData()));
				Log.bb(TAG, "Add " + obj.getFieldObj(fieldInfo) + "as tainted due to r1 data " + vm.getReg(inst.r1).getData());
			} else { 
				if (in.containsKey(obj.getFieldObj(fieldInfo))) {
					out.remove(obj.getFieldObj(fieldInfo));
				}
			}

			return out;
		}
	}

	public Taint() {
		currtRes = new HashMap<>();
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
		auxByteCodes.put(0x16, new TAINT_OP_MOV_EXCEPTION());
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
		auxByteCodes.put(0x39, new TAINT_OP_EXCEPTION_THROW());
	}

	@Override
	public Map<Object, Instruction> runAnalysis(DalvikVM vm, Instruction inst, Map<Object, Instruction> in) {
		Map<Object, Instruction> out;
		if (byteCodes.containsKey((int) inst.opcode)) {
			out = byteCodes.get((int) inst.opcode).flow(vm, inst, in);
		} else if (auxByteCodes.containsKey((int) inst.opcode_aux)) {
			out = auxByteCodes.get((int) inst.opcode_aux).flow(vm, inst, in);
		} else {
			Log.bb(TAG, "Not a taint op " + inst);
			out = new HashMap<>(in);
		}

		currtRes = out;
		return out;
	}

	@Override
	public Map<Object, Instruction> getCurrRes() {
		return currtRes;
	}

	@Override
	public void reset() {
		currtRes = new HashMap<>();
	}

}
