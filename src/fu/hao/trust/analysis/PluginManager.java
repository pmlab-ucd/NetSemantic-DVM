package fu.hao.trust.analysis;

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
	
	public PluginManager() {
		plugins = new ArrayList<>();
	}
	
	public void addPlugin(Plugin plugin) {
		plugins.add(plugin);
		Map<String, Map<Object, Instruction>> res = new HashMap<>();
		plugin.setCurrtRes(res);
	}
	
	public List<Plugin> getPlugins() {
		return plugins;
	}
	
	public void runAnalysis(DalvikVM vm, Instruction inst) {
		for (Plugin plugin : plugins) {
			plugin.runAnalysis(vm, inst, plugin.getCurrtRes());
		}
	}
	
	public Map<Plugin, Map<String, Map<Object, Instruction>>> cloneCurrtRes() {
		Map<Plugin, Map<String, Map<Object, Instruction>>> cloned = new HashMap<>();
		for (Plugin plugin : plugins) {
			Map<String, Map<Object, Instruction>> newMap = new HashMap<>(plugin.getCurrtRes());
			cloned.put(plugin, newMap);
		}
		
		return cloned;
	}

	public void setCurrRes(Map<Plugin, Map<String, Map<Object, Instruction>>> currtResults) {
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
			for (String tag : plugin.getCurrtRes().keySet()) {
				Log.msg(TAG, tag + " Tainted Res: " + plugin.getCurrtRes().get(tag));
			}
		}
	}
	
	public void preprossing(DalvikVM vm, Instruction inst) {
		for (Plugin plugin : plugins) {
			plugin.preProcessing(vm, inst, plugin.getCurrtRes());
		}
	}

}
