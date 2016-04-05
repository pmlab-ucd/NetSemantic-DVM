package fu.hao.trust.data;

import java.util.LinkedList;
import java.util.List;

import patdroid.core.ClassInfo;
import patdroid.core.PrimitiveInfo;
import fu.hao.trust.solver.Unknown;
import fu.hao.trust.utils.Log;

public class MultiValueVar {
	
	final static String TAG = MultiValueVar.class.getName();
	
	List<Object> concreteVals;
	
	public MultiValueVar() {
		concreteVals = new LinkedList<>();
	}
	
	public void addConcreteVal(Object value) {
		concreteVals.add(value);
	}
	
	public static Object valueCombination(Object val1, Object val2) {
		if (!checkType(val1, val2)) {
			Log.err(TAG, "Inconsistent type");
		}
		
		if (val1 instanceof PrimitiveInfo || val1 instanceof SymbolicVar) {
			return new Unknown(ClassInfo.primitiveInt);
		} else if (val1 instanceof String || val1 instanceof MSVar) {
			// TODO handle MSVar
			MSVar msv = new MSVar();
			msv.addConcreteVal(val2);
			return msv;
		} else {
			MNVar mnv = MNVar.createInstance(val1);
			mnv.combineValue(val2);
			return mnv;
		}
	}
	
	private static boolean checkType(Object val1, Object val2) {
		if (val1 instanceof PrimitiveInfo || val1 instanceof SymbolicVar) {
			if (val2 instanceof PrimitiveInfo || val2 instanceof SymbolicVar) {
				return true;
			} else {
				return false;
			}
		} else if (val1 instanceof String || val1 instanceof MSVar) {
			if (val2 instanceof String || val2 instanceof MSVar) {
				return true;
			} else {
				return false;
			}
		} else {
			return true;
		}
	}
}
