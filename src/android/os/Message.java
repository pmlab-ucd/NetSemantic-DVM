package android.os;

import java.lang.reflect.Modifier;

import patdroid.core.ClassInfo;
import patdroid.core.FieldInfo;
import patdroid.core.MethodInfo;
import patdroid.dalvik.Instruction;
import patdroid.util.Pair;
import fu.hao.trust.dvm.DVMObject;
import fu.hao.trust.dvm.DalvikVM.StackFrame;
import fu.hao.trust.utils.Log;
import fu.hao.trust.utils.Settings;

public class Message extends DVMObject {
	public int what;
	public int arg1;
	public int arg2;
	public Object obj;
	public Messenger replyTo;
	
	private final static String TAG = Message.class.getSimpleName();

	public Message() {
		super(Settings.getVM(), ClassInfo.findClass("android.os.Message"));
	}
	
	public Message(Handler h) {
		super(Settings.getVM(), ClassInfo.findClass("android.os.Message"));
	}

	public static Message obtain() {
		return new Message();
	}

	public static Message obtain(Message orig) {
		throw new RuntimeException("Stub!");
	}

	public static Message obtain(Handler h) {
		throw new RuntimeException("Stub!");
	}

	public static Message obtain(Handler h, int what) {
		throw new RuntimeException("Stub!");
	}

	public static Message obtain(Handler h, int what, Object obj) {
		Message msg = new Message(h);
		msg.setField("what", what);
		msg.what = what;
		msg.setField("obj", obj);
		msg.obj = obj;
		return msg;
	}

	public static Message obtain(Handler h, int what, int arg1, int arg2) {
		Message msg = new Message(h);
		
		ClassInfo[] paramTypes = new ClassInfo[4];
		paramTypes[0] = h == null ? ClassInfo.primitiveVoid : h.getType();
		paramTypes[1] = ClassInfo.primitiveInt;
		paramTypes[2] = ClassInfo.primitiveInt;
		paramTypes[3] = ClassInfo.primitiveInt;
		MethodInfo mi = new MethodInfo(msg.getType(), "myObtain", ClassInfo.primitiveVoid,
				paramTypes, Modifier.PRIVATE);
		Instruction[] insns = new Instruction[6];
		
		Instruction i = new Instruction();
		i.opcode = Instruction.OP_SPECIAL;
		i.opcode_aux = Instruction.OP_SP_ARGUMENTS;
		Settings.getVM().getReg(65535).setValue(msg, msg.getType());
		int[] args = new int[5];
		args[0] = (short) 1;
		args[1] = (short) 2;
		args[2] = (short) 3;
		args[3] = (short) 4;
		args[4] = (short) 5;
		i.setExtra(args);
		insns[0] = i;
		
		i = new Instruction();
		i.opcode = Instruction.OP_INSTANCE_OP;
		i.opcode_aux = Instruction.OP_INSTANCE_PUT_FIELD;
		i.type = ClassInfo.rootObject;
		i.setExtra(new FieldInfo(msg.getType(), "handler"));
		i.r0 = (short) 1; // object register
		i.r1 = (short) 2; // value register, may be source or dest
		insns[1] = i;
	
		i = new Instruction();
		i.opcode = Instruction.OP_INSTANCE_OP;
		i.opcode_aux = Instruction.OP_INSTANCE_PUT_FIELD;
		i.type = ClassInfo.rootObject;
		i.setExtra(new FieldInfo(msg.getType(), "what"));
		i.r0 = (short) 1; // object register
		i.r1 = (short) 3; // value register, may be source or dest
		insns[2] = i;
		
		i = new Instruction();
		i.opcode = Instruction.OP_INSTANCE_OP;
		i.opcode_aux = Instruction.OP_INSTANCE_PUT_FIELD;
		i.type = ClassInfo.rootObject;
		i.setExtra(new FieldInfo(msg.getType(), "arg1"));
		i.r0 = (short) 1; // object register
		i.r1 = (short) 4; // value register, may be source or dest
		insns[3] = i;
		
		i = new Instruction();
		i.opcode = Instruction.OP_INSTANCE_OP;
		i.opcode_aux = Instruction.OP_INSTANCE_PUT_FIELD;
		i.type = ClassInfo.rootObject;
		i.setExtra(new FieldInfo(msg.getType(), "arg2"));
		i.r0 = (short) 1; // object register
		i.r1 = (short) 5; // value register, may be source or dest
		insns[4] = i;
		
		i = new Instruction();
		i.opcode = Instruction.OP_RETURN;
		i.opcode_aux = Instruction.OP_RETURN_VOID;
		insns[5] = i;
		
		mi.insns = insns;
		
		StackFrame frame = Settings.getVM().newStackFrame(msg.getClazz(), mi, null, false);
		i = Settings.getVM().getCurrtInst();
		Object[] extra = (Object[]) i.getExtra();
		
		// The register index referred by args
		int[] nargs = (int[]) extra[1];
		args = new int[5];
		args[0] = 65535;
		Log.warn(TAG, "currtInst: " + Settings.getVM().getCurrtInst());
		for (int j = 0; j < nargs.length; j++) {
			Log.bb(TAG, "reg " + nargs[j]);
			args[j + 1] = nargs[j];
		}
		Settings.getVM().setGlobalCallContext(args);
		frame.setCallCtx(Settings.getVM().getGlobalCallCtx());;
		Settings.getVM().runInstrumentedMethods(frame);
		
		
		/*
		msg.setField("what", what);
		msg.what = what;
		msg.setField("arg1", arg1);
		msg.arg1 = arg1;
		msg.setField("arg2", arg2);
		msg.arg2 = arg2;
		*/
		return msg;
	}

	public static Message obtain(Handler h, int what, int arg1, int arg2,
			Object obj) {
		Message msg = new Message(h);
		ClassInfo[] paramTypes = new ClassInfo[5];
		paramTypes[0] = h.getType();
		paramTypes[1] = ClassInfo.primitiveInt;
		paramTypes[2] = ClassInfo.primitiveInt;
		paramTypes[3] = ClassInfo.primitiveInt;
		paramTypes[4] = ClassInfo.findClass("java.lang.Object");
		MethodInfo mi = new MethodInfo(msg.getType(), "myObtain", ClassInfo.primitiveVoid,
				paramTypes, Modifier.PRIVATE);
		Instruction[] insns = new Instruction[6];
		
		Instruction i = new Instruction();
		i.opcode = Instruction.OP_SPECIAL;
		i.opcode_aux = Instruction.OP_SP_ARGUMENTS;
		int[] args = new int[7];
		args[0] = (short) 1;
		args[1] = (short) 2;
		args[2] = (short) 3;
		args[3] = (short) 4;
		args[4] = (short) 5;
		args[5] = (short) 6;
		i.setExtra(args);
		insns[0] = i;
		
		i = new Instruction();
		i.opcode = Instruction.OP_INSTANCE_OP;
		i.opcode_aux = Instruction.OP_INSTANCE_PUT_FIELD;
		i.type = ClassInfo.rootObject;
		i.setExtra(new FieldInfo(msg.getType(), "handler"));
		i.r0 = (short) 1; // object register
		i.r1 = (short) 2; // value register, may be source or dest
		insns[1] = i;
	
		i = new Instruction();
		i.opcode = Instruction.OP_INSTANCE_OP;
		i.opcode_aux = Instruction.OP_INSTANCE_PUT_FIELD;
		i.type = ClassInfo.rootObject;
		i.setExtra(new FieldInfo(msg.getType(), "what"));
		i.r0 = (short) 1; // object register
		i.r1 = (short) 3; // value register, may be source or dest
		insns[2] = i;
		
		i = new Instruction();
		i.opcode = Instruction.OP_INSTANCE_OP;
		i.opcode_aux = Instruction.OP_INSTANCE_PUT_FIELD;
		i.type = ClassInfo.rootObject;
		i.setExtra(new FieldInfo(msg.getType(), "arg1"));
		i.r0 = (short) 1; // object register
		i.r1 = (short) 4; // value register, may be source or dest
		insns[3] = i;
		
		i = new Instruction();
		i.opcode = Instruction.OP_INSTANCE_OP;
		i.opcode_aux = Instruction.OP_INSTANCE_PUT_FIELD;
		i.type = ClassInfo.rootObject;
		i.setExtra(new FieldInfo(msg.getType(), "arg2"));
		i.r0 = (short) 1; // object register
		i.r1 = (short) 5; // value register, may be source or dest
		insns[4] = i;
		
		i = new Instruction();
		i.opcode = Instruction.OP_INSTANCE_OP;
		i.opcode_aux = Instruction.OP_INSTANCE_PUT_FIELD;
		i.type = ClassInfo.rootObject;
		i.setExtra(new FieldInfo(msg.getType(), "obj"));
		i.r0 = (short) 1; // object register
		i.r1 = (short) 6; // value register, may be source or dest
		insns[5] = i;
		
		i = new Instruction();
		i.opcode = Instruction.OP_RETURN;
		i.opcode_aux = Instruction.OP_RETURN_VOID;
		insns[6] = i;
		
		mi.insns = insns;
		
		@SuppressWarnings("unchecked")
		Pair<Object, ClassInfo>[] params = (Pair<Object, ClassInfo>[]) new Pair[6]; 
		params[0] = new Pair<Object, ClassInfo>(msg, msg.getType());
		params[1] = new Pair<Object, ClassInfo>(h, h.getType());
		params[2] = new Pair<Object, ClassInfo>(what, ClassInfo.primitiveInt);
		params[3] = new Pair<Object, ClassInfo>(arg1, ClassInfo.primitiveInt);
		params[4] = new Pair<Object, ClassInfo>(arg2, ClassInfo.primitiveInt);
		params[5] = new Pair<Object, ClassInfo>(obj, ClassInfo.findClass("java.lang.Object"));
		StackFrame frame = Settings.getVM().newStackFrame(msg.getClazz(), mi, params, false);
		Settings.getVM().runInstrumentedMethods(frame);
		
		/*
		msg.setField("what", what);
		msg.what = what;
		msg.setField("arg1", arg1);
		msg.arg1 = arg1;
		msg.setField("arg2", arg2);
		msg.arg2 = arg2;
		msg.setField("obj", obj);
		msg.obj = obj;
		*/
		return msg;
	}
	
	public void recycle() {
        throw new RuntimeException("Stub!");
    }

    public void copyFrom(Message o) {
        throw new RuntimeException("Stub!");
    }

    public long getWhen() {
        throw new RuntimeException("Stub!");
    }

    public void setTarget(Handler target) {
        throw new RuntimeException("Stub!");
    }

    public Handler getTarget() {
        throw new RuntimeException("Stub!");
    }

    public Runnable getCallback() {
        throw new RuntimeException("Stub!");
    }

    public Bundle getData() {
        throw new RuntimeException("Stub!");
    }

    public Bundle peekData() {
        throw new RuntimeException("Stub!");
    }

    public void setData(Bundle data) {
        throw new RuntimeException("Stub!");
    }

    public void sendToTarget() {
        throw new RuntimeException("Stub!");
    }



}
