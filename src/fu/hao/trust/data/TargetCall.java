package fu.hao.trust.data;

import java.util.Collection;
import java.util.Set;

import fu.hao.trust.dvm.DalvikVM;
import patdroid.core.MethodInfo;
import patdroid.dalvik.Instruction;

/**
 * @ClassName: TargetCall
 * @Description: Store context and influence of the target API call
 * @author: Hao Fu
 * @date: Mar 18, 2016 3:20:34 PM
 */
public class TargetCall {
	final String TAG = getClass().toString();
	
	Instruction call;
	MethodInfo mi;
	Object[] params;
	// The API calls influencing this target call.
	Collection<Instruction> depAPIs;
	// The API calls influenced by this target call. 
	Set<Instruction> influAPIs;

	public TargetCall(Instruction call, DalvikVM vm, Set<Instruction> depAPIs) {
		this.call = call;
		Object[] extra = (Object[]) call.extra;
		int[] args = (int[]) extra[1];
		params = new Object[args.length];
		for (int i = 0; i < args.length; i++) {
			params[i] = vm.getReg(args[i]).getData();
			// TODO If param[i] is sensCTXVar
		}

		mi = (MethodInfo) extra[0];
		this.depAPIs = depAPIs;
	}

	public TargetCall(Instruction call, DalvikVM vm) {
		this.call = call;
		Object[] extra = (Object[]) call.extra;
		int[] args = (int[]) extra[1];
		params = new Object[args.length];
		for (int i = 0; i < args.length; i++) {
			params[i] = vm.getReg(args[i]).getData();
			// TODO If param[i] is sensCTXVar
		}

		mi = (MethodInfo) extra[0];
	}

	public void setDepAPIs(Collection<Instruction> depAPIs) {
		this.depAPIs = depAPIs;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(mi + "-- its Param: [");
		for (Object param : params) {
			sb.append(param.toString() + ",");
		}
		sb.replace(sb.length() - 1, sb.length(), "]");
		return sb.toString() + "-- it deps on APIs: " + depAPIs;
	}

}
