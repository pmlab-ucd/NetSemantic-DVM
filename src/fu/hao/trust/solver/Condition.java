package fu.hao.trust.solver;

import java.util.HashSet;
import java.util.Set;

import fu.hao.trust.analysis.Results;
import fu.hao.trust.dvm.DVMObject;

/**
 * @ClassName: Condition
 * @Description: Representation of unknown condition 
 * @author: Hao Fu
 * @date: Feb 26, 2016 1:58:38 PM
 */
public class Condition {
	Set<Object> result = new HashSet<>();
	int tag;
	
	Condition(int tag) {
		for (Object res : Results.results) {
			if (!(res instanceof DVMObject)) {
				result.add(res);
			}
		}
		
		this.tag = tag;
	}
	
	
}
