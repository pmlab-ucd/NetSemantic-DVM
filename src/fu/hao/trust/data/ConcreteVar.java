package fu.hao.trust.data;

import java.util.LinkedList;
import java.util.List;

/**
 * @ClassName: ConcreteVar
 * @Description: Representation a multiple-concrete-value variable.
 * @author: Hao Fu
 * @date: Mar 30, 2016 2:19:13 PM
 */
public class ConcreteVar {
	List<Object> values;
	
	public ConcreteVar(Object value1, Object value2) {
		values = new LinkedList<>();
		values.add(value1);
		values.add(value2);	
	}
	
	public List<Object> getValues() {
		return values;
	}
	
	public void addValue(Object value) {
		values.add(value);
	}
	
}
