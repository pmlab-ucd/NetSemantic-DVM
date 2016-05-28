package fu.hao.trust.utils;

public final class Pair<T1, T2> extends patdroid.util.Pair<T1, T2> {

	public Pair(T1 first, T2 second) {
		super(first, second);
	}

	public void setFirst(T1 first) {
		this.first = first;
	}
	
	public void setSecond(T2 second) {
		this.second = second;
	}
	
	public T1 getFirst() {
		return first;
	}
	
	public T2 getSecond() {
		return second;
	}
}