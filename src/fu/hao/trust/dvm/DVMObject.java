package fu.hao.trust.dvm;

import java.util.HashMap;
import java.util.Map;

import patdroid.core.ClassInfo;
import patdroid.core.FieldInfo;

public class DVMObject {
	private ClassInfo type = null;
	private Map<FieldInfo, Object> fields = new HashMap<FieldInfo, Object>();
	private DVMClass dvmClass = null;
	
	DVMObject(DalvikVM vm, ClassInfo type) {
		if (vm.heap.getClass(type) == null) {
			vm.heap.setClass(type, new DVMClass(type));
		}
		DVMClass dvmClass = vm.heap.getClass(type);
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
	
}
