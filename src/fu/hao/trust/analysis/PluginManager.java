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
	private final String TAG = getClass().toString();
	
	List<Plugin> plugins;
	Method method;
	Instruction condition;
	private Map<Plugin, Map<Object, Instruction>> currtRes;
	
	public PluginManager() {
		plugins = new ArrayList<>();
		currtRes = new HashMap<>();
	}
	
	public void addPlugin(Plugin plugin) {
		plugins.add(plugin);
		Map<Object, Instruction> res = new HashMap<>();
		plugin.currtRes = res;
		currtRes.put(plugin, plugin.currtRes);
	}
	
	public Method getMethod() {
		return method;
	}
	
	public void setMethod(Method method) {
		this.method = method; 
		for (Plugin plugin : plugins) {
			plugin.method = method;
		}
	}
	
	public Instruction getCondition() {
		return condition;
	}
	
	public void setCondition(Instruction condition) {
		this.condition = condition; 
		for (Plugin plugin : plugins) {
			plugin.condition = condition;
		}
	}
	
	public void runAnalysis(DalvikVM vm, Instruction inst) {
		Log.bb(TAG, "run analysis " + currtRes.keySet().size());
		for (Plugin plugin : currtRes.keySet()) {
			plugin.currtRes = plugin.runAnalysis(vm, inst, plugin.currtRes);
			currtRes.put(plugin, plugin.currtRes);
			Log.bb(TAG, "analysis " + inst);
		}
	}
	
	public Map<Plugin, Map<Object, Instruction>> cloneCurrtRes() {
		Map<Plugin, Map<Object, Instruction>> cloned = new HashMap<>();
		for (Plugin plugin : currtRes.keySet()) {
			Map<Object, Instruction> newMap = new HashMap<>(currtRes.get(plugin));
			cloned.put(plugin, newMap);
		}
		
		return cloned;
	}

	public Map<Plugin, Map<Object, Instruction>> getCurrRes() {
		return currtRes;
	}
	
	public void setCurrRes(Map<Plugin, Map<Object, Instruction>> currtRes) {
		this.currtRes = currtRes;
	}

	public void checkInst(Instruction inst) {
		for (Plugin plugin : currtRes.keySet()) {
			if (plugin.interested != null && plugin.interested.contains(inst)) {
				plugin.interested.remove(inst);
				Log.msg(TAG, "Found interested inst " + inst, ", rm it.");
				Log.bb(TAG, "Left interested " + plugin.interested);
			}
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

}
