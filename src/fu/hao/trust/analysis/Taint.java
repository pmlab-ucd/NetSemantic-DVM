package fu.hao.trust.analysis;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.widget.Adapter;
import patdroid.core.ClassInfo;
import patdroid.core.FieldInfo;
import patdroid.core.MethodInfo;
import patdroid.core.PrimitiveInfo;
import patdroid.dalvik.Instruction;
import patdroid.util.Pair;
import fu.hao.trust.data.PluginConfig;
import fu.hao.trust.data.Results;
import fu.hao.trust.data.SymbolicVar;
import fu.hao.trust.dvm.DVMClass;
import fu.hao.trust.dvm.DVMObject;
import fu.hao.trust.dvm.DalvikVM;
import fu.hao.trust.dvm.DalvikVM.Register;
import fu.hao.trust.utils.Log;
import fu.hao.trust.utils.Maid;
import fu.hao.trust.utils.Settings;
import fu.hao.trust.utils.SrcSinkParser;

public class Taint extends Plugin {
	private final String tag = getClass().getSimpleName();
	protected Map<Integer, Rule> auxByteCodes = new HashMap<>();
	protected Map<Integer, Rule> byteCodes = new HashMap<>();

	protected Map<String, PluginConfig> configs;

	private static Set<String> defaultSources;
	private static Set<String> defaultSinks;

	private HashMap<Integer, Rule> preProcessings;

	static {
		SrcSinkParser parser;
		try {
			parser = SrcSinkParser.fromFile("SourcesAndSinks.txt");
			setDefaultSources(parser.getSrcStrs());
			setDefaultSinks(parser.getSinkStrs());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	protected Set<String> thisAsTaintedList;

	class TAINT_OP_MOV_REG implements Rule {
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
		public Map<String, Map<Object, Instruction>> flow(DalvikVM vm,
				Instruction inst, Map<String, Map<Object, Instruction>> ins) {
			// TODO array
			Map<String, Map<Object, Instruction>> outs = new HashMap<>();
			for (String tag : ins.keySet()) {
				Map<Object, Instruction> in = ins.get(tag);
				Map<Object, Instruction> out = new HashMap<>(in);
				if (in.containsKey(vm.getReg(inst.r0))) {
					out.put(vm.getReg(inst.rdst), in.get(vm.getReg(inst.r0)));
					out.put(vm.getReg(inst.rdst).getData(),
							in.get(vm.getReg(inst.r0)));
				} else if (in.containsKey(vm.getReg(inst.r0).getData())) {
					out.put(vm.getReg(inst.rdst),
							in.get(vm.getReg(inst.r0).getData()));
					out.put(vm.getReg(inst.rdst).getData(),
							in.get(vm.getReg(inst.r0).getData()));
				} else {
					if (out.containsKey(vm.getReg(inst.rdst))) {
						out.remove(vm.getReg(inst.rdst));
					}
					if (in.containsKey(vm.getReturnReg())) {
						out.remove(vm.getReturnReg());
					}
				}
				outs.put(tag, out);
			}

			return outs;
		}
	}

	class TAINT_OP_MOV_CONST implements Rule {
		final String tag = getClass().getSimpleName();

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
		public Map<String, Map<Object, Instruction>> flow(DalvikVM vm,
				Instruction inst, Map<String, Map<Object, Instruction>> ins) {
			Map<String, Map<Object, Instruction>> outs = new HashMap<>();
			for (String tag : ins.keySet()) {
				Map<Object, Instruction> in = ins.get(tag);
				Map<Object, Instruction> out = new HashMap<>(in);
				if (out.containsKey(vm.getReg(inst.rdst))) {
					out.remove(vm.getReg(inst.rdst));
				}
				outs.put(tag, out);
			}

			return outs;
		}
	}

	class TAINT_OP_SP_ARGUMENTS implements Rule {

		final String tag = getClass().getSimpleName();

		@Override
		public Map<String, Map<Object, Instruction>> flow(DalvikVM vm,
				Instruction inst, Map<String, Map<Object, Instruction>> ins) {
			int[] params = (int[]) inst.getExtra();
			Register[] callingCtx = vm.getCallContext();
			Map<String, Map<Object, Instruction>> outs = new HashMap<>();

			for (String tag : ins.keySet()) {
				Map<Object, Instruction> in = ins.get(tag);
				Map<Object, Instruction> out = new HashMap<>(in);
				for (Object res : in.keySet()) {
					// Copy non-reg objs
					if (res instanceof Register) {
						continue;
					}
					out.put(res, in.get(res));
					try {
						Log.bb(tag, "Copy " + res + " as tainted.");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				if (callingCtx != null) {
					for (int i = 0; i < callingCtx.length; i++) {
						if (in.containsKey(callingCtx[i])) {
							out.put(vm.getReg(params[i]), in.get(callingCtx[i]));
							out.put(callingCtx[i].getData(),
									in.get(callingCtx[i]));
							Log.bb(tag, "Add " + vm.getReg(params[i])
									+ " as tainted due to " + callingCtx[i]);
							out.remove(callingCtx[i]);
						} else if (callingCtx[i].isUsed()
								&& in.containsKey(callingCtx[i].getData())) {
							out.put(vm.getReg(params[i]),
									in.get(callingCtx[i].getData()));
							out.put(callingCtx[i].getData(),
									in.get(callingCtx[i].getData()));
							Log.bb(tag,
									"Add " + vm.getReg(params[i])
											+ " as tainted due to "
											+ callingCtx[i].getData());
						}
					}
				}
				outs.put(tag, out);
			}

			return outs;
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
		public Map<String, Map<Object, Instruction>> flow(DalvikVM vm,
				Instruction inst, Map<String, Map<Object, Instruction>> ins) {
			Map<String, Map<Object, Instruction>> outs = new HashMap<>();

			for (String tag : ins.keySet()) {
				Map<Object, Instruction> in = ins.get(tag);
				Map<Object, Instruction> out = new HashMap<>(in);
				Log.bb(tag, "Rdst " + vm.getReg(inst.rdst));

				if (out.containsKey(vm.getReg(inst.rdst))) {
					out.remove(vm.getReg(inst.rdst));
					Log.bb(tag, "Rm " + vm.getReg(inst.rdst));
				}

				outs.put(tag, out);
			}
			return outs;
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
		public Map<String, Map<Object, Instruction>> flow(DalvikVM vm,
				Instruction inst, Map<String, Map<Object, Instruction>> ins) {
			Map<String, Map<Object, Instruction>> outs = new HashMap<>(ins);

			return outs;
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
		public Map<String, Map<Object, Instruction>> flow(DalvikVM vm,
				Instruction inst, Map<String, Map<Object, Instruction>> ins) {
			Map<String, Map<Object, Instruction>> outs = new HashMap<>();
			// TODO Auto-generated method stub

			return outs;
		}
	}

	class TAINT_PRE_INVOKE implements Rule {

		public Map<String, Map<Object, Instruction>> flow(DalvikVM vm,
				Instruction inst, Map<String, Map<Object, Instruction>> ins) {
			Object[] extra = (Object[]) inst.getExtra();
			MethodInfo mi = (MethodInfo) extra[0];
			// The register index referred by args
			int[] args = (int[]) extra[1];

			Map<String, Map<Object, Instruction>> outs = new HashMap<>();

			for (String tag : ins.keySet()) {
				Map<Object, Instruction> in = ins.get(tag);
				Map<Object, Instruction> out = new HashMap<>(in);

				if (mi.isConstructor()) {
					if (!mi.isStatic() && vm.getReg(args[0]).isUsed()
							&& vm.getReg(args[0]).getData() instanceof Adapter) {
						// FIXME
						/*
						 * if (sources.contains(sootSignature)) { Log.warn(tag,
						 * "Found a src!"); out.add(vm.getReg(args[0]));
						 * out.add(vm.getReg(args[0]).getData()); } else {
						 * Log.debug(tag, "Not a src. " + signature); if
						 * (out.contains(vm.getReg(args[0]))) {
						 * out.remove(vm.getReg(args[0])); } }
						 */
						for (int i = 1; i < args.length; i++) {
							Log.bb(tag,
									i
											+ "th: "
											+ (vm.getReg(args[i]).isUsed() ? vm
													.getReg(args[i]).getData()
													: null));
							if (in.containsKey(vm.getReg(args[i]))) {
								Log.warn(tag, "Found a tainted init instance!");
								if (vm.getReg(args[0]).isUsed()) {
									out.put(vm.getReg(args[0]),
											in.get(vm.getReg(args[i])));
									out.put(vm.getReg(args[0]).getData(),
											in.get(vm.getReg(args[i])));
								}
								break;
							} else if (vm.getReg(args[i]).isUsed()
									&& in.containsKey(vm.getReg(args[i])
											.getData())) {
								Log.warn(tag, "Found a tainted init instance!");
								out.put(vm.getReg(args[0]),
										in.get(vm.getReg(args[i]).getData()));
								out.put(vm.getReg(args[0]).getData(),
										in.get(vm.getReg(args[i]).getData()));
								break;
							}
						}
					}
				}
				outs.put(tag, out);
			}

			return outs;
		}
	}

	class TAINT_OP_INVOKE implements Rule {
		final String tag = getClass().getSimpleName();

		@Override
		public Map<String, Map<Object, Instruction>> flow(DalvikVM vm,
				Instruction inst, Map<String, Map<Object, Instruction>> ins) {
			Object[] extra = (Object[]) inst.getExtra();
			MethodInfo mi = (MethodInfo) extra[0];
			// The register index referred by args
			int[] args = (int[]) extra[1];

			Map<String, Map<Object, Instruction>> outs = new HashMap<>();

			for (String tag : ins.keySet()) {
				Map<Object, Instruction> in = ins.get(tag);
				Map<Object, Instruction> out = new HashMap<>(in);
				Set<String> sources = configs.get(tag).getSources();
				Set<String> sinks = configs.get(tag).getSinks();

				// Must be a reflective call;
				if (vm.getReflectMethod() != null || mi.isConstructor()) {
					String sootSignature = Taint.getSootSignature(mi);
					Log.bb(tag, sootSignature);
					Pair<Integer, Instruction> hasTaintedParam = null;

					// Decide whether add return value.
					if (sources != null && sources.contains(sootSignature)) {
						Log.warn(tag, "Found a tainted return value!");
						out.put(vm.getReturnReg(), inst);
						out.put(vm.getReturnReg().getData(), inst);
					} else if (!mi.returnType.isPrimitive()
							|| mi.name.contains("parse")) {
						Log.debug(tag, "Not a taint source call: "
								+ sootSignature);
						for (int i = 0; i < args.length; i++) {
							if (in.containsKey(vm.getReg(args[i]))) {
								if (vm.getReturnReg().isUsed()) {
									Log.warn(tag, "Found a tainted return val!");
									out.put(vm.getReturnReg(),
											in.get(vm.getReg(args[i])));
									out.put(vm.getReturnReg().getData(),
											in.get(vm.getReg(args[i])));
								}
								hasTaintedParam = new Pair<>(args[i], in.get(vm
										.getReg(args[i])));
								break;
							} else if (vm.getReg(args[i]).isUsed()
									&& in.containsKey(vm.getReg(args[i])
											.getData())) {
								if (vm.getReturnReg().isUsed()) {
									Log.warn(tag, "Found a tainted return val!");
									out.put(vm.getReturnReg(), in.get(vm
											.getReg(args[i]).getData()));
									out.put(vm.getReturnReg().getData(), in
											.get(vm.getReg(args[i]).getData()));
								}
								hasTaintedParam = new Pair<>(args[i], in.get(vm
										.getReg(args[i]).getData()));
								break;
							} else if (vm.getReg(args[i]).isUsed()
									&& vm.getReg(args[i]).getData() != null
									&& vm.getReg(args[i]).getData().getClass()
											.isArray()) {
								// For array.
								Object array = vm.getReg(args[i]).getData();
								for (int j = 0; j < Array.getLength(array); j++) {
									if (in.containsKey(Array.get(array, j))) {
										if (vm.getReturnReg().isUsed()) {
											Log.warn(tag,
													"Found a tainted return val!");
											out.put(vm.getReturnReg(),
													in.get(Array.get(array, j)));
											out.put(vm.getReturnReg().getData(),
													in.get(Array.get(array, j)));
										}
										hasTaintedParam = new Pair<>(args[i],
												in.get(Array.get(array, j)));
										break;
									}
								}

								if (hasTaintedParam != null) {
									break;
								}
							}
						}
					}

					if (sinks != null && sinks.contains(sootSignature)) {
						Log.debug(tag, "Found a sink invocation. " + sootSignature);
						if (hasTaintedParam == null) {
							// Double Check
							hasTaintedParam = hasTaintedParam(vm, args, in);
						}

						if (hasTaintedParam != null) {
							Log.warn(tag, "Found a tainted sink "
									+ sootSignature
									+ " leaking data ["
									+ vm.getReg(hasTaintedParam.getFirst())
											.getData() + "] at reg "
									+ hasTaintedParam.getFirst() + "!!!");
							Map<String, String> res = new HashMap<>();
							res.put(sootSignature,
									vm.getReg(hasTaintedParam.getFirst())
											.getData().toString());
							Results.results.add(res);

							if (mi.isConstructor()) {
								out.put(vm.getReg(args[0]),
										hasTaintedParam.getSecond());
								out.put(vm.getReg(args[0]).getData(),
										hasTaintedParam.getSecond());
								Log.warn(tag,
										"A tainted init "
												+ vm.getReg(args[0]).getData()
												+ " identified, add reg"
												+ args[0] + " as tainted");
							}
						}
					}
					
					if (Maid.isElem(thisAsTaintedList, mi.toString())) {
						if (hasTaintedParam == null) {
							// Double Check
							hasTaintedParam = hasTaintedParam(vm, args, in);
						}
						
						if (hasTaintedParam != null) {
							out.put(vm.getReg(args[0]),
									hasTaintedParam.getSecond());
							out.put(vm.getReg(args[0]).getData(),
									hasTaintedParam.getSecond());
							Log.warn(tag,
									"A tainted thisObj "
											+ vm.getReg(args[0]).getData()
											+ " identified, add reg"
											+ args[0] + " as tainted");
						}
					}

					if (in.size() == out.size()
							&& in.containsKey(vm.getReturnReg())) {
						out.remove(vm.getReturnReg());
					}

				}
				outs.put(tag, out);
			}
			return outs;
		}
	}
	
	private Pair<Integer, Instruction> hasTaintedParam(DalvikVM vm, int[] args, Map<Object, Instruction> in) {
		for (int i = 0; i < args.length; i++) {
			if (vm.getReg(args[i]).isUsed()) {
				if (in.containsKey(vm.getReg(args[i]))
						|| in.containsKey(vm
								.getReg(args[i]).getData())) {
					return new Pair<>(args[i],
							in.get(vm.getReg(args[i])
									.getData()));
				} else if (vm.getReg(args[i]).getData() instanceof List) {
					List<?> list = (List<?>) vm.getReg(args[i]).getData();
					for (Object elem : list) {
						if (in.containsKey(elem)) {
							return new Pair<>(args[i],
									in.get(elem));
						}
					}
				}
			}
		}
		
		return null;
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
		public Map<String, Map<Object, Instruction>> flow(DalvikVM vm,
				Instruction inst, Map<String, Map<Object, Instruction>> ins) {
			Map<String, Map<Object, Instruction>> outs = new HashMap<>(ins);
			// TODO
			return outs;
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
		public Map<String, Map<Object, Instruction>> flow(DalvikVM vm,
				Instruction inst, Map<String, Map<Object, Instruction>> ins) {
			Map<String, Map<Object, Instruction>> outs = new HashMap<>(ins);
			// TODO
			return outs;
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
		public Map<String, Map<Object, Instruction>> flow(DalvikVM vm,
				Instruction inst, Map<String, Map<Object, Instruction>> ins) {
			Map<String, Map<Object, Instruction>> outs = new HashMap<>(ins);
			// TODO long-to-int
			return outs;
		}
	}

	class TAINT_OP_MOV_RESULT implements Rule {
		final String tag = getClass().getSimpleName();

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
		public Map<String, Map<Object, Instruction>> flow(DalvikVM vm,
				Instruction inst, Map<String, Map<Object, Instruction>> ins) {
			Map<String, Map<Object, Instruction>> outs = new HashMap<>();
			for (String tag : ins.keySet()) {
				Map<Object, Instruction> in = ins.get(tag);
				Map<Object, Instruction> out = new HashMap<>(in);
				if (in.containsKey(vm.getReturnReg())) {
					out.put(vm.getReg(inst.rdst), in.get(vm.getReturnReg()));
					Log.bb(tag, "" + in.get(vm.getReturnReg()));
					if (vm.getReg(inst.rdst).getData() != null) {
						out.put(vm.getReg(inst.rdst).getData(),
								in.get(vm.getReturnReg()));
					}
					Log.bb(tag, "Add reg " + inst.rdst + " due to ret reg.");
				} else if (in.containsKey(vm.getReturnReg().getData())) {
					out.put(vm.getReg(inst.rdst),
							in.get(vm.getReturnReg().getData()));
					out.put(vm.getReg(inst.rdst).getData(),
							in.get(vm.getReturnReg().getData()));
					Log.bb(tag, "Add reg " + inst.rdst + " due to ret data.");
				} else {
					if (in.containsKey(vm.getReg(inst.rdst))) {
						out.remove(vm.getReg(inst.rdst));
						Log.bb(tag, "Rm reg " + inst.rdst);
					}
				}

				if (in.containsKey(vm.getReturnReg())) {
					out.remove(vm.getReturnReg());
				}
				outs.put(tag, out);
			}
			return outs;
		}
	}

	class TAINT_OP_MOV_EXCEPTION implements Rule {
		@Override
		public Map<String, Map<Object, Instruction>> flow(DalvikVM vm,
				Instruction inst, Map<String, Map<Object, Instruction>> ins) {
			Map<String, Map<Object, Instruction>> outs = new HashMap<>();
			for (String tag : ins.keySet()) {
				Map<Object, Instruction> in = ins.get(tag);
				// TODO Auto-generated method stub
				Map<Object, Instruction> out = new HashMap<>(in);
				if (in.containsKey(vm.getCurrStackFrame().getExceptReg())) {
					out.put(vm.getReg(inst.rdst),
							in.get(vm.getCurrStackFrame().getExceptReg()));
				} else {
					if (in.containsKey(vm.getCurrStackFrame().getExceptReg())) {
						out.remove(vm.getCurrStackFrame().getExceptReg());
					}
				}
				outs.put(tag, out);
			}
			return outs;
		}
	}

	class TAINT_OP_EXCEPTION_THROW implements Rule {
		@Override
		public Map<String, Map<Object, Instruction>> flow(DalvikVM vm,
				Instruction inst, Map<String, Map<Object, Instruction>> ins) {
			Map<String, Map<Object, Instruction>> outs = new HashMap<>();
			for (String tag : ins.keySet()) {
				Map<Object, Instruction> in = ins.get(tag);
				// TODO Auto-generated method stub
				Map<Object, Instruction> out = new HashMap<>(in);
				if (in.containsKey(vm.getReg(inst.r0))) {
					out.put(vm.getCurrStackFrame().getExceptReg(),
							in.get(vm.getReg(inst.r0)));
				} else {
					if (in.containsKey(vm.getCurrStackFrame().getExceptReg())) {
						out.remove(vm.getCurrStackFrame().getExceptReg());
					}
				}
				outs.put(tag, out);
			}
			return outs;
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
		public Map<String, Map<Object, Instruction>> flow(DalvikVM vm,
				Instruction inst, Map<String, Map<Object, Instruction>> ins) {
			Map<String, Map<Object, Instruction>> outs = new HashMap<>();
			for (String tag : ins.keySet()) {
				Map<Object, Instruction> in = ins.get(tag);
				Map<Object, Instruction> out = new HashMap<>(in);
				if (in.containsKey(vm.getReg(inst.r0))) {
					out.put(vm.getReg(inst.rdst), in.get(vm.getReg(inst.r0)));
					out.put(vm.getReg(inst.rdst).getData(),
							in.get(vm.getReg(inst.r0)));
				} else {
					if (in.containsKey(vm.getReg(inst.rdst))) {
						out.remove(vm.getReg(inst.rdst));
					}
				}
				outs.put(tag, out);
			}
			return outs;
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
		public Map<String, Map<Object, Instruction>> flow(DalvikVM vm,
				Instruction inst, Map<String, Map<Object, Instruction>> ins) {
			Map<String, Map<Object, Instruction>> outs = new HashMap<>();
			for (String tag : ins.keySet()) {
				Map<Object, Instruction> in = ins.get(tag);
				Map<Object, Instruction> out = new HashMap<>(in);
				if (in.containsKey(vm.getReg(inst.r0))) {
					out.put(vm.getReg(inst.rdst), in.get(vm.getReg(inst.r0)));
					out.put(vm.getReg(inst.rdst).getData(),
							in.get(vm.getReg(inst.r0)));
				} else if (in.containsKey(vm.getReg(inst.r0).getData())) {
					out.put(vm.getReg(inst.rdst),
							in.get(vm.getReg(inst.r0).getData()));
					out.put(vm.getReg(inst.rdst).getData(),
							in.get(vm.getReg(inst.r0).getData()));
				} else {
					if (inst.r1 != -1 && (in.containsKey(vm.getReg(inst.r1)))) {
						out.put(vm.getReg(inst.rdst),
								in.get(vm.getReg(inst.r1)));
						out.put(vm.getReg(inst.rdst).getData(),
								in.get(vm.getReg(inst.r1)));
					} else if (in.containsKey(vm.getReg(inst.r1).getData())) {
						out.put(vm.getReg(inst.rdst),
								in.get(vm.getReg(inst.r1).getData()));
						out.put(vm.getReg(inst.rdst).getData(),
								in.get(vm.getReg(inst.r1).getData()));
					} else {
						if (in.containsKey(vm.getReg(inst.rdst))) {
							out.remove(vm.getReg(inst.rdst));
						}
					}
				}
				outs.put(tag, out);
			}

			return outs;
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
		public Map<String, Map<Object, Instruction>> flow(DalvikVM vm,
				Instruction inst, Map<String, Map<Object, Instruction>> ins) {
			Map<String, Map<Object, Instruction>> outs = new HashMap<>();
			for (String tag : ins.keySet()) {
				Map<Object, Instruction> in = ins.get(tag);
				Map<Object, Instruction> out = new HashMap<>(in);
				if (inst.r0 != -1 && inst.r1 != -1
						&& (in.containsKey(vm.getReg(inst.r0)))) {
					out.put(vm.getReg(inst.r1), in.get(vm.getReg(inst.r0)));
					out.put(vm.getReg(inst.r1).getData(),
							in.get(vm.getReg(inst.r0)));
				}

				if (inst.r1 != -1 && (in.containsKey(vm.getReg(inst.r1)))) {
					out.put(vm.getReg(inst.r0), in.get(vm.getReg(inst.r1)));
					out.put(vm.getReg(inst.r0).getData(),
							in.get(vm.getReg(inst.r1)));
				}
				outs.put(tag, out);
			}
			return outs;
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
		public Map<String, Map<Object, Instruction>> flow(DalvikVM vm,
				Instruction inst, Map<String, Map<Object, Instruction>> ins) {
			Map<String, Map<Object, Instruction>> outs = new HashMap<>();
			for (String tag : ins.keySet()) {
				Map<Object, Instruction> in = ins.get(tag);
				Map<Object, Instruction> out = new HashMap<>(in);
				// array reg
				// Object[] array = (Object[]) vm.getReg(inst.r0).getData();
				if (vm.getReg(inst.r0).isUsed()) {
					Object array = vm.getReg(inst.r0).getData();
					// index reg
					PrimitiveInfo pindex = null;
					if (vm.getReg(inst.r1).getData() instanceof SymbolicVar) {
						if (((SymbolicVar) vm.getReg(inst.r1).getData())
								.getValue() instanceof PrimitiveInfo) {
							pindex = (PrimitiveInfo) ((SymbolicVar) vm.getReg(
									inst.r1).getData()).getValue();
						} else {
							Log.warn(tag,
									"Array get error! index is not a int.");
						}
					} else {
						if (vm.getReg(inst.r1).getData() instanceof PrimitiveInfo) {
							pindex = (PrimitiveInfo) vm.getReg(inst.r1)
									.getData();
						} else {
							Log.warn(tag, "Wrong type: "
									+ vm.getReg(inst.r1).getData());
							pindex = new PrimitiveInfo(0);
						}
					}

					int index = pindex != null ? pindex.intValue() : -1;
					if (in.containsKey(array)) {
						out.put(vm.getReg(inst.rdst), in.get(array));
						out.put(vm.getReg(inst.rdst).getData(), in.get(array));
					} else if (array instanceof Array && index != -1
							&& in.containsKey(Array.get(array, index))) {
						out.put(vm.getReg(inst.rdst),
								in.get(Array.get(array, index)));
						out.put(vm.getReg(inst.rdst).getData(),
								in.get(Array.get(array, index)));
					}
				}
				outs.put(tag, out);
			}
			return outs;
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
		public Map<String, Map<Object, Instruction>> flow(DalvikVM vm,
				Instruction inst, Map<String, Map<Object, Instruction>> ins) {
			Map<String, Map<Object, Instruction>> outs = new HashMap<>();

			// TODO taint all?
			// dest reg
			Register rdst = vm.getReg(inst.rdst);
			for (String tag : ins.keySet()) {
				Map<Object, Instruction> in = ins.get(tag);
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
				outs.put(tag, out);
			}
			return outs;
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
		public Map<String, Map<Object, Instruction>> flow(DalvikVM vm,
				Instruction inst, Map<String, Map<Object, Instruction>> ins) {
			Map<String, Map<Object, Instruction>> outs = new HashMap<>();
			for (String tag : ins.keySet()) {
				Map<Object, Instruction> in = ins.get(tag);
				Map<Object, Instruction> out = new HashMap<>(in);
				if (in.containsKey(vm.getReg(inst.r0))) {
					out.put(vm.getReg(inst.rdst), in.get(vm.getReg(inst.r0)));
					out.put(vm.getReg(inst.rdst).getData(),
							in.get(vm.getReg(inst.r0)));
				} else {
					if (inst.r1 != -1 && in.containsKey(vm.getReg(inst.r1))) {
						out.put(vm.getReg(inst.rdst),
								in.get(vm.getReg(inst.r1)));
						out.put(vm.getReg(inst.rdst).getData(),
								in.get(vm.getReg(inst.r1)));
					} else {
						if (in.containsKey(vm.getReg(inst.rdst))) {
							out.remove(vm.getReg(inst.rdst));
						}
					}
				}
				outs.put(tag, out);
			}

			return outs;
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
		public Map<String, Map<Object, Instruction>> flow(DalvikVM vm,
				Instruction inst, Map<String, Map<Object, Instruction>> ins) {
			Map<String, Map<Object, Instruction>> outs = new HashMap<>();

			@SuppressWarnings("unchecked")
			Pair<ClassInfo, String> pair = (Pair<ClassInfo, String>) inst
					.getExtra();

			for (String tag : ins.keySet()) {
				Map<Object, Instruction> in = ins.get(tag);
				Map<Object, Instruction> out = new HashMap<>(in);

				try {
					Class<?> clazz = Class.forName(pair.first.toString());
					Field field = clazz
							.getDeclaredField(pair.second.toString());
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
						out.put(vm.getReg(inst.r0),
								in.get(dvmClass.getStatField(fieldName)));
					} else if (in.containsKey(vm.getReg(inst.r0))) {
						out.remove(vm.getReg(inst.r0));
					}
				}
				outs.put(tag, out);
			}

			return outs;
		}
	}

	class TAINT_OP_STATIC_PUT_FIELD implements Rule {
		final String tag = getClass().getSimpleName();

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
		public Map<String, Map<Object, Instruction>> flow(DalvikVM vm,
				Instruction inst, Map<String, Map<Object, Instruction>> ins) {
			Map<String, Map<Object, Instruction>> outs = new HashMap<>();
			for (String tag : ins.keySet()) {
				Map<Object, Instruction> in = ins.get(tag);
				Map<Object, Instruction> out = new HashMap<>(in);
				@SuppressWarnings("unchecked")
				Pair<ClassInfo, String> pair = (Pair<ClassInfo, String>) inst
						.getExtra();
				ClassInfo owner = pair.first;
				String fieldName = pair.second;
				// TODO
				String fieldInfo = "/" + owner.fullName + "/" + fieldName;
				DVMClass dvmClass = vm.getClass(owner);
				if (in.containsKey(vm.getReg(inst.r0))) {
					Log.bb(tag, "SPut at " + vm.getReg(inst.r0));
					out.put(dvmClass.getStatField(fieldName),
							in.get(vm.getReg(inst.r0)));
					if (Settings.isRecordTaintedFields()) {
						if (Settings.isCheckNewTaintedHeapLoc()) {
							if (!Results.getSTaintedFields().containsKey(
									fieldInfo)) {
								Results.setHasNewTaintedHeapLoc(true);
							}
						}

						Results.addTaintedField(
								fieldInfo,
								new Pair<>(dvmClass.getStatField(fieldName), in
										.get(vm.getReg(inst.r0))), Results
										.getSTaintedFields());
					}
				} else if (vm.getReg(inst.r0).isUsed()
						&& in.containsKey(vm.getReg(inst.r0).getData())) {
					out.put(dvmClass.getStatField(fieldName),
							in.get(vm.getReg(inst.r0).getData()));
					if (Settings.isRecordTaintedFields()) {
						if (Settings.isCheckNewTaintedHeapLoc()) {
							Log.bb(tag, "Is new heap loc?");
							if (!Results.getSTaintedFields().containsKey(
									fieldInfo)) {
								Results.setHasNewTaintedHeapLoc(true);
							}
						}
						Results.addTaintedField(
								fieldInfo,
								new Pair<>(dvmClass.getStatField(fieldName), in
										.get(vm.getReg(inst.r0).getData())),
								Results.getSTaintedFields());
					}
				} else {
					if (in.containsKey(dvmClass.getStatField(fieldName))) {
						out.remove(dvmClass.getStatField(fieldName));
					}
					if (Settings.isRecordTaintedFields()
							&& Results.getSTaintedFields().containsKey(
									fieldInfo)) {
						Results.getSTaintedFields().remove(fieldInfo);
					}
				}
				outs.put(tag, out);
			}
			return outs;
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
		public Map<String, Map<Object, Instruction>> flow(DalvikVM vm,
				Instruction inst, Map<String, Map<Object, Instruction>> ins) {
			Map<String, Map<Object, Instruction>> outs = new HashMap<>();
			if (vm.getReg(inst.r1).isUsed()) {
				for (String tag : ins.keySet()) {
					Map<Object, Instruction> in = ins.get(tag);
					Map<Object, Instruction> out = new HashMap<>(in);

					if (in.containsKey(vm.getReg(inst.r1).getData())) {
						out.put(vm.getReg(inst.r1),
								in.get(vm.getReg(inst.r1).getData()));
						Log.bb(tag,
								"Add " + vm.getReg(inst.r1)
										+ " as tainted due to field "
										+ vm.getReg(inst.r1).getData());
					} else if (in.containsKey(vm.getReg(inst.r1))) {
						// value register, has been assigned to new value
						out.remove(vm.getReg(inst.r1));
					}
					outs.put(tag, out);
				}
			}
			return outs;
		}
	}

	class TAINT_OP_INSTANCE_PUT_FIELD implements Rule {
		final String tag = getClass().getSimpleName();

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
		public Map<String, Map<Object, Instruction>> flow(DalvikVM vm,
				Instruction inst, Map<String, Map<Object, Instruction>> ins) {
			Map<String, Map<Object, Instruction>> outs = new HashMap<>();
			for (String tag : ins.keySet()) {
				Map<Object, Instruction> in = ins.get(tag);
				Map<Object, Instruction> out = new HashMap<>(in);
				DVMObject obj = (DVMObject) vm.getReg(inst.r0).getData();
				FieldInfo fieldInfo = (FieldInfo) inst.getExtra();
				// TODO
				String fieldString;
				if (obj.getType().isConvertibleTo(
						ClassInfo.findClass("android.app.Activity"))) {
					fieldString = fieldInfo.owner + "/" + fieldInfo.fieldName;
				} else {
					// FIXME should be the absolute path to the field, such as
					// "Activity1/button1/imei".
					fieldString = obj.getMemUrl() + "/" + fieldInfo.fieldName;

				}

				if (in.containsKey(vm.getReg(inst.r1))) {
					out.put(obj.getFieldObj(fieldInfo),
							in.get(vm.getReg(inst.r1)));
					Log.bb(tag, "Add " + obj.getFieldObj(fieldInfo)
							+ "as tainted due to " + vm.getReg(inst.r1));
					Log.msg(tag, "has " + Results.isHasNewTaintedHeapLoc());
					if (Settings.isRecordTaintedFields()) {
						Log.msg(tag, "Tfields: " + Results.getITaintedFields());
						if (Settings.isCheckNewTaintedHeapLoc()) {
							// if
							// (!Results.getTaintedFields().containsKey(fieldString))
							// {
							boolean found = false;
							for (String str : Results.getITaintedFields()
									.keySet()) {
								if (str.equals(fieldString)) {
									found = true;
									break;
								} else {
									Log.warn(tag, "Not equ " + str + ", "
											+ fieldString);
								}
							}
							if (!found) {
								Results.setHasNewTaintedHeapLoc(true);
								Log.msg(tag, "New tainted heap loc: "
										+ fieldString);
								Log.msg(tag, Results.getITaintedFields());
							}
							// }
						}
						Results.addTaintedField(
								fieldString,
								new Pair<>(obj.getFieldObj(fieldInfo), in
										.get(vm.getReg(inst.r1))), Results
										.getITaintedFields());
						Log.msg(tag, "has " + Results.isHasNewTaintedHeapLoc());
					}
				} else if (in.containsKey(vm.getReg(inst.r1).getData())) {
					out.put(obj.getFieldObj(fieldInfo),
							in.get(vm.getReg(inst.r1).getData()));
					Log.bb(tag, "Add " + obj.getFieldObj(fieldInfo)
							+ "as tainted due to r1 data "
							+ vm.getReg(inst.r1).getData());
					if (Settings.isRecordTaintedFields()) {
						Log.msg(tag, "Tfields: " + Results.getITaintedFields());
						if (Settings.isCheckNewTaintedHeapLoc()) {
							if (!Results.getITaintedFields().containsKey(
									fieldString)) {
								Results.setHasNewTaintedHeapLoc(true);
								Log.msg(tag, "New tainted heap loc: "
										+ fieldString);
							}
						}
						Results.addTaintedField(
								fieldString,
								new Pair<>(obj.getFieldObj(fieldInfo), in
										.get(vm.getReg(inst.r1).getData())),
								Results.getITaintedFields());
					}
				} else {
					if (obj != null
							&& in.containsKey(obj.getFieldObj(fieldInfo))) {
						out.remove(obj.getFieldObj(fieldInfo));
						if (Settings.isRecordTaintedFields()
								&& Results.getITaintedFields().containsKey(
										fieldString)) {
							Results.getITaintedFields().remove(fieldString);
						}
					}
				}
				outs.put(tag, out);
			}
			return outs;
		}
	}

	class TAINT_OP_RETURN_SOMETHING implements Rule {
		final String tag = getClass().getSimpleName();

		@Override
		public Map<String, Map<Object, Instruction>> flow(DalvikVM vm,
				Instruction inst, Map<String, Map<Object, Instruction>> ins) {
			Map<String, Map<Object, Instruction>> outs = new HashMap<>();
			boolean rmRet = true;
			for (String tag : ins.keySet()) {
				Map<Object, Instruction> in = ins.get(tag);
				Map<Object, Instruction> out = new HashMap<>(in);
				for (Object obj : in.keySet()) {
					if (obj instanceof Register
					// Since the callee's reg has already been rm
							&& ((Register) obj).getIndex() == inst.r0) {
						out.put(vm.getReturnReg(), in.get(obj));
						Log.bb(tag, "Add " + vm.getReturnReg()
								+ " as tainted due to " + obj);
						rmRet = false;
					}
				}

				if (rmRet && in.containsKey(vm.getReturnReg())) {
					out.remove(vm.getReturnReg());
					Log.bb(tag, "Remove " + vm.getReturnReg() + "as tainted.");

				}
				outs.put(tag, out);
			}

			return outs;
		}
	}

	class TAINT_OP_RETURN implements Rule {
		final String tag = getClass().getSimpleName();

		@Override
		public Map<String, Map<Object, Instruction>> flow(DalvikVM vm,
				Instruction inst, Map<String, Map<Object, Instruction>> ins) {
			Map<String, Map<Object, Instruction>> outs = ins;
			if (inst.opcode_aux == Instruction.OP_RETURN_SOMETHING) {
				outs = new TAINT_OP_RETURN_SOMETHING().flow(vm, inst, ins);
			}

			return outs;
		}
	}

	private Map<String, Instruction> switchTainted = new HashMap<>();

	class TAINT_OP_SWITCH implements Rule {
		final String tag = getClass().getSimpleName();

		@Override
		public Map<String, Map<Object, Instruction>> flow(DalvikVM vm,
				Instruction inst, Map<String, Map<Object, Instruction>> ins) {
			Map<String, Map<Object, Instruction>> outs = ins;
			if (vm.getReg(inst.r0).isUsed()) {
				for (String tag : ins.keySet()) {
					Map<Object, Instruction> in = ins.get(tag);
					Map<Object, Instruction> out = new HashMap<>(in);
					if (in.containsKey(vm.getReg(inst.r0))) {
						switchTainted.put(tag, in.get(vm.getReg(inst.r0)));
						Log.msg(tag, "SwitchTainted as tainted due to field "
								+ vm.getReg(inst.r0));
					} else if (in.containsKey(vm.getReg(inst.r0).getData())) {
						switchTainted.put(tag,
								in.get(vm.getReg(inst.r0).getData()));
						Log.msg(tag, "SwitchTainted as tainted due to field "
								+ vm.getReg(inst.r0).getData());
					} else {
						// value register, has been assigned to new value
						switchTainted.remove(tag);
					}
					outs.put(tag, out);
				}
			}

			return outs;
		}
	}

	class TAINT_OP_GOTO implements Rule {
		final String tag = getClass().getSimpleName();

		@Override
		public Map<String, Map<Object, Instruction>> flow(DalvikVM vm,
				Instruction inst, Map<String, Map<Object, Instruction>> ins) {
			// FIXME is it a correct way?
			switchTainted.clear();
			;
			return ins;
		}
	}

	public Taint() {
		configs = new HashMap<>();

		byteCodes.put(0x07, new TAINT_OP_CMP());
		byteCodes.put(0x08, new TAINT_OP_IF());
		byteCodes.put(0x0C, new TAINT_OP_INVOKE());
		byteCodes.put(0x02, new TAINT_OP_RETURN());
		byteCodes.put(0x0E, new TAINT_OP_SWITCH());
		byteCodes.put(0x06, new TAINT_OP_GOTO());

		auxByteCodes.put(0x01, new TAINT_OP_MOV_REG());
		auxByteCodes.put(0x02, new TAINT_OP_MOV_CONST());
		// auxByteCodes.put(0x03, new TAINT_OP_RETURN_VOID());
		auxByteCodes.put(0x04, new TAINT_OP_RETURN_SOMETHING());
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
		 */
		auxByteCodes.put(0x12, new TAINT_OP_A_CHECKCAST());

		// auxByteCodes.put(0x13, new TAINT_OP_A_NOT()); auxByteCodes.put(0x14,
		// new TAINT_OP_A_NEG());

		auxByteCodes.put(0x15, new TAINT_OP_MOV_RESULT());
		auxByteCodes.put(0x16, new TAINT_OP_MOV_EXCEPTION());

		auxByteCodes.put(0x17, new TAINT_OP_A_CAST());
		/*
		 * auxByteCodes.put(0x18, new TAINT_OP_IF_EQ()); auxByteCodes.put(0x19,
		 * new TAINT_OP_IF_NE()); auxByteCodes.put(0x1A, new TAINT_OP_IF_LT());
		 * auxByteCodes.put(0x1B, new TAINT_OP_IF_GE()); auxByteCodes.put(0x1C,
		 * new TAINT_OP_IF_GT()); auxByteCodes.put(0x1D, new TAINT_OP_IF_LE());
		 * auxByteCodes.put(0x1E, new TAINT_OP_IF_EQZ()); auxByteCodes.put(0x1F,
		 * new TAINT_OP_IF_NEZ()); auxByteCodes.put(0x20, new
		 * TAINT_OP_IF_LTZ()); auxByteCodes.put(0x21, new TAINT_OP_IF_GEZ());
		 * auxByteCodes.put(0x22, new TAINT_OP_IF_GTZ()); auxByteCodes.put(0x23,
		 * new TAINT_OP_IF_LEZ());
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

		preProcessings = new HashMap<>();
		preProcessings.put(0x0c, new TAINT_PRE_INVOKE());
		
		thisAsTaintedList = new HashSet<>();
		thisAsTaintedList.add("HttpEntityEnclosingRequestBase/setEntity");
	}

	@Override
	public Map<String, Map<Object, Instruction>> runAnalysis(DalvikVM vm,
			Instruction inst, Map<String, Map<Object, Instruction>> ins) {
		Map<String, Map<Object, Instruction>> outs;
		if (byteCodes.containsKey((int) inst.opcode)) {
			outs = byteCodes.get((int) inst.opcode).flow(vm, inst, ins);
		} else if (auxByteCodes.containsKey((int) inst.opcode_aux)) {
			outs = auxByteCodes.get((int) inst.opcode_aux).flow(vm, inst, ins);
		} else {
			Log.bb(tag, "Not a taint op " + inst);
			outs = new HashMap<>(ins);
		}

		for (String tag : outs.keySet()) {
			if (switchTainted.containsKey(tag)
					&& !vm.getAssigned()[0].equals(-1)) {
				outs.get(tag).put(vm.getAssigned()[0], switchTainted.get(tag));
				Log.warn(tag, "Add " + vm.getAssigned()[0]
						+ " as tainted due to switchTaitned.");
			}
		}

		for (Map<Object, Instruction> out : outs.values()) {
			if (out.isEmpty()) {
				continue;
			}

			List<Object> dels = new ArrayList<>();
			for (Object obj : out.keySet()) {
				if (obj instanceof Integer || obj instanceof Boolean) {
					dels.add(obj);
				}
			}

			for (Object obj : dels) {
				out.remove(obj);
			}

			if (out.containsKey(null)) {
				out.remove(null);
			}

			for (Instruction instr : out.values()) {
				if (instr == null) {
					Log.err(tag, "Empty src found at " + inst);
				}
			}
		}

		setCurrtRes(outs);
		return outs;
	}

	@Override
	public Map<String, Map<Object, Instruction>> preProcessing(DalvikVM vm,
			Instruction inst, Map<String, Map<Object, Instruction>> ins) {
		if (configs.isEmpty()) {
			configs.put(tag,
					new PluginConfig(tag, defaultSources, defaultSinks));
		}

		if (ins == null || ins.isEmpty()) {
			ins = new HashMap<>();
			for (String tag : configs.keySet()) {
				Map<Object, Instruction> res = new HashMap<>();
				ins.put(tag, res);
			}
		}

		Map<String, Map<Object, Instruction>> outs;
		if (preProcessings.containsKey((int) inst.opcode)) {
			outs = preProcessings.get((int) inst.opcode).flow(vm, inst, ins);
		} else {
			outs = new HashMap<>(ins);
		}

		for (Map<Object, Instruction> out : outs.values()) {
			if (out.containsKey(null)) {
				out.remove(null);
			}

			for (Instruction instr : out.values()) {
				if (instr == null) {
					Log.err(tag, "Empty src found at " + inst);
				}
			}
		}

		setCurrtRes(outs);
		return outs;
	}

	public static Set<String> getDefaultSources() {
		return defaultSources;
	}

	public static void setDefaultSources(Set<String> defaultSources) {
		Taint.defaultSources = defaultSources;
	}

	public static Set<String> getDefaultSinks() {
		return defaultSinks;
	}

	public static void setDefaultSinks(Set<String> defaultSinks) {
		Taint.defaultSinks = defaultSinks;
	}

	public static String getSootSignature(MethodInfo mi) {
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

		return sootSignature;
	}

}
