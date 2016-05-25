package android.app;

import java.util.HashMap;
import java.util.Map;

import fu.hao.trust.utils.Settings;

public class FragmentTransaction  {
	
	Map<Integer, Fragment> fragments;
	
	public FragmentTransaction beginTransaction() {
		return this;
	}
	
	public FragmentTransaction add(int var1, Fragment fragment) {
		if (fragments == null) {
			fragments = new HashMap<>();
		}
		
		fragments.put(var1, fragment);
		fragment.myOnAttach(Settings.getVM().getCurrtActivity());
		return this;
	}
	
	public int commit() {
		for (Fragment fragment : fragments.values()) {
			fragment.onStart();
		}
		return 1;
	}

}
