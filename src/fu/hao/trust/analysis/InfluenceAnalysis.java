package fu.hao.trust.analysis;

import java.io.IOException;
import java.util.Map;
import java.util.Stack;

import patdroid.core.MethodInfo;
import patdroid.dalvik.Instruction;
import fu.hao.trust.data.Branch;
import fu.hao.trust.data.CorrelatedDataFact;
import fu.hao.trust.data.PluginConfig;
import fu.hao.trust.data.TargetCall;
import fu.hao.trust.dvm.DalvikVM;
import fu.hao.trust.solver.BiDirBranch;
import fu.hao.trust.solver.InfluVar;
import fu.hao.trust.utils.Log;
import fu.hao.trust.utils.SrcSinkParser;

/**
 * @ClassName: Condition
 * @Description: Extract the API calls influenced by the target API calls, which
 *               generate influencing variables.
 * @author: Hao Fu
 * @date: Mar 9, 2016 3:10:38 PM
 */
public class InfluenceAnalysis {

	final String TAG = getClass().getSimpleName();

	private PluginConfig config;

	public void influInvoke(DalvikVM vm, Instruction inst,
			CorrelatedDataFact fact, Map<Instruction, TargetCall> targetCalls) {
		Object[] extra = (Object[]) inst.extra;
		MethodInfo mi = (MethodInfo) extra[0];
		Stack<Branch> interestedSimple = fact.getInterestedSimple();
		Stack<BiDirBranch> interestedBiDir = fact.getInterestedBiDir();

		// Generate influVar
		if (vm.getReflectMethod() != null
				&& config.getSources().contains(Taint.getSootSignature(mi))) {
			try {
				// FIXME
				vm.getReturnReg().setData(
						new InfluVar(mi.returnType,
								vm.getReturnReg().getData(), inst));
				Log.warn(TAG, "Add new influencing obj: "
						+ vm.getReturnReg().getData());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		if (vm.getReflectMethod() != null) {
			// Detect influenced API calls
			Log.bb(TAG, "Method " + vm.getReflectMethod());
			if (!interestedSimple.isEmpty()) {
				Log.bb(TAG, "Not empty simple branch!");
				for (Branch branch : interestedSimple) {
					Log.bb(TAG, "Branch: " + branch);
					for (Instruction elemSrc : branch.getElemSrcs()) {
						Log.bb(TAG, "Elem src " + elemSrc);
						if (targetCalls != null
								&& targetCalls.containsKey(elemSrc)) {
							Log.msg(TAG, "Target call matched! " + elemSrc);
							targetCalls.get(elemSrc).addInfluAPI(inst);
							break;
						}
					}
				}
				
				Log.warn(TAG, "Found influenced API call " + mi);
			} else if (!interestedBiDir.isEmpty()) {
				Log.bb(TAG, "Not empty bidir branch!");
				for (Branch branch : interestedBiDir) {
					for (Instruction elemSrc : branch.getElemSrcs()) {
						if (targetCalls != null
								&& targetCalls.containsKey(elemSrc)) {
							Log.msg(TAG, "Target call matched! " + elemSrc);
							targetCalls.get(elemSrc).addInfluAPI(inst);
							break;
						}
					}
				}
				Log.warn(TAG, "Found influenced API call " + mi);
			} else {
				Log.msg(TAG, "Not API Recording");
			}
		}

	}

	public InfluenceAnalysis() {
		SrcSinkParser parser = null;
		try {
			parser = SrcSinkParser.fromFile("Connections.txt");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		setConfig(new PluginConfig(TAG, parser.getSrcStrs(), null));
	}

	public PluginConfig getConfig() {
		return config;
	}

	public void setConfig(PluginConfig config) {
		this.config = config;
	}

	public void influInvoke(DalvikVM vm, Instruction inst,
			Map<Instruction, TargetCall> targetCalls) {
		influInvoke(vm, inst, config.getCorrFact(), targetCalls);
	}
}
