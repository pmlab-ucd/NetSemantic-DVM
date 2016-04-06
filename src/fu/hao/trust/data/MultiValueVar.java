package fu.hao.trust.data;

import java.util.LinkedList;
import java.util.List;

public class MultiValueVar {
	
	final static String TAG = MultiValueVar.class.getName();
	
	List<Object> concreteVals;
	
	public MultiValueVar() {
		concreteVals = new LinkedList<>();
	}
	
	public void addConcreteVal(Object value) {
		concreteVals.add(value);
	}
	
	
}
