package fu.hao.trust.dvm;

import java.util.HashMap;
import java.util.Map;

import fu.hao.trust.utils.Log;
import patdroid.core.ClassInfo;
import patdroid.core.FieldInfo;
import patdroid.core.MethodInfo;

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
	private int index;

	public DVMObject(DalvikVM vm, ClassInfo type) {
		this.vm = vm;
		if (vm.getClass(type) == null) {
			Log.bb(TAG, "New class representation of " + type);
			vm.setClass(type, new DVMClass(vm, type));
		}
		Log.bb(TAG, "New instance of " + type);
		DVMClass dvmClass = vm.getClass(type);
		this.setType(type);
		this.setDvmClass(dvmClass);
		vm.setObj(type, this);
		
		ClassInfo superClass = type.getSuperClass();
		if (superClass != null && superClass.fullName.contains("Activity")) {
			vm.callbackOwner = this;
		}
		/*
		if (superClass != null) {
			Log.debug(TAG, "Set super class " + superClass);
			Class<?> superClazz;
			try {
				superClazz = Class.forName(superClass.toString());
				setSuperObj(superClazz.newInstance());
			} catch (Exception e) {
				setSuperObj(new DVMObject(vm, superClass));
			}
		}*/
		
		MethodInfo oinit = type.getDefaultConstructor();
		if (oinit != null) {
			Log.bb(TAG, "not empty constructor");
			vm.newStackFrame(oinit);
			vm.setContext(null);
			// vm.getCurrStackFrame().thisObj = null;
			// To force run the constructor.
			for (int i = 0; i < oinit.insns.length; i++) {
				vm.interpreter.exec(vm, oinit.insns[i]);
			}
		}
	}
	
	public DVMObject(DalvikVM vm, ClassInfo type, VMHeap heap) {
		this.vm = vm;
		if (vm.getClass(type) == null) {
			Log.debug(TAG, "new object of " + type);
			vm.setClass(type, new DVMClass(vm, type));
		}
		Log.debug(TAG, "new object of " + type);
		DVMClass dvmClass = vm.getClass(type);
		this.setType(type);
		this.setDvmClass(dvmClass);
		heap.setObj(this);
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
		if (fields.get(fieldInfo) == null) {
			for (FieldInfo field : fields.keySet()) {
				if (fieldInfo.fieldName.equals(field.fieldName)) {
					return fields.get(field);
				}
			}
			return null;
		} else {
			return fields.get(fieldInfo);
		}
	}

	public void setField(FieldInfo fieldInfo, Object obj) {
		if (obj == null) {
			Log.warn(TAG, "null field put!");
		}
		Log.debug(TAG, "put field " + obj);
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
	
	public void setIndex(int index) {
		this.index = index;
	}
	
	public DVMObject clone(VMHeap heap) {
		DVMObject newObj = new DVMObject(vm, type, heap);
		newObj.setFields(fields);
		newObj.setSuperObj(superObj);
		newObj.setDvmClass(dvmClass);
		newObj.setIndex(index);
		return newObj;
	}
	
	public void setTag(int i) {
		index = i;
	}
	
	@Override
	public String toString() {
		return "instance number " + index + "@" + type;
	}

}
