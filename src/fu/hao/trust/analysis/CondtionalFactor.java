package fu.hao.trust.analysis;

import fu.hao.trust.dvm.DalvikVM.Register;

public class CondtionalFactor {
	Register reg;
	Object obj;
	
	CondtionalFactor (Object obj) {
		this.obj = obj;
	}
	
	@Override
	public String toString() {
		return "Conditional Factor: " + obj.toString();
	}
}
