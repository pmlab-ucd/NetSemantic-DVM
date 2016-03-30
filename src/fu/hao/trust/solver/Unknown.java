package fu.hao.trust.solver;

import org.jacop.constraints.XmodYeqZ;
import org.jacop.constraints.XneqY;
import org.jacop.core.IntVar;
import org.jacop.core.Store;
import org.jacop.core.Var;

import fu.hao.trust.data.SymbolicVar;
import fu.hao.trust.dvm.DalvikVM;
import fu.hao.trust.utils.Log;
import patdroid.core.ClassInfo;
import patdroid.core.PrimitiveInfo;
import patdroid.dalvik.Instruction;

/**
 * @ClassName: Unknown
 * @Description: Representation of a unknown variable and its value is primitive.
 * @author: Hao Fu
 * @date: Feb 26, 2016 12:19:45 PM
 */
public class Unknown extends SymbolicVar {
	
	Var var;
	Store store;
	ClassInfo type;
	/**
	 * @fieldName: last
	 * @fieldType: Instruction
	 * @Description: The last arith instruction before a if stmt. 
	 */
	Instruction lastArith;
	
	boolean on = false;
	
	final String TAG = "Unknown";
	
	public Unknown(ClassInfo type) {
		this.type = type;
		store = new Store();
		//if (type.equals(ClassInfo.primitiveInt)) {
			var = new IntVar(store);
		//}
		Log.bb(TAG, "Unknown created!");
	}
	
	public void addConstriant(DalvikVM vm, Instruction ifInst) {
		// FIXME
		if (lastArith == null) {
			return;
		}

		PrimitiveInfo op1;
		if (lastArith.r1 != -1) {
			op1 = (PrimitiveInfo) vm.getReg(lastArith.r1).getData();
		} else {
			op1 = (PrimitiveInfo) lastArith.extra;
		}
		
		IntVar op1Var = new IntVar(store, op1.intValue(), op1.intValue());
		IntVar res = new IntVar(store);
		IntVar zero = new IntVar(store, 0, 0);
		// zero.setDomain(0, 0);
		
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
		return "Unknown var, type: " + type;
	}
	
	public void addLastArith(Instruction lastArith) {
		this.lastArith = lastArith;
	}
	
	public boolean isValid() { 
		if (getRes() == null) {
			return false;
		}
		return true;
	}
	
	/**
	* @Title: getRes
	* @Author: hao
	* @Description: Get the result through resolving the constrains
	* @param @return  
	* @return Var   
	* @throws
	*/
	public Var getRes() {
		// TODO
		return null;
	}

	@Override
	public Object getValue() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setValue(Object value) {
		// TODO Auto-generated method stub
		
	}

	public boolean isOn() {
		return on;
	}
	
}
