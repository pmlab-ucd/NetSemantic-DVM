package fu.hao.trust.solver;

import patdroid.core.ClassInfo;
import patdroid.dalvik.Instruction;
import fu.hao.trust.data.SymbolicVar;
import fu.hao.trust.dvm.DalvikVM;

/**
 * @ClassName: SensCtxVar
 * @Description: Sensitive Contextual Variable (e.g. imei)
 * @author: Hao Fu
 * @date: Mar 15, 2016 8:53:58 PM
 */
public class SensCtxVar extends SymbolicVar{
	
	private ClassInfo type;
	private Instruction src;
	
	boolean on = false;
	
	public SensCtxVar(ClassInfo type, Object value, Instruction src) {
		this.setType(type);
		addConcreteVal(value);
		this.src = src;
	}

	@Override
	public void addConstriant(DalvikVM vm, Instruction inst) {
		// Null or not
		
	}

	@Override
	public Object getValue() {
		return getLastVal();
	}
	
	@Override
	public String toString() {
		return "[SensCtxVar for " + getValue() + "]";
	}

	public ClassInfo getType() {
		return type;
	}

	public void setType(ClassInfo type) {
		this.type = type;
	}

	public Instruction getSrc() {
		return src;
	}

	public void setSrc(Instruction src) {
		this.src = src;
	}

	@Override
	public void setValue(Object value) {
		addConcreteVal(value);
	}

	public boolean isOn() {
		return on;
	}
	

}
