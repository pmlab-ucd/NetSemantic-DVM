package android.content;

import java.util.HashSet;
import java.util.Set;

public class IntentFilter {
	
	Set<String> actions = new HashSet<>();

    public final void addAction(String action) {
    	actions.add(action);
    }
    
    public final boolean matchAction(String action) {
    	if (actions.contains(action)) {
    		return true;
    	}
    	
    	return false;
    }

}
