package fu.hao.trust.solver;

import patdroid.dalvik.Instruction;
import fu.hao.trust.dvm.DalvikVM;

/**
 * @ClassName: BiDirVar
 * @Description: Represent a variable who will lead to bi-direction proceed of the branch. 
 * @author: Hao Fu
 * @date: Mar 15, 2016 8:48:22 PM
 */
public interface BiDirVar {
	
	public void addConstriant(DalvikVM vm, Instruction inst);
	
	public Object getValue();
	
	public void setValue(Object value);
	
	/**
	* @Title: isOn
	* @Author: Hao Fu
	* @Description: Whether turn bi-dir traversal on
	* @param @return  
	* @return boolean   
	* @throws
	*/
	public boolean isOn();
	
}
