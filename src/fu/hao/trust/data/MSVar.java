package fu.hao.trust.data;

/**
 * @ClassName: MSVar
 * @Description: Multi-string Value Variable, for string analysis
 * @author: Hao Fu
 * @date: Apr 5, 2016 9:18:24 AM
 */
public class MSVar extends MultiValueVar {
	
	public void addValue(Object val) {
		if (val instanceof String) {
			concreteVals.add((String) val);
		} else if (val instanceof MSVar) {
			concreteVals.addAll(((MSVar) val).getConcreteVals());
		}
	}
	
}
