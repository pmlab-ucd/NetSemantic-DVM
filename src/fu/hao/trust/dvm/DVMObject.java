package fu.hao.trust.dvm;

import java.util.HashMap;
import java.util.Map;

import fu.hao.trust.dvm.DalvikVM.StackFrame;
import fu.hao.trust.utils.Log;
import fu.hao.trust.utils.Pair;
import patdroid.core.ClassInfo;
import patdroid.core.FieldInfo;
import patdroid.core.MethodInfo;
import patdroid.dalvik.Instruction;

/**
 * @ClassName: DVMObject
 * @Description: To represent a instance of an user-defined class (not def as a
 *               primitive, java.lang and android sdk)
 * @author: Hao Fu
 * @date: Feb 23, 2016 11:28:14 AM
 */
public class DVMObject {
	protected ClassInfo type = null;
	protected Map<FieldInfo, Object> fields = new HashMap<FieldInfo, Object>();
	protected DVMClass dvmClass = null;
	protected final String TAG = getClass().getSimpleName();
	protected Object superObj = null;
	protected DalvikVM vm;
	protected int index;
	// FIXME could be multiple urls due to summarization. 
	// /Activity|Service|Application/(field of Activity...)/.../owner class name/field name
	protected String memUrl = ""; 

	public DVMObject(DalvikVM vm, ClassInfo type) {
		this.vm = vm;
		Log.bb(TAG, "New instance of " + type);
		DVMClass dvmClass = vm.getClass(type);
		this.type = type;
		this.setDvmClass(dvmClass);
		vm.setObj(type, this);

		/*
		 * ClassInfo superClass = type.getSuperClass();
		 * 
		 * if (superClass != null && superClass.fullName.contains("Activity")) {
		 * vm.callbackOwner = this; }
		 */
		/*
		 * if (superClass != null) { Log.debug(TAG, "Set super class " +
		 * superClass); Class<?> superClazz; try { superClazz =
		 * Class.forName(superClass.toString());
		 * setSuperObj(superClazz.newInstance()); } catch (Exception e) {
		 * setSuperObj(new DVMObject(vm, superClass)); } }
		 */

		// If the instance is implicitly initialized. 
		Instruction inst = vm.getCurrtInst();
		if (inst != null && inst.opcode != Instruction.OP_NEW_INSTANCE) {
			if (inst.opcode == Instruction.OP_INVOKE_OP) {
				Object[] extra = (Object[]) inst.getExtra();
				MethodInfo mi = (MethodInfo) extra[0];
				if (mi.name.contains("init")) {
					return;
				}
			}
			MethodInfo oinit = type.getDefaultConstructor();
			if (oinit != null) {
				Log.msg(TAG, "Not empty constructor");
				@SuppressWarnings("unchecked")
				Pair<Object, ClassInfo>[] params = (Pair<Object, ClassInfo>[]) new Pair[1];
				params[0] = new Pair<Object, ClassInfo>(this, type);
				StackFrame frame = vm.newStackFrame(type, oinit, params, false);
				vm.runInstrumentedMethods(frame);
			}
		}
	}

	public DVMObject(DalvikVM vm, ClassInfo type, VMHeap heap) {
		this.vm = vm;
		if (vm.getClass(type) == null) {
			Log.debug(TAG, "new object of " + type);
			vm.setClass(type, new DVMClass(vm, type));
		}
		Log.debug(TAG, "new object of " + type);
		DVMClass dvmClass = vm.getClass(type);
		this.setType(type);
		this.setDvmClass(dvmClass);
		heap.setObj(this);
	}

	public void callDefaultConstructor() {
		MethodInfo constructor = type.getDefaultConstructor();
		Log.bb(TAG, "Try to call default constructor of " + type);
		if (constructor != null) {
			@SuppressWarnings("unchecked")
			Pair<Object, ClassInfo>[] params = (Pair<Object, ClassInfo>[]) new Pair[1];
			params[0] = new Pair<Object, ClassInfo>(this, type);
			StackFrame frame = vm.newStackFrame(type, constructor, params,
					false);
			vm.runInstrumentedMethods(frame);
		}
	}

	public ClassInfo getType() {
		return type;
	}

	public void setType(ClassInfo type) {
		this.type = type;
	}

	public Map<FieldInfo, Object> getFields() {
		return fields;
	}

	public void setFields(Map<FieldInfo, Object> fields) {
		this.fields = fields;
	}

	public Object getFieldObj(FieldInfo fieldInfo) {
		if (fields.get(fieldInfo) == null) {
			for (FieldInfo field : fields.keySet()) {
				if (fieldInfo.fieldName.equals(field.fieldName)) {
					return fields.get(field);
				}
			}
			return null;
		} else {
			return fields.get(fieldInfo);
		}
	}
	
	public void setField(String fieldName, Object value) {
		for(FieldInfo field : fields.keySet()) {
			if (field.fieldName.equals(fieldName)) {
				fields.put(field, value);
				return;
			}
		}
		
		fields.put(new FieldInfo(type, fieldName), value);
	}

	public void setField(FieldInfo fieldInfo, Object obj) {
		if (obj == null) {
			Log.warn(TAG, "null field put!");
		}

		Log.debug(TAG, "put field " + obj);
		fields.put(fieldInfo, obj);
	}

	public DVMClass getDvmClass() {
		return dvmClass;
	}

	public void setDvmClass(DVMClass dvmClass) {
		this.dvmClass = dvmClass;
	}

	public Object getSuperObj() {
		return superObj;
	}

	public void setSuperObj(Object superObj) {
		this.superObj = superObj;
	}

	@Override
	public DVMObject clone() {
		DVMObject newObj = new DVMObject(vm, type);
		newObj.setFields(fields);
		newObj.setSuperObj(superObj);
		newObj.setDvmClass(dvmClass);
		return newObj;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public DVMObject clone(VMHeap heap) {
		DVMObject newObj = new DVMObject(vm, type, heap);
		newObj.setFields(fields);
		newObj.setSuperObj(superObj);
		newObj.setDvmClass(dvmClass);
		newObj.setIndex(index);
		return newObj;
	}

	public void setTag(int i) {
		index = i;
	}

	@Override
	public String toString() {
		return "instance number " + index + "@" + type;
	}

	public String getMemUrl() {
		return memUrl;
	}

	public void setMemUrl(String memUrl) {
		this.memUrl = memUrl;
	}
	
	public ClassInfo getClazz() {
		return type;
	}

}
