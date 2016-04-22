package fu.hao.trust.data;

import fu.hao.trust.dvm.DVMObject;
import fu.hao.trust.dvm.DalvikVM.Register;
import fu.hao.trust.solver.Unknown;
import fu.hao.trust.utils.Log;
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
	protected Map<Register, ConcreteVal[]> conflicts;

	// To avoid inf loop.
	protected Set<Instruction> met;

	// The regs serve as tmp locals, their values should not be combined.
	protected Set<Register> ignoreCombVars;

	static final String TAG = Branch.class.getName();

	class ConcreteVal {
		ClassInfo type;
		Object val;

		ConcreteVal(ClassInfo type, Object val) {
			this.type = type;
			this.val = val;
		}

		@Override
		public String toString() {
			return "[" + val + ":" + val.getClass() + ":" + type + "]";
		}
	}

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
		conflicts.put(var, new ConcreteVal[2]);
	}

	public void addValue(Register var, ClassInfo type, Object val) {
		if (ignoreCombVars != null && ignoreCombVars.contains(var)) {
			return;
		}

		// Set the original val before the branch
		if (!conflicts.containsKey(var)) {
			// The original value.
			addVar(var);
			conflicts.get(var)[0] = new ConcreteVal(type, val);
			Log.bb(Settings.getRuntimeCaller(), "Add conflict at var " + var
					+ ", with value " + val);
		} else {
			Object oldVal = conflicts.get(var)[0].val;
			ClassInfo oldType = conflicts.get(var)[0].type;
			if (val != null && val.equals(oldVal)) {
				return;
			}

			if (checkType(val, oldVal, type, oldType)) {
				// Overwrite the current value in the block
				conflicts.get(var)[1] = new ConcreteVal(type, val);
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
			Object currtVal = null;
			for (ConcreteVal cVal : conflicts.get(var)) {
				currtVal = valCombination(currtVal, cVal.val);
				Log.bb(TAG, "Currt val " + currtVal + ", with original value "
						+ cVal.val);
			}

			var.setData(currtVal);
		}
	}

	public static Object valCombination(Object val1, Object val2) {
		if (val1 == null || val1 == val2) {
			return val2;
		}

		if (val2 == null) {
			return val1;
		}

		if (!checkRuntimeType(val1, val2)) {
			Log.err(TAG,
					"Inconsistent type: " + val1 +
							 ", " + val2);

		}

		if (val1 instanceof PrimitiveInfo || val1 instanceof Unknown) {
			return new Unknown(ClassInfo.primitiveInt);
		} else if (val1 instanceof SymbolicVar || val2 instanceof SymbolicVar) {
			return val1;
		} else if (val1 instanceof String || val1 instanceof MSVar) {
			MSVar msv = new MSVar();
			msv.addValue(val1);
			if (val2 instanceof String || val2 instanceof MSVar) {
				msv.addValue(val2);
			}
			return msv;
		} else if (val1 instanceof DVMObject) {
			MNVar mnv = MNVar.createInstance(val1);
			mnv.combineValue(val2);
			return mnv;
		} else {
			// FIXME
			return val1;
		}
	}


	public static boolean checkClassInfo(ClassInfo type1, ClassInfo type2) {
		if (type1 == null || type2 == null) {
			Log.warn(TAG, "Null type");
		}
		if (type1 == null || type1.isConvertibleTo(type2) || type2 == null || type2.isConvertibleTo(type1)) {
			return true;
		} else {
			Log.bb(TAG, "Inconsistent ClassInfo: " + type1 + ", " + type2);
			return false;
		}
	}

	public static boolean checkType(Object val1, Object val2, ClassInfo type1,
			ClassInfo type2) {
		if (checkClassInfo(type1, type2)) {
			return checkRuntimeType(val1, val2);
		} else {
			return false;
		}
	}

	public static boolean checkRuntimeType(Object val1, Object val2) {
		if (val1 == null || val2 == null || val1 instanceof Integer
				&& (int) val1 == 0 || val1 instanceof PrimitiveInfo
				&& ((PrimitiveInfo) val1).isZero()) {
			return true;
		}

		if (val1 instanceof PrimitiveInfo || val1 instanceof SymbolicVar) {
			if (val2 instanceof PrimitiveInfo || val2 instanceof SymbolicVar) {
				return true;
			} else {
				return false;
			}
		} else if (val1 instanceof String || val1 instanceof MSVar) {
			if (val2 instanceof Integer || val2 instanceof SymbolicVar
					|| val2 instanceof String || val2 instanceof MSVar
					|| val2 instanceof PrimitiveInfo
					&& ((PrimitiveInfo) val2).intValue() == 0) {
				return true;
			} else {
				return false;
			}
		} else if (val1 instanceof DVMObject || val1 instanceof DVMObject) {
			return true;
		} else {
			return false;
		}
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
