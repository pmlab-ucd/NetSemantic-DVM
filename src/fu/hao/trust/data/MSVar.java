package fu.hao.trust.data;

import java.util.HashSet;
import java.util.Set;

/**
 * @ClassName: MSVar
 * @Description: Multi-string Value Variable, for string analysis
 * @author: Hao Fu
 * @date: Apr 5, 2016 9:18:24 AM
 */
public class MSVar extends MultiValueVar {
	Set<String> values;
	
	public MSVar() {
		values = new HashSet<>();
	}
	
	public void addValue(Object val) {
		if (val instanceof String) {
			values.add((String) val);
		} else if (val instanceof MSVar) {
			values.addAll(((MSVar) val).getValues());
		}
	}
	
	
	public Set<String> getValues() {
		return values;
	}
}
