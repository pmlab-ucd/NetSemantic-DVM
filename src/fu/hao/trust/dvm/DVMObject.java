package fu.hao.trust.dvm;

import java.util.HashMap;
import java.util.Map;

import fu.hao.trust.utils.Log;
import patdroid.core.ClassInfo;
import patdroid.core.FieldInfo;

/**
 * @ClassName: DVMObject
 * @Description: To represent a instance of an user-defined class (not def as a
 *               primitive, java.lang and android sdk)
 * @author: Hao Fu
 * @date: Feb 23, 2016 11:28:14 AM
 */
public class DVMObject {
	private ClassInfo type = null;
	private Map<FieldInfo, Object> fields = new HashMap<FieldInfo, Object>();
	private DVMClass dvmClass = null;
	private final String TAG = getClass().toString();
	private Object superObj = null;
	private DalvikVM vm;

	public DVMObject(DalvikVM vm, ClassInfo type) {
		this.vm = vm;
		if (vm.getClass(type) == null) {
			Log.debug(TAG, "new object of " + type);
			vm.setClass(type, new DVMClass(vm, type));
		}
		Log.debug(TAG, "new object of " + type);
		DVMClass dvmClass = vm.getClass(type);
		this.setType(type);
		this.setDvmClass(dvmClass);
	}

	public ClassInfo getType() {
		return type;
	}

	public void setType(ClassInfo type) {
		this.type = type;
	}

	public Map<FieldInfo, Object> getFields() {
		return fields;
	}

	public void setFields(Map<FieldInfo, Object> fields) {
		this.fields = fields;
	}

	public Object getFieldObj(FieldInfo fieldInfo) {
		return fields.get(fieldInfo);
	}

	public void setField(FieldInfo fieldInfo, Object obj) {
		fields.put(fieldInfo, obj);
	}

	public DVMClass getDvmClass() {
		return dvmClass;
	}

	public void setDvmClass(DVMClass dvmClass) {
		this.dvmClass = dvmClass;
	}

	public Object getSuperObj() {
		return superObj;
	}

	public void setSuperObj(Object superObj) {
		this.superObj = superObj;
	}
	
	@Override
	public DVMObject clone() {
		DVMObject newObj = new DVMObject(vm, type);
		newObj.setFields(fields);
		newObj.setSuperObj(superObj);
		newObj.setDvmClass(dvmClass);
		return newObj;
	}

}
