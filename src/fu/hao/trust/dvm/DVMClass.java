package fu.hao.trust.dvm;

import java.util.HashMap;
import java.util.Map;

import patdroid.core.ClassInfo;
import patdroid.core.MethodInfo;

public class DVMClass {
	// TODO access flag
	
	private ClassInfo type = null;
	private Map<String, Object> staticFields = new HashMap<>();
	// private Map<MethodInfo, DVMethod> methods = new HashMap<>();
	
	DVMClass(DalvikVM vm, ClassInfo type) {
		this.setType(type);
		if (type.getStaticInitializer() != null) {
			vm.runMethod(type.getStaticInitializer());
		}
	}

	public ClassInfo getType() {
		return type;
	}

	public void setType(ClassInfo type) {
		this.type = type;
	}
	
	public void setStatField(String fieldName, Object obj) {
		staticFields.put(fieldName, obj);
	}
	
	public Object getStatField(String fieldName) {
		return staticFields.get(fieldName);
	}
	
	public void invokeStatic(MethodInfo mInfo) {
		//methods.put(minfo, n)
	}
	
	@Override
	public String toString() {
		return type.toString();
	}

}
