package fu.hao.trust.dvm;

import java.util.HashSet;
import java.util.Set;

import patdroid.core.MethodInfo;

/**
 * @ClassName: DVMethod
 * @Description: The method representation for run-time method
 * @author: hao
 * @date: Feb 17, 2016 11:12:55 AM
 */
public class DVMethod {
	// TODO access flag
	
	MethodInfo info;
	Set<Object> params = new HashSet<>();
	
	DVMethod(MethodInfo info) {
		this.info = info;
	}

}
