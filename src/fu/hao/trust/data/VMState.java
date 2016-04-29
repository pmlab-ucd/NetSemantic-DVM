package fu.hao.trust.data;

import java.util.HashMap;
import java.util.Map;

import patdroid.core.ClassInfo;
import patdroid.core.FieldInfo;
import patdroid.dalvik.Instruction;
import fu.hao.trust.analysis.Plugin;
import fu.hao.trust.dvm.DVMClass;
import fu.hao.trust.dvm.DVMObject;
import fu.hao.trust.dvm.DalvikVM;
import fu.hao.trust.dvm.DalvikVM.Register;
import fu.hao.trust.utils.Log;

public class VMState {
	
	Map<Integer, Object[]> backRegs;
	// <DVMObject, [FieldInfo, Object]>
	Map<DVMObject, Object[]> backObjFields;
	Map<DVMClass, Object[]> backClassFields;
	
	Map<Plugin, Map<String, Map<Object, Instruction>>> pluginRes;
	
	private final String TAG = getClass().getSimpleName();
	
	public VMState(DalvikVM vm) {
		backRegs = new HashMap<>();
		Register[] regs = vm.getCurrStackFrame().getRegs();
		for (int i = 0; i < regs.length; i++) {
			if (regs[i].isUsed()) {
				Object[] value = new Object[2];
				value[0] = regs[i].getType();
				value[1] = regs[i].getData();
				backRegs.put(i, value);
			}
		}
		
		backObjFields = new HashMap<>();
		backClassFields = new HashMap<>();
		
		pluginRes = vm.getCurrStackFrame().clonePluginRes(); 
	}
	
	public void saveField(DVMObject dvmObj, FieldInfo fieldInfo, Object data) {
		Object[] value = new Object[2];
		value[0] = fieldInfo;
		value[1] = data;
		backObjFields.put(dvmObj, value);
	}
	
	public void saveField(DVMClass dvmClass, String fieldName, Object data) {
		Object[] value = new Object[2];
		value[0] = fieldName;
		value[1] = data;
		backClassFields.put(dvmClass, value);
	}
	
	public void restore(DalvikVM vm, Instruction cond) {
		Log.msg(TAG, "++++++++++++++++++++++++++++++++BackTrace+++++++++++++++++++++++++++++++++");
		// Restore heap elements.
		for (DVMObject dvmObj : backObjFields.keySet()) {
			FieldInfo fieldInfo = (FieldInfo) backObjFields.get(dvmObj)[0];
			Object data = backObjFields.get(dvmObj)[1];
			dvmObj.setField(fieldInfo, data);
		}
		
		for (DVMClass dvmClass : backClassFields.keySet()) {
			String fieldInfo = (String) backClassFields.get(dvmClass)[0];
			Object data = backObjFields.get(dvmClass)[1];
			dvmClass.setStatField(fieldInfo, data);
		}
		
		// Restore registers.
		for (Integer index : backRegs.keySet()) {
			Object[] value = backRegs.get(index);
			vm.getCurrStackFrame().getRegs()[index].setValue(value[1], (ClassInfo)value[0]);
		}
		
		vm.getCurrStackFrame().setPluginRes(pluginRes);
		
		vm.jump(cond, false);
		vm.getPluginManager().printResults();
	}
	
	public boolean isSaved(DVMObject dvmObj, FieldInfo fieldInfo) {
		if (backObjFields.containsKey(dvmObj)) {
			if (backObjFields.get(dvmObj)[0] == fieldInfo) {
				return true;
			}
		}
		
		return false;
	}
	
	public boolean isSaved(DVMClass clazz, String fieldName) {
		if (backClassFields.containsKey(clazz)) {
			if (backClassFields.get(clazz)[0].equals(fieldName)) {
				return true;
			}
		}
		
		return false;
	}
	
	

}
