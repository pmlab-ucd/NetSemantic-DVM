package fu.hao.trust.dvm;

import org.jf.dexlib2.iface.DexFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import patdroid.core.ClassInfo;
import patdroid.core.MethodInfo;
import patdroid.dalvik.Instruction;

public class DalvikVM {
	private final Logger logger = LoggerFactory
			.getLogger(DalvikVM.class);
	//---------------------------------
	/*
	 * generic parameter parser for 35c
	 */
	class invoke_parameters {
	    int method_id ;
	    int reg_count ;
	    int[] reg_idx = new int[5] ; // 0-5 map C-G
	}

	// Dalvik VM Register Bank
	class simple_dvm_register {
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
	
	int[] heap = new int[8192];
	int[] object_ref = new int[4];
	simple_dvm_register[] regs = new simple_dvm_register[65536]; //32
	invoke_parameters p ;
    int[] result = new int[8];
    long pc;
    
    public void start(DexFile dex) {
    	// TODO support running dex
    }
    
    public void runMethod(MethodInfo method) {
    	logger.info("RUN METHOD");
    	pc = 0;
    	for(Instruction insns : method.insns) {
    		byte opcode = insns.opcode;
    	}
    }
}
