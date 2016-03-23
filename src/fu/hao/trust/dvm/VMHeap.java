package fu.hao.trust.dvm;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import patdroid.core.ClassInfo;
import fu.hao.trust.utils.Log;

public class VMHeap {
	Map<ClassInfo, DVMClass> dvmClasses = new HashMap<>();
	Map<ClassInfo, Set<DVMObject>> dvmObjs = new HashMap<>();
	
	private final String TAG = getClass().getSimpleName();

	public void setClass(ClassInfo type, DVMClass dvmClass) {
		dvmClasses.put(type, dvmClass);
	}

	public void setClass(DVMClass dvmClass) {
		dvmClasses.put(dvmClass.getType(), dvmClass);
	}

	public void setClass(DalvikVM vm, ClassInfo type) {
		Log.bb(TAG, "new class representation for " + type + " at "
				+ vm.heap);
		dvmClasses.put(type, new DVMClass(vm, type));
	}

	public DVMClass getClass(DalvikVM vm, ClassInfo type) {
		Log.bb(TAG, "getClass " + type + " at " + vm.heap);
		if (!dvmClasses.containsKey(type)) {
			setClass(vm, type);
		}
		return dvmClasses.get(type);
	}

	public void setObj(ClassInfo type, DVMObject dvmObj) {
		if (dvmObjs.get(type) == null) {
			dvmObjs.put(type, new HashSet<DVMObject>());
		}
		dvmObjs.get(type).add(dvmObj);
		dvmObj.setTag(dvmObjs.get(type).size());
	}

	public void setObj(DVMObject dvmObj) {
		if (dvmObjs.get(dvmObj.getType()) == null) {
			dvmObjs.put(dvmObj.getType(), new HashSet<DVMObject>());
		}
		dvmObjs.get(dvmObj.getType()).add(dvmObj);
		dvmObj.setTag(dvmObjs.get(dvmObj.getType()).size());
	}


}
