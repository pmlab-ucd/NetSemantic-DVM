package android.view;

import android.content.res.Resources;
import android.view.ViewGroup.LayoutParams;
import patdroid.core.ClassInfo;
import patdroid.core.FieldInfo;
import patdroid.core.MethodInfo;
import patdroid.dalvik.Instruction;
import patdroid.util.Pair;
import fu.hao.trust.dvm.DVMObject;
import fu.hao.trust.dvm.DalvikVM;
import fu.hao.trust.dvm.DalvikVM.StackFrame;

public class View extends DVMObject {
	int color;
	String hint = "";
	
	DVMObject listener;
	
	class OnClickListener {
		
	}

	public View(DalvikVM vm, ClassInfo type, int mid) {
		super(vm, type);
		this.mID = mid;
	}

	public int mID;
	
	private int visibility;
	
	public void setVisibility(int visibility) {
		this.visibility = visibility;
	}
	
	public int getVisibility() {
		return visibility;
	}
	
	public void setBackgroundColor(int color) {
		this.color = color;
	}
	
	public void setOnClickListener(OnClickListener l) {
		
	}
	
	public void setOnClickListener(DVMObject listener) {
		
		if (this.listener != null) {
			vm.getCallbackPool().remove(listener.getClazz().fullName);
		}
		this.listener = listener;
		if (listener != null) {
			vm.getCallbackPool().put(listener.getClazz().fullName, listener);
		}
	}
	
	public void setType() {
		
	}
	
    public final void setHint(CharSequence hint) {
       this.hint = hint.toString(); 
       Instruction[] insns = new Instruction[3];
		Instruction i = new Instruction();
		i.opcode = Instruction.OP_SPECIAL;
		i.opcode_aux = Instruction.OP_SP_ARGUMENTS;
		int[] args = new int[2];
		args[0] = (short) 1;
		args[1] = (short) 2;
		i.setExtra(args);
		ClassInfo[] paramTypes = new ClassInfo[1];
		ClassInfo strType = ClassInfo.findClass("java.lang.String");
		paramTypes[0] = strType;
		insns[0] = i;
		i = new Instruction();
		i.opcode = Instruction.OP_INSTANCE_OP;
		i.opcode_aux = Instruction.OP_INSTANCE_PUT_FIELD;
		i.type = ClassInfo.rootObject;
		i.setExtra(new FieldInfo(type, "hint"));
		i.r0 = (short) 1; // object register
		i.r1 = (short) 2; // value register, may be source or dest
		insns[1] = i;
		i = new Instruction();
		i.opcode = Instruction.OP_RETURN;
		i.opcode_aux = Instruction.OP_RETURN_VOID;
		insns[2] = i;
		MethodInfo mi = new MethodInfo(getType(), "setHint", ClassInfo.primitiveVoid, paramTypes, 1);
		mi.insns = insns;
		
		@SuppressWarnings("unchecked")
		Pair<Object, ClassInfo>[] params = (Pair<Object, ClassInfo>[]) new Pair[2]; 
		params[0] = new Pair<Object, ClassInfo>(this, type);
		params[1] = new Pair<Object, ClassInfo>(hint.toString(), strType);
		StackFrame frame = vm.newStackFrame(type, mi, params, false);
		vm.runInstrumentedMethods(frame);
    }
    
    public CharSequence getHint() {
       return hint;
    }
    
    private LayoutParams layoutParams;
    public LayoutParams getLayoutParams() {
    	if (layoutParams == null) {
    		layoutParams = new LayoutParams();
    	}
    	return layoutParams;
    }
   
    private Resources resources;
    public Resources getResources() {
    	if (resources == null) {
    		resources = new Resources();
    	}
    	
    	return resources;
    }
}
