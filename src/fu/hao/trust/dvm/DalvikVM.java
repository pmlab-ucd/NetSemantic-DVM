package fu.hao.trust.dvm;

import org.jf.dexlib2.iface.DexFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import patdroid.core.ClassInfo;
import patdroid.core.MethodInfo;
import patdroid.dalvik.Instruction;

public class DalvikVM {
	private final Logger logger = LoggerFactory.getLogger(DalvikVM.class);

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
		Object data = null;

		public void copy(simple_dvm_register y) {
			this.type = y.type;
			this.data = y.data;
		}

		public void copy(ClassInfo type, Object data) {
			this.type = type;
			this.data = data;
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
		long pc;
		// which reg store the return value of callee called by this method
		int return_val_reg;

		JVM_STACK_FRAME(MethodInfo method) {
			this.method = method;
			pc = 0;
		}
	}

	class constant_info_st {
		int index;
		int tag;
		int[] base;
	}

	/**
	 * @ClassName: Context/Env
	 * @Description: TODO
	 * @author: hao
	 * @date: Feb 15, 2016 8:47:43 PM
	 */
	class JVM_INTERP_ENV {
		constant_info_st constant_info;
		JVM_INTERP_ENV prev_env;
	};

	// We directly use underlying jvm who runs this interpreter to manage memory
	// int[] heap = new int[8192];
	// int[] object_ref = new int[4];
	simple_dvm_register[] regs = new simple_dvm_register[65536]; // 32
	invoke_parameters p;
	int[] result = new int[8];
	// since we do not really use pc to guide the inter-procedure
	// directly bind pc with stack frame for convenience
	// so this pc is actually useless now.
	long pc;

	JVM_INTERP_ENV curr_jvm_interp_env;
	JVM_STACK_FRAME curr_jvm_stack;
	int jvm_stack_depth = 0;

	public void start(DexFile dex) {
		// TODO support running dex
	}

	public void runMethod(MethodInfo method) {
		logger.info("RUN METHOD");

		JVM_STACK_FRAME jvm_stack_frame = new JVM_STACK_FRAME(method);
		curr_jvm_stack = jvm_stack_frame;
		for (Instruction insns : method.insns) {
			//byte opcode = insns.opcode;
		}
	}
}
