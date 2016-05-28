package android.app;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import android.myclasses.GenInstance;
import fu.hao.trust.utils.Log;
import fu.hao.trust.utils.Settings;

public class FragmentManager {

	private final String TAG = getClass().getSimpleName();
	
	Activity activity;

	// For convenient
	private Map<Integer, Fragment> fragments;

	FragmentManager(Activity activity) {
		fragments = new HashMap<Integer, Fragment>();
		this.activity = activity;
	}

	public FragmentTransaction beginTransaction() {
		return new FragmentTransaction();
	}

	public Fragment findFragmentById(int id) {
		if (!fragments.containsKey(id)) {
			if (Activity.getWidgetPool().containsKey(id)) {
				Fragment fragment = GenInstance.getFragment(Settings.getVM(),
						Activity.getWidgetPool().get(id));
				fragment.callDefaultConstructor();
				fragment.myOnAttach(activity);
				fragment.myOnStart();
				fragments.put(id, fragment);
			} else {
				Log.err(TAG, "Cannot find the fragment with id " + id);
			}
		}

		return fragments.get(id);
	}

	public Collection<Fragment> getFragments() {
		return fragments.values();
	}

	public void addFragment(int id, Fragment fragment) {
		fragments.put(id, fragment);
	}

}
