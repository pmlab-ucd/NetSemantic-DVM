package fu.hao.trust.solver;

import org.jacop.constraints.XmodYeqZ;
import org.jacop.constraints.XneqY;
import org.jacop.core.IntVar;
import org.jacop.core.Store;
import org.jacop.core.Var;

import fu.hao.trust.dvm.DalvikVM;
import patdroid.core.ClassInfo;
import patdroid.core.PrimitiveInfo;
import patdroid.dalvik.Instruction;

/**
 * @ClassName: Unknown
 * @Description: Representation of a unknown variable.
 * @author: Hao Fu
 * @date: Feb 26, 2016 12:19:45 PM
 */
public class Unknown {
	
	Var var;
	Store store;
	ClassInfo type;
	/**
	 * @fieldName: last
	 * @fieldType: Instruction
	 * @Description: The last arith instruction before a if stmt. 
	 */
	Instruction lastArith;
	
	public Unknown(ClassInfo type) {
		this.type = type;
		store = new Store();
		if (type.equals(ClassInfo.primitiveInt)) {
			var = new IntVar();
		}
	}
	
	public void addConstriant(DalvikVM vm, Instruction ifInst, boolean then) {
		PrimitiveInfo op1;
		if (lastArith.r1 != -1) {
			op1 = (PrimitiveInfo) vm.getReg(lastArith.r1).getData();
		} else {
			op1 = (PrimitiveInfo) lastArith.extra;
		}
		
		IntVar op1Var = new IntVar(store, op1.intValue(), op1.intValue());
		IntVar res = new IntVar(store);
		IntVar zero = new IntVar(store, 0, 0);
		zero.setDomain(0, 0);
		
		switch ((int) lastArith.opcode_aux) {
		case 0x2A:
			store.impose(new XmodYeqZ((IntVar) var, op1Var, res));
		}
		
		switch ((int) ifInst.opcode_aux) {
		// FIXME should not determined by opcode
		case 0x1F:
			store.impose(new XneqY(res, zero));
		}
	}
	
	public String toString() {
		return "Unknown var, type: " + type.toString();
	}
	
	public void addLastArith(Instruction lastArith) {
		this.lastArith = lastArith;
	}
}
