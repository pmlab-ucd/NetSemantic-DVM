package android.content;

import java.util.HashSet;
import java.util.Set;

public class IntentFilter {
	
	Set<String> actions;
	
	public IntentFilter() {
		actions = new HashSet<>();
	}
	
    public IntentFilter(String action) {
    	actions = new HashSet<>();
    	actions.add(action);
    }

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
