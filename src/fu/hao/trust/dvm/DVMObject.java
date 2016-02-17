package fu.hao.trust.dvm;

import java.util.HashMap;
import java.util.Map;

import patdroid.core.ClassInfo;
import patdroid.core.FieldInfo;

public class DVMObject {
	private ClassInfo type = null;
	private Map<FieldInfo, Object> fields = new HashMap<FieldInfo, Object>();
	
	DVMObject(ClassInfo dvmClass) {
		this.setType(dvmClass);
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
	
}
