package android.content;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import android.app.Activity;
import patdroid.core.ClassInfo;
import patdroid.util.Pair;
import fu.hao.trust.utils.Log;
import fu.hao.trust.utils.Settings;

public class SharedPreferences {

	Map<String, Object> tmpData = new HashMap<>();
	Map<String, Object> data = new HashMap<>();
	Editor edit = new Editor(this);
	Set<OnSharedPreferenceChangeListener> listeners = new HashSet<>();
	final String TAG = getClass().getSimpleName();

	public Map<String, ?> getAll() {
		return data;
	}

	public String getString(String var1, String var2) {
		Object res = data.get(var1);
		if (res instanceof String) {
			return (String) res;
		}

		return var2;
	}

	@SuppressWarnings("unchecked")
	public Set<String> getStringSet(String var1, Set<String> var2) {
		Object res = data.get(var1);
		try {
			return (Set<String>) res;
		} catch (Exception e) {
			return var2;
		}
	}

	public int getInt(String var1, int var2) {
		Object res = data.get(var1);
		if (res instanceof Integer) {
			return (int) res;
		}

		return var2;
	}

	public long getLong(String var1, long var2) {
		Object res = data.get(var1);
		if (res instanceof Long) {
			return (Long) res;
		}

		return var2;
	}

	public float getFloat(String var1, float var2) {
		Object res = data.get(var1);
		if (res instanceof Float) {
			return (Float) res;
		}

		return var2;
	}

	public boolean getBoolean(String var1, boolean var2) {
		Object res = data.get(var1);
		if (res instanceof Boolean) {
			return (boolean) res;
		}

		return var2;
	}

	public boolean contains(String var1) {
		return data.containsKey(var1);
	}

	public SharedPreferences.Editor edit() {
		return edit;
	}

	public void registerOnSharedPreferenceChangeListener(
			SharedPreferences.OnSharedPreferenceChangeListener var1) {
		listeners.add(var1);
	}

	public void unregisterOnSharedPreferenceChangeListener(
			SharedPreferences.OnSharedPreferenceChangeListener var1) {
		listeners.remove(var1);
	}

	public class Editor {
		private  SharedPreferences preference;
		
		Editor(SharedPreferences preference) {
			this.preference = preference;
		}
		
		public SharedPreferences.Editor putString(String var1, String var2) {
			tmpData.put(var1, var2);
			return this;
		}

		public SharedPreferences.Editor putStringSet(String var1,
				Set<String> var2) {
			tmpData.put(var1, var2);
			return this;
		}

		public SharedPreferences.Editor putInt(String var1, int var2) {
			tmpData.put(var1, var2);
			return this;
		}

		public SharedPreferences.Editor putLong(String var1, long var2) {
			tmpData.put(var1, var2);
			return this;
		}

		public SharedPreferences.Editor putFloat(String var1, float var2) {
			tmpData.put(var1, var2);
			return this;
		}

		public SharedPreferences.Editor putBoolean(String var1, boolean var2) {
			tmpData.put(var1, var2);
			return this;
		}

		public SharedPreferences.Editor remove(String var1) {
			tmpData.remove(var1);
			return this;
		}

		public SharedPreferences.Editor clear() {
			tmpData.clear();
			return this;
		}

		public boolean commit() {
			data.putAll(tmpData);

			if (listeners != null && listeners.size() > 0) {
				// MethodInfo[] onSharedPreferenceChangeds =
				Activity activity = Settings.getVM().getCurrtActivity();
				ClassInfo activityClass = Settings.getVM().getCurrtActivity()
						.getType();
				if (Settings.getEventChain() != null
						&& Settings.getEventChain().size() > 0) {
					Pair<String, String> nextEvent = Settings.getEventChain()
							.get(0);
					String clazzName = nextEvent.getFirst();
					String methodName = nextEvent.getSecond();
					if (methodName.contains("onSharedPreferenceChanged")
							&& clazzName.contains(activityClass.fullName)) {
						// FIXME should not be only called once
						Settings.getEventChain().remove(0);
						@SuppressWarnings("unchecked")
						Pair<Object, ClassInfo>[] args = (Pair<Object, ClassInfo>[]) new Pair[3];
						args[0] = new Pair<Object, ClassInfo>(activity,
								activityClass);
						args[1] = new Pair<Object, ClassInfo>(
								preference,
								ClassInfo
										.findClass("android.content.SharedPreferences"));
						for (String key : tmpData.keySet()) {
							args[2] = new Pair<Object, ClassInfo>(key,
									ClassInfo.findClass("java.lang.String"));
							Settings.getVM().addEventFrame(activity, args,
									methodName);
						}
					}
				}
			}
			tmpData.clear();
			return true;
		}

		public void apply() {
			data.putAll(tmpData);

			if (listeners != null && listeners.size() > 0) {
				// MethodInfo[] onSharedPreferenceChangeds =
				Activity activity = Settings.getVM().getCurrtActivity();
				ClassInfo activityClass = Settings.getVM().getCurrtActivity()
						.getType();
				if (Settings.getEventChain() != null
						&& Settings.getEventChain().size() > 0) {
					Pair<String, String> nextEvent = Settings.getEventChain()
							.get(0);
					String clazzName = nextEvent.getFirst();
					String methodName = nextEvent.getSecond();
					if (methodName.contains("onSharedPreferenceChanged")
							&& clazzName.contains(activityClass.fullName)) {
						// FIXME should not be only called once
						Settings.getEventChain().remove(0);
						@SuppressWarnings("unchecked")
						Pair<Object, ClassInfo>[] args = (Pair<Object, ClassInfo>[]) new Pair[3];
						args[0] = new Pair<Object, ClassInfo>(activity,
								activityClass);
						args[1] = new Pair<Object, ClassInfo>(
								preference,	ClassInfo.findClass("android.content.SharedPreferences"));
						for (String key : tmpData.keySet()) {
							Log.bb(TAG, "tmpKey: " + key);
							args[2] = new Pair<Object, ClassInfo>(key,
									ClassInfo.findClass("java.lang.String"));
							Settings.getVM().addEventFrame(activity, args,
									methodName);
						}
					}
				}
			}
			tmpData.clear();
		}
	}

	public interface OnSharedPreferenceChangeListener {
		void onSharedPreferenceChanged(SharedPreferences var1, String var2);
	}
}
