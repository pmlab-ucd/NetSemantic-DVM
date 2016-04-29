package fu.hao.trust.solver;

import java.util.LinkedList;
import java.util.Map;

import patdroid.core.MethodInfo;
import patdroid.dalvik.Instruction;
import fu.hao.trust.analysis.Plugin;
import fu.hao.trust.data.Branch;
import fu.hao.trust.data.VMState;
import fu.hao.trust.data.VMFullState;
import fu.hao.trust.dvm.DalvikVM;
import fu.hao.trust.utils.Log;

/**
 * @ClassName: BiDirBranch
 * @Description: The conditional statement whose "then" block contains "return",
 *               so that "else if " part also should be executed and bi-direction.
 * @author: Hao Fu
 * @date: Mar 30, 2016 3:27:47 PM
 */
public class BiDirBranch extends Branch {
	public Instruction addRetReg = null;
	
	String TAG = getClass().getName(); 
	
	private VMState state;
	VMFullState fullState;
	// The start point of <rest>, in theory it should be the sum point. Currently only depAPI utilizes it.
	private Instruction restBegin; 
	/**
	 * @fieldName: remove
	 * @fieldType: boolean
	 * @Description: Whether to remove this branch from the bidirBranches.
	 */
	boolean remove;
	
	LinkedList<Map<Plugin, Map<String, Map<Object, Instruction>>>> backPluginRes;

	public BiDirBranch(Instruction inst, int index, MethodInfo method,
			DalvikVM vm) {
		super(inst, index, method);
		Log.warn(TAG, "New BiDirBranch " + this);
		//this.fullState = vm.storeState();
		remove = false;
		state = new VMState(vm);
	}
	
	public void setRmFlag(boolean remove) {
		this.remove = remove;
	}
	
	public boolean getRmFlag() {
		return remove;
	}

	public VMFullState getFullState() {
		return fullState;
	}

	public BiDirBranch(Instruction inst, int index, MethodInfo method) {
		super(inst, index, method);
	}

	public VMState getState() {
		return state;
	}

	public void setState(VMState state) {
		this.state = state;
	}
	
	public void backup(DalvikVM vm) {
		state = new VMState(vm);
	}

	public void restore(DalvikVM vm) {
		state.restore(vm, insts.getLast());
		if (backPluginRes == null) {
			backPluginRes = new LinkedList<>();
		}
		
		backPluginRes.add(vm.getCurrStackFrame().clonePluginRes());
		Log.bb(TAG, "Add backPlugres: " + backPluginRes);
	}
	
	@Override
	public void pluginResComb(DalvikVM vm) {
		Map<Plugin, Map<String, Map<Object, Instruction>>> res = vm.getCurrStackFrame().getPluginRes();
		for (Map<Plugin, Map<String, Map<Object, Instruction>>> bres : backPluginRes) {
			for (Plugin plugin : bres.keySet()) {
				for (String tag : bres.get(plugin).keySet()) {
					Log.bb(TAG, "P " + bres.get(plugin).get(tag));
					Log.bb(TAG, "O " + res.get(plugin).get(tag));
					res.get(plugin).get(tag).putAll(bres.get(plugin).get(tag));
				}
			}
		}
		Log.bb(TAG, "Done pluginRes Combination!");
	}
	
	

	public Instruction getRestBegin() {
		return restBegin;
	}

	public void setRestBegin(Instruction restBegin) {
		this.restBegin = restBegin;
	}
	
}
