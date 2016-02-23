package fu.hao.trust.android;

import java.util.HashMap;
import java.util.Map;

public class APIs {
	static Map<String, Map<String, API>> apis;
	
	APIs() {
		apis = new HashMap<>();
		Map<String, API> apiPack = new HashMap<>();
		apis.put("android.telephony.TelephonyManager", apiPack);
		
	}

}
