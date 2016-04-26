package fu.hao.trust.data;

import java.util.LinkedList;

public class MultiValueVar {
	
	final static String TAG = MultiValueVar.class.getName();
	
	protected LinkedList<Object> concreteVals;
	
	public MultiValueVar() {
		concreteVals = new LinkedList<>();
	}
	
	public void addConcreteVal(Object value) {
		concreteVals.add(value);
	}
	
	public void addConcreteVals(LinkedList<Object> values) {
		for (Object cval : values) {
			concreteVals.add(cval);
		}
	}
	
	public Object getLastVal() {
		if (concreteVals.isEmpty()) {
			return null;
		}
		return concreteVals.getLast();
	}
	
	public LinkedList<Object> getConcreteVals() {
		return concreteVals;
	}
	
	
}
