package fu.hao.trust.data;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import android.view.View;
import fu.hao.trust.dvm.DalvikVM;
import fu.hao.trust.utils.Log;
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

	List<Instruction> calls;
	MethodInfo mi;
	List<Object[]> paramList;
	// The API calls influencing this target call.
	Collection<Instruction> depAPIs;
	// The API calls influenced by this target call.
	Set<Instruction> influAPIs;
	private Set<View> fedViews;

	public TargetCall(Instruction call, DalvikVM vm, Set<Instruction> depAPIs) {
		this.calls = new LinkedList<>();
		calls.add(call);
		Object[] extra = (Object[]) call.extra;
		int[] args = (int[]) extra[1];
		paramList = new LinkedList<>();
		Object[] params = new Object[args.length];
		for (int i = 0; i < args.length; i++) {
			params[i] = vm.getReg(args[i]).getData();
			// TODO If param[i] is sensCTXVar
		}

		paramList.add(params);

		mi = (MethodInfo) extra[0];
		this.depAPIs = new HashSet<>(depAPIs);
		influAPIs = new HashSet<>();
	}

	public TargetCall(Instruction call, DalvikVM vm) {
		this.calls = new LinkedList<>();
		calls.add(call);
		Object[] extra = (Object[]) call.extra;
		int[] args = (int[]) extra[1];
		paramList = new LinkedList<>();
		Object[] params = new Object[args.length];
		for (int i = 0; i < args.length; i++) {
			params[i] = vm.getReg(args[i]).getData();
			// TODO If param[i] is sensCTXVar
		}

		paramList.add(params);

		mi = (MethodInfo) extra[0];
		influAPIs = new HashSet<>();
		depAPIs = new HashSet<>();
		fedViews = new HashSet<>();
	}

	public void setDepAPIs(Collection<Instruction> depAPIs) {
		this.depAPIs = new HashSet<>(depAPIs);
	}

	public void addInfluAPI(Instruction apiCall) {
		influAPIs.add(apiCall);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(mi + "-- its Param: [");
		for (Object[] params : paramList) {
			for (Object param : params) {
				sb.append(param + ",");
			}
		}
		sb.replace(sb.length() - 1, sb.length(), "]");
		return sb.toString() + "\n -- it deps on APIs: " + depAPIs
				+ "\n influAPIs " + influAPIs
				+ "\n fedViews " + fedViews;
	}

	public Collection<Instruction> getDepAPIs() {
		return depAPIs;
	}

	public Set<Instruction> getInfluAPIs() {
		return influAPIs;
	}

	public void addParams(Instruction call, DalvikVM vm) {
		calls.add(call);
		Object[] extra = (Object[]) call.extra;
		int[] args = (int[]) extra[1];
		Object[] params = new Object[args.length];
		StringBuilder sb = new StringBuilder(mi + "-- its Param: [");
		for (int i = 0; i < args.length; i++) {		
			params[i] = vm.getReg(args[i]).getData();
			sb.append(params[i] + ",");
			// TODO If param[i] is sensCTXVar
		}
		paramList.add(params);
		sb.replace(sb.length() - 1, sb.length(), "]");
		Log.warn(TAG, "Add params " + sb);
	}

	public void addDepAPI(Instruction APICall) {
		if (depAPIs == null) {
			depAPIs = new HashSet<>();
		}
		
		depAPIs.add(APICall);
	}

	public void addDepAPIs(LinkedList<Instruction> elemSrcs) {
		depAPIs.addAll(elemSrcs);
	}

	public Set<View> getFedViews() {
		return fedViews;
	}

	public void setFedViews(Set<View> fedViews) {
		this.fedViews = fedViews;
	}
	
	public void addFedView(View view) {
		fedViews.add(view);
		Log.msg(TAG, "Add fedView " + view);
	}

}
