package fu.hao.trust.dvm;

import java.util.HashMap;
import java.util.Map;

import patdroid.core.ClassInfo;
import patdroid.core.FieldInfo;
import patdroid.core.MethodInfo;

public class DVMClass {
	// TODO access flag
	
	private ClassInfo type = null;
	private Map<FieldInfo, Object> staticFields = new HashMap<FieldInfo, Object>();
	// private Map<MethodInfo, DVMethod> methods = new HashMap<>();
	
	DVMClass(ClassInfo type) {
		this.setType(type);
	}

	public ClassInfo getType() {
		return type;
	}

	public void setType(ClassInfo type) {
		this.type = type;
	}
	
	public void setStatField(FieldInfo statFieldInfo, Object obj) {
		staticFields.put(statFieldInfo, obj);
	}
	
	public Object getStatField(FieldInfo statFieldInfo) {
		return staticFields.get(statFieldInfo);
	}
	
	public void invokeStatic(MethodInfo mInfo) {
		//methods.put(minfo, n)
	}
	
	@Override
	public String toString() {
		return type.toString();
	}

}
