package fu.hao.trust.data;

import java.util.Stack;

import fu.hao.trust.solver.BiDirBranch;

public class CorrelatedDataFact {
	String tag;
	// The interestedSimple branches that have CDTAINTVar inside the
	// conditions.
	// It is a subset of simpleBranches.
	private Stack<Branch> interestedSimple;
	// Add new interested when first meet or bracktrace, rm when encounter
	// the
	// beginning of <rest>
	private Stack<BiDirBranch> interestedBiDir;

	public CorrelatedDataFact(String tag) {
		this.tag = tag;
		interestedSimple = new Stack<>();
		interestedBiDir = new Stack<>();
	}
	
	public Stack<Branch> getInterestedSimple() {
		return interestedSimple;
	}
	
	public Stack<BiDirBranch> getInterestedBiDir() {
		return interestedBiDir;
	}
}
