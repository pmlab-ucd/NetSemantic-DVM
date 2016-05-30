package fu.hao.trust.dvm;

import java.util.HashMap;
import java.util.Map;

import fu.hao.trust.utils.Pair;
import patdroid.core.ClassInfo;
import patdroid.core.MethodInfo;

public class DVMClass {
	// TODO access flag

	private ClassInfo type = null;
	private Map<String, Object> staticFields = new HashMap<>();
	DalvikVM vm;
	DVMClass superClass;

	// private Map<MethodInfo, DVMethod> methods = new HashMap<>();

	@SuppressWarnings("unchecked")
	DVMClass(DalvikVM vm, ClassInfo type) {
		this.vm = vm;
		this.setType(type);
		vm.setClass(type, this);

		MethodInfo clinit = type.getStaticInitializer();
		if (clinit != null && clinit.insns != null) {	
			@SuppressWarnings("rawtypes")
			Pair[] callCtxObjs = new Pair[0];
			vm.runInstrumentedMethods(vm.newStackFrame(type, clinit, callCtxObjs, false));
			/*
			vm.resetCallCtx();
			vm.newStackFrame(clinit);
			// vm.getCurrStackFrame().thisObj = null;
			// To force run the constructor.
			vm.setPC(0);
			for (int i = 0; i < clinit.insns.length; i++) {
				vm.interpreter.exec(vm, clinit.insns[i]);
			}*/
			//vm.runMethod(clinit, null, false);
		}
		
		superClass = vm.getClass(type.getSuperClass());
	}

	public ClassInfo getType() {
		return type;
	}

	public void setType(ClassInfo type) {
		this.type = type;
	}

	public void setStatField(String fieldName, Object value) {
		staticFields.put(fieldName, value);
		// FIXME Check "protected" and the fields in the ancestors
		if (superClass != null && superClass.getStatField(fieldName) != null) {
			superClass.setStatField(fieldName, value);
		}
	}

	public Object getStatField(String fieldName) {
		if (staticFields.containsKey(fieldName)) {
			return staticFields.get(fieldName);
		} else if (superClass != null) {
			return superClass.getStatField(fieldName);
		} else {
			return null;
		}
	}

	public void invokeStatic(MethodInfo mInfo) {
		// methods.put(minfo, n)
	}

	public Map<String, Object> getFields() {
		return staticFields;
	}

	/*
	 * @Override public String toString() { return type.toString() + ": " +
	 * vm.heap; }
	 */

	@Override
	public DVMClass clone() {
		DVMClass newClass = new DVMClass(vm, type);
		return newClass;
	}

}
