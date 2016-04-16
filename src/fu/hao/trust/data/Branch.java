package fu.hao.trust.data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import fu.hao.trust.dvm.DVMObject;
import fu.hao.trust.dvm.DalvikVM.Register;
import fu.hao.trust.solver.Unknown;
import fu.hao.trust.utils.Log;
import fu.hao.trust.utils.Settings;
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
	protected Set<Instruction> elemSrcs;
	// The memory element who has conflict values <Memory obj: reg, MVV>
	protected Map<Register, Object[]> conflicts; 
	
	// To avoid inf loop.
	protected Set<Instruction> met;
	
	static final String TAG = Branch.class.getName();

	public Branch(Instruction inst, int index, MethodInfo method) {
		insts = new LinkedList<>();
		insts.add(inst);
		this.method = method;
		this.index = index;
		sumPoint = method.insns[((int) inst.extra)];
		conflicts = new HashMap<>();
		elemSrcs = new HashSet<>();
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
	
	public Set<Instruction> getElemSrcs() {
		return elemSrcs;
	}
	
	public void addElemSrc(Instruction elemSrc) {
		elemSrcs.add(elemSrc);
	}
	
	public void addVar(Register var) {
		// Object[0]: original val, Object[1]: new value
		conflicts.put(var, new Object[2]);
	}
	
	public void addValue(Register var, Object val) {
		// Set the original val before the branch
		if (!conflicts.containsKey(var)) {
			// The original value.
			addVar(var);
			conflicts.get(var)[0] = val;
		} else {
			// Over write the current value in the block
			conflicts.get(var)[1] = val;
		}

		Log.bb(Settings.getRuntimeCaller(), "Add conflict at var " + var + ", with value " + val);
	}
	
	public void valCombination() {
		Log.bb(TAG, "Begins value combination:" + this);
		Log.bb(TAG, conflicts);
		for (Register var : conflicts.keySet()) {
			Log.bb(TAG, "Value combination for var " + var);
			Object currtVal = null;
			for (Object val : conflicts.get(var)) {
				currtVal = valCombination(currtVal, val);
				Log.bb(TAG, "Currt val " + currtVal + ", with original value " + val);
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
		
		if (!checkType(val1, val2)) {
			Log.err(TAG, "Inconsistent type: " + val1 + ", " + val2);
		}
		
		if (val1 instanceof PrimitiveInfo || val1 instanceof Unknown) {
			return new Unknown(ClassInfo.primitiveInt);
		} else if (val1 instanceof SymbolicVar) {
			return val1;
		} else if (val1 instanceof String || val1 instanceof MSVar) {
			// TODO handle MSVar
			MSVar msv = new MSVar();
			msv.addValue(val2);
			return msv;
		} else if (val1 instanceof DVMObject){
			MNVar mnv = MNVar.createInstance(val1);
			mnv.combineValue(val2);
			return mnv;
		} else {
			// FIXME
			return val1;
		}
	}
	
	public static boolean checkType(Object val1, Object val2) {
		if (val1 == null || val2 == null) {
			return true;
		}
		
		if (val1 instanceof PrimitiveInfo || val1 instanceof SymbolicVar) {
			if (val2 instanceof PrimitiveInfo || val2 instanceof SymbolicVar) {
				return true;
			} else {
				return false;
			}
		} else if (val1 instanceof String || val1 instanceof MSVar || val1 instanceof PrimitiveInfo && ((PrimitiveInfo) val1).intValue() == 0) {
			if (val2 instanceof String || val2 instanceof MSVar || val2 instanceof PrimitiveInfo && ((PrimitiveInfo) val2).intValue() == 0) {
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
