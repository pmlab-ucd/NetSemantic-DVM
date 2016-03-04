package fu.hao.trust.dvm;

import java.util.HashMap;
import java.util.Map;

import patdroid.core.ClassInfo;
import patdroid.core.MethodInfo;

public class DVMClass {
	// TODO access flag
	
	private ClassInfo type = null;
	private Map<String, Object> staticFields = new HashMap<>();
	DalvikVM vm;
	// private Map<MethodInfo, DVMethod> methods = new HashMap<>();
	
	DVMClass(DalvikVM vm, ClassInfo type) {
		this.vm = vm;
		this.setType(type);
		vm.setClass(type, this);
		
		if (type.getStaticInitializer() != null) {
			vm.stack.add(vm.newStackFrame(type.getStaticInitializer()));
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
	
	public Map<String, Object> getFields() {
		return staticFields;
	}
	
	/*
	@Override
	public String toString() {
		return type.toString() + ": " + vm.heap; 
	}*/
	
	@Override
	public DVMClass clone() {
		DVMClass newClass = new DVMClass(vm, type);
		return newClass;
	}

}
