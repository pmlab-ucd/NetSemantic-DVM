package fu.hao.trust.data;

import java.util.Set;

public class PluginConfig {
	private String tag;
	private Set<String> sources;
	private Set<String> sinks;
	
	private CorrelatedDataFact corrFact;
	
	public PluginConfig(String tag, Set<String> sources, Set<String> sinks ) {
		this.setTag(tag);
		this.setSources(sources);
		this.setSinks(sinks);
		corrFact = new CorrelatedDataFact(tag);
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	public Set<String> getSources() {
		return sources;
	}

	public void setSources(Set<String> sources) {
		this.sources = sources;
	}

	public Set<String> getSinks() {
		return sinks;
	}

	public void setSinks(Set<String> sinks) {
		this.sinks = sinks;
	}

	public CorrelatedDataFact getCorrFact() {
		return corrFact;
	}

	public void setCorrFact(CorrelatedDataFact corrFact) {
		this.corrFact = corrFact;
	}

}
