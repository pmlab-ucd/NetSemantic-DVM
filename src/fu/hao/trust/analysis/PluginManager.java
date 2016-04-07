package fu.hao.trust.analysis;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fu.hao.trust.dvm.DalvikVM;
import fu.hao.trust.utils.Log;
import patdroid.dalvik.Instruction;

public class PluginManager {
	private final String TAG = getClass().getSimpleName();
	
	private List<Plugin> plugins;
	private Method method;
	
	public PluginManager() {
		plugins = new ArrayList<>();
	}
	
	public void addPlugin(Plugin plugin) {
		plugins.add(plugin);
		Map<Object, Instruction> res = new HashMap<>();
		plugin.setCurrtRes(res);
	}
	
	public List<Plugin> getPlugins() {
		return plugins;
	}
	
	public Method getMethod() {
		return method;
	}
	
	public void setMethod(Method method) {
		this.method = method; 
		for (Plugin plugin : plugins) {
			plugin.setMethod(method);
		}
	}
	
	public void runAnalysis(DalvikVM vm, Instruction inst) {
		for (Plugin plugin : plugins) {
			plugin.runAnalysis(vm, inst, plugin.getCurrtRes());
		}
	}
	
	public Map<Plugin, Map<Object, Instruction>> cloneCurrtRes() {
		Map<Plugin, Map<Object, Instruction>> cloned = new HashMap<>();
		for (Plugin plugin : plugins) {
			Map<Object, Instruction> newMap = new HashMap<>(plugin.getCurrtRes());
			cloned.put(plugin, newMap);
		}
		
		return cloned;
	}

	public void setCurrRes(Map<Plugin, Map<Object, Instruction>> currtResults) {
		for (Plugin plugin : plugins) {
			plugin.setCurrtRes(currtResults.get(plugin));
		}
	}

	public void reset() {
		for (Plugin plugin : plugins) {
			plugin.reset();
		}
	}

	public boolean isEmpty() {
		return plugins.isEmpty();
	}
	
	public void printResults() {
		for (Plugin plugin : plugins) {
			Log.msg(TAG, plugin.getClass().getSimpleName() + " Tainted Res: " + plugin.getCurrtRes());
		}
	}
	
	public void preprossing(DalvikVM vm, Instruction inst) {
		for (Plugin plugin : plugins) {
			plugin.preprocessing(vm, inst);
		}
	}

}
