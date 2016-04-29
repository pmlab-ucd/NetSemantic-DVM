package fu.hao.trust.data;

import fu.hao.trust.dvm.DVMObject;
import fu.hao.trust.dvm.DalvikVM;
import fu.hao.trust.dvm.DalvikVM.Register;
import fu.hao.trust.solver.Unknown;
import fu.hao.trust.utils.Log;
import fu.hao.trust.utils.Pair;
import fu.hao.trust.utils.Settings;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import patdroid.core.ClassInfo;
import patdroid.core.MethodInfo;
import patdroid.core.PrimitiveInfo;
import patdroid.dalvik.Instruction;

public class Branch {
	protected LinkedList<Instruction> insts;
	protected MethodInfo method;
	protected int index;
	protected Instruction sumPoint;
	// The src API call that generates the element of the branch.
	protected LinkedList<Instruction> elemSrcs;
	// The memory element who has conflict values <Memory obj: reg, MVV>
	protected Map<Register, Pair<Object, ClassInfo>[]> conflicts;

	// To avoid inf loop.
	protected Set<Instruction> met;

	// The regs serve as tmp locals, their values should not be combined.
	protected Set<Register> ignoreCombVars;

	static final String TAG = Branch.class.getName();

	public Branch(Instruction inst, int index, MethodInfo method) {
		insts = new LinkedList<>();
		insts.add(inst);
		this.method = method;
		this.index = index;
		sumPoint = method.insns[((int) inst.extra)];
		conflicts = new HashMap<>();
		elemSrcs = new LinkedList<>();
	}

	public void addIgnoreVar(Register reg) {
		if (ignoreCombVars == null) {
			ignoreCombVars = new HashSet<>();
		}
		ignoreCombVars.add(reg);

		if (conflicts.containsKey(reg)) {
			conflicts.remove(reg);
		}
	}

	public Instruction getSumPoint() {
		return sumPoint;
	}

	public void setSumPoint(Instruction sumPoint) {
		this.sumPoint = sumPoint;
	}

	public LinkedList<Instruction> getInstructions() {
		return insts;
	}

	public void addInst(Instruction inst) {
		insts.add(inst);
	}

	public MethodInfo getMethod() {
		return method;
	}

	public LinkedList<Instruction> getElemSrcs() {
		return elemSrcs;
	}

	public void addElemSrc(Instruction elemSrc) {
		Log.bb(TAG, "Add elemSrc " + elemSrc);
		elemSrcs.add(elemSrc);
	}

	public void addVar(Register var) {
		// Object[0]: original val, Object[1]: new value
		@SuppressWarnings("unchecked")
		Pair<Object, ClassInfo>[] pairs = new Pair[2];
		conflicts.put(var, pairs);
	}

	public void addValue(Register var, Pair<Object, ClassInfo> value) {
		ClassInfo type = value.getSecond();
		Object val = value.getFirst();
		if (ignoreCombVars != null && ignoreCombVars.contains(var)) {
			return;
		}

		// Set the original val before the branch
		if (!conflicts.containsKey(var)) {
			// The original value.
			addVar(var);
			conflicts.get(var)[0] = new Pair<>(val, type);
			Log.bb(Settings.getRuntimeCaller(), "Add conflict at var " + var
					+ ", with value " + val);
		} else {
			Object oldVal = conflicts.get(var)[0].getFirst();
			if (val != null && val.equals(oldVal)) {
				return;
			}

			if (checkType(conflicts.get(var)[0], value)) {
				// Overwrite the current value in the block
				conflicts.get(var)[1] = new Pair<>(val, type);
				Log.bb(Settings.getRuntimeCaller(), "Overwrite at var " + var
						+ ", with value " + val);
			} else {
				addIgnoreVar(var);
			}
		}

	}

	public void valCombination() {
		Log.bb(TAG, "Begins value combination:" + this);
		Log.bb(TAG, conflicts);
		for (Register var : conflicts.keySet()) {
			Log.bb(TAG, "Value combination for var " + var);
			Pair<Object, ClassInfo> currtVal = null;
			for (Pair<Object, ClassInfo> cVal : conflicts.get(var)) {
				if (cVal != null) {
					currtVal = valCombination(currtVal, cVal);
					Log.bb(TAG, "Currt val " + currtVal
							+ ", with original value " + cVal);
				}
			}
			var.setValue(currtVal);
		}
	}
	
	public void pluginResComb(DalvikVM vm) {
		
	}

	public static Pair<Object, ClassInfo> valCombination(
			Pair<Object, ClassInfo> val1, Pair<Object, ClassInfo> val2) {
		/*
		 * if (!checkRuntimeType(val1, val2)) { Log.err(TAG,
		 * "Inconsistent type: " + val1 + ", " + val2); }
		 */
		if (val1 == null || val1.getFirst().equals(val2.getFirst())) {
			return val2;
		}

		Object data1 = val1.getFirst();
		Object data2 = val2.getFirst();

		if (data1 instanceof SymbolicVar) {
			// When is primitive or reflecable types.
			if (data2 instanceof SymbolicVar) {
				((SymbolicVar) data1).addConcreteVals(((SymbolicVar) data2)
						.getConcreteVals());
			} else if (data2 == null) {
				((SymbolicVar) data1).addConcreteVal(null);
			} else if (data2 instanceof DVMObject) {
				Log.err(TAG, "Incorrect type!");
			} else {
				((SymbolicVar) data1).addConcreteVal(data2);
			}

			return val1;
		} else if (data1 instanceof DVMObject || data1 instanceof MNVar) {
			MNVar mnv = MNVar.createInstance(data1);
			if (data2 instanceof DVMObject || data2 instanceof MNVar) {
				mnv.combineValue(data2);
				// FIXME Multiple types? use java.Object?
				return new Pair<Object, ClassInfo>(mnv, val1.getSecond());
			} else {
				Log.err(TAG, "Incorrect type!");
				return null;
			}
		} else {
			if (data2 instanceof SymbolicVar) {
				((SymbolicVar) data2).addConcreteVal(data1);
				return val2;
			} else if (data2 instanceof DVMObject) {
				Log.err(TAG, "Incorrect type!");
				return null;
			} else {
				// FIXME Multiple types? use java.Object?
				Unknown unknown = new Unknown(val1.getSecond());
				return new Pair<Object, ClassInfo>(unknown, val1.getSecond());
			}
		}
	}

	public static boolean checkClassInfo(ClassInfo type1, ClassInfo type2) {
		if (type1 == null || type2 == null) {
			Log.warn(TAG, "Null type");
		}
		if (type1 == null || type2 == null || type1.isConvertibleTo(type2)
				|| type2.isConvertibleTo(type1)) {
			return true;
		} else {
			Log.bb(TAG, "Inconsistent ClassInfo: " + type1 + ", " + type2);
			return false;
		}
	}

	public static boolean checkType(Pair<Object, ClassInfo> oldVal,
			Pair<Object, ClassInfo> newVal) {
		ClassInfo oldType = oldVal.getSecond();
		ClassInfo newType = newVal.getSecond();
		if (oldType.isConvertibleTo(newType)) {
			if ((oldType.equals(ClassInfo.primitiveVoid) || newType
					.equals(ClassInfo.primitiveVoid))
					&& checkRuntimeType(oldVal.getFirst(), newVal.getFirst())) {
				Log.bb(TAG, "Type-old: " + oldVal.getSecond() + ", Type-new: "
						+ newVal.getSecond());
				return true;
			}
		}

		return false;
	}

	public static boolean checkRuntimeType(Object data1, Object data2) {
		if (data1 instanceof DVMObject) {
			if (data2 instanceof DVMObject || data2 instanceof MNVar
					|| data2 == null) {
				return true;
			} else {
				return false;
			}
		}

		if (data1 instanceof PrimitiveInfo) {
			if (data2 instanceof PrimitiveInfo || data2 == null
					|| data2 instanceof Unknown) {
				return true;
			} else {
				return false;
			}
		}

		if (data1 instanceof SymbolicVar) {
			return true;
		}

		// FIXME use patdroid.getClass to see the reflectable if can be casted.

		return false;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("[Branch: " + index);

		for (Instruction inst : insts) {
			sb.append("--[cond: " + inst + "]");
		}

		sb.append("[sumpoint: " + sumPoint + "]" + "@" + method.name + "]");
		return sb.toString();
	}

	public void addMet(Instruction cond) {
		if (met == null) {
			met = new HashSet<>();
		}
		met.add(cond);
	}

	public boolean Met(Instruction cond) {
		return met.contains(cond);
	}

}
