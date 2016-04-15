package fu.hao.trust.analysis;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import patdroid.dalvik.Instruction;
import fu.hao.trust.dvm.DalvikVM;

public abstract class Plugin {
	private Map<String, Map<Object, Instruction>> currtRes;
	private Method method;
	
	public abstract Map<String, Map<Object, Instruction>> runAnalysis(DalvikVM vm, Instruction inst, Map<String,Map<Object, Instruction>> ins);
	
	public void reset() {
		currtRes = new HashMap<>();
		setMethod(null);
	}
	
	public abstract void preprocessing(DalvikVM vm, Instruction inst);

	public Map<String, Map<Object, Instruction>> getCurrtRes() {
		return currtRes;
	}

	public void setCurrtRes(Map<String, Map<Object, Instruction>> currtRes) {
		this.currtRes = currtRes;
	}

	public Method getMethod() {
		return method;
	}

	public void setMethod(Method method) {
		this.method = method;
	}
}
