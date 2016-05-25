package fu.hao.trust.dvm;

import java.util.HashMap;
import java.util.Map;
import patdroid.core.ClassInfo;
import android.app.Service;

/**
 * @ClassName: ServicePool
 * @Description: Store living services, services are natural singletons
 * @author: Hao Fu
 * @date: May 24, 2016 4:54:17 PM
 */
public class ServicePool { 
	private Map<ClassInfo, Service> pool;
	
	ServicePool() {
		pool = new HashMap<>();
	}
	
	public void addService(Service service) {
		pool.put(service.getType(), service);
	}
	
	public void rmService(Service service) {
		if (pool.containsKey(service.getType())) {
			pool.remove(service.getType());
		}
	}
	
	public Service getService(ClassInfo type) {
		return pool.get(type);
	}
	
}
