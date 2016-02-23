package fu.hao.trust.dvm;

import java.io.File;
import java.io.IOException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import fu.hao.trust.analysis.Plugin;
import fu.hao.trust.utils.Log;
import patdroid.core.ClassInfo;
import patdroid.core.MethodInfo;
import patdroid.core.ReflectionClassDetailLoader;
import patdroid.dalvik.Instruction;
import patdroid.smali.SmaliClassDetailLoader;

public class DalvikVM {

	private final String tag = getClass().toString();

	// ---------------------------------
	/*
	 * generic parameter parser for 35c
	 */
	class invoke_parameters {
		int method_id;
		int reg_count;
		int[] reg_idx = new int[5]; // 0-5 map C-G
	}

	// Dalvik VM Register Bank
	public class simple_dvm_register {
		ClassInfo type = null;
		// data can be instance of: PrimitiveInfo, DVMObject and any class
		// reflection supports
		Object data = null;

		public void copy(simple_dvm_register y) {
			this.type = y.type;
			this.data = y.data;
		}

		public void copy(ClassInfo type, Object data) {
			this.type = type;
			this.data = data;
		}

		public Object getData() {
			return data;
		}
	}

	public class JVM_STACK_FRAME {
		int[] local_var_table;
		// int[] operand_stack; // dvm leverages reg, not a stack
		MethodInfo method;
		int return_addr;
		long offset;
		long max_stack;
		long max_locals;
		JVM_STACK_FRAME prev_stack;
		// in theory, pc should not be here
		// but it's easier to implement in our case
		// since we know we do not need pc to cross procedure
		int pc;

		JVM_STACK_FRAME(MethodInfo method) {
			this.method = method;
			pc = 0;
			return_val_reg = new simple_dvm_register();
		}
	}

	class constant_info_st {
		int index;
		int tag;
		int[] base;
	}

	class Heap {
		Map<ClassInfo, DVMClass> dvmClasses = new HashMap<>();
		Map<ClassInfo, Set<DVMObject>> dvmObjs = new HashMap<>();

		public void setClass(ClassInfo type, DVMClass dvmClass) {
			dvmClasses.put(type, dvmClass);
		}

		public DVMClass getClass(ClassInfo type) {
			Log.debug(tag, "getClass " + type);
			if (!dvmClasses.containsKey(type)) {
				return null;
			}
			return dvmClasses.get(type);
		}

		public void setObj(ClassInfo type, DVMObject dvmObj) {
			if (dvmObjs.get(type) == null) {
				dvmObjs.put(type, new HashSet<DVMObject>());
			}
			dvmObjs.get(type).add(dvmObj);
		}
	}

	// We directly use underlying jvm who runs this interpreter to manage memory
	Heap heap;
	simple_dvm_register[] regs = new simple_dvm_register[65536]; // 32
	// invoke_parameters p;
	// int[] result = new int[8];
	int pc;

	JVM_STACK_FRAME curr_jvm_stack;
	int jvm_stack_depth = 0;

	Interpreter interpreter;

	simple_dvm_register test = new simple_dvm_register();
	// which reg store the return value of callee called by this method
	simple_dvm_register return_val_reg;

	int[] calling_ctx;
	DVMObject thisObj;
	// DVMClass thisClass;

	Plugin plugin;

	public simple_dvm_register getReg(int i) {
		return regs[i];
	}

	/**
	 * @Title: getClass
	 * @Description: Get the class in the heap.
	 * @return
	 * @see java.lang.Object#getClass()
	 */
	public DVMClass getClass(ClassInfo type) {
		return heap.getClass(type);
	}

	/**
	 * @Title: getObj
	 * @Author: hao
	 * @Description: TODO
	 * @param @return
	 * @return DVMObject
	 * @throws
	 */
	public DVMObject getObj() {
		// TODO
		return null;
	}

	public int[] getContext() {
		return calling_ctx;
	}

	public DalvikVM() {
		heap = new Heap();
		interpreter = new Interpreter();
		for (int i = 0; i < regs.length; i++) {
			regs[i] = new simple_dvm_register();
		}
		return_val_reg = new simple_dvm_register();
	}

	public simple_dvm_register getReturnReg() {
		return return_val_reg;
	}

	public JVM_STACK_FRAME newStackFrame(MethodInfo mi) {
		JVM_STACK_FRAME newStackFrame = new JVM_STACK_FRAME(mi);
		// ctx switch
		newStackFrame.prev_stack = curr_jvm_stack;
		curr_jvm_stack = newStackFrame;
		jvm_stack_depth++;
		pc = 0;
		return new JVM_STACK_FRAME(mi);
	}
	
	ClassLoader loader;

	public void runMethod(String apk, String className, String main,
			Plugin plugin) throws ZipException, IOException {
		Log.msg(tag, "Begin run " + main + " at " + apk);
		// for normal java run-time classes
		// when a class is not loaded, load it with reflection
		ClassInfo.rootDetailLoader = new ReflectionClassDetailLoader();
		// pick an apk
		ZipFile apkFile;
		apkFile = new ZipFile(new File(apk));
		// load all classes, methods, fields and instructions from an apk
		// we are using smali as the underlying engine
		new SmaliClassDetailLoader(apkFile, true).loadAll();
		// get the class representation for the MainActivity class in the
		// apk
		ClassInfo c = ClassInfo.findClass(className);
		// find all methods with the name "onCreate", most likely there is
		// only one
		MethodInfo[] methods = c.findMethodsHere(main);

		// print all instructions
		int counter = 0;
		for (Instruction ins : methods[0].insns) {
			counter++;
			Log.debug(tag, "opcode: " + ins.opcode + " " + ins.opcode_aux);
			Log.debug(tag, "[" + counter + "]" + ins.toString());
		}

		this.plugin = plugin;
		interpreter.invocation(this, methods[0]);
	}
}
