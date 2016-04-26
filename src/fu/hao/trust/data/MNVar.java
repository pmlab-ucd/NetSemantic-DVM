package fu.hao.trust.data;

import java.util.HashMap;
import java.util.Map;

import patdroid.core.ClassInfo;
import patdroid.core.FieldInfo;
import fu.hao.trust.dvm.DVMObject;
import fu.hao.trust.utils.Log;
import fu.hao.trust.utils.Pair;

/**
 * @ClassName: ConcreteVar
 * @Description: Representation of a multiple-value non-primitive variable
 *               (MVV).
 * @author: Hao Fu
 * @date: Mar 30, 2016 2:19:13 PM
 */
public class MNVar extends MultiValueVar {
	Map<ClassInfo, DVMObject> values;
	String TAG = getClass().getSimpleName();

	public MNVar(DVMObject value) {
		values = new HashMap<>();
		values.put(value.getType(), value);
	}

	public Map<ClassInfo, DVMObject> getValues() {
		return values;
	}

	public void combineValue(Object value) {
		if (value instanceof DVMObject) {
			DVMObject dvmObjVal = (DVMObject) value;
			combineValue(dvmObjVal);
		} else if (value instanceof MNVar) {
			MNVar mnValue = (MNVar) value;
			for (ClassInfo classInfo : mnValue.values.keySet()) {
				if (values.containsKey(classInfo)) {
					combineValue(mnValue.values.get(classInfo));
				}
			}
		} else {
			Log.err(TAG, "Incorrect value:" + value);
		}
	}
	
	private void combineValue(DVMObject dvmObjVal) {
		if (values.containsKey(dvmObjVal.getType())) {
			for (FieldInfo fieldInfo : dvmObjVal.getFields().keySet()) {
				Object field = values.get(dvmObjVal.getType()).getFieldObj(fieldInfo);
				Pair<Object, ClassInfo> val1 = new Pair<>(field, fieldInfo.getFieldType());
				Pair<Object, ClassInfo> val2 = new Pair<>(dvmObjVal.getFieldObj(fieldInfo), fieldInfo.getFieldType());	
				values.get(dvmObjVal.getType()).setField(fieldInfo, Branch.valCombination(val1, val2));
			}
		} else {
			values.put(dvmObjVal.getType(), dvmObjVal);
		}
	}

	public static MNVar createInstance(Object val1) {
		if (val1 instanceof MNVar) {
			return (MNVar) val1;
		} else {
			return new MNVar((DVMObject) val1);
		}
	}
	

}
