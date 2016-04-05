package fu.hao.trust.data;

import java.util.HashMap;
import java.util.Map;

import patdroid.core.ClassInfo;
import patdroid.core.FieldInfo;
import fu.hao.trust.dvm.DVMObject;

/**
 * @ClassName: ConcreteVar
 * @Description: Representation of a multiple-value non-primitive variable
 *               (MVV).
 * @author: Hao Fu
 * @date: Mar 30, 2016 2:19:13 PM
 */
public class MNVar extends MultiValueVar {
	Map<ClassInfo, DVMObject> values;

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
		}
	}
	
	private void combineValue(DVMObject dvmObjVal) {
		if (values.containsKey(dvmObjVal.getType())) {
			for (FieldInfo fieldInfo : dvmObjVal.getFields().keySet()) {
				Object field = values.get(dvmObjVal.getType()).getFieldObj(fieldInfo);
				values.get(dvmObjVal.getType()).setField(fieldInfo, valueCombination(field,
						dvmObjVal.getFieldObj(fieldInfo)));
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
