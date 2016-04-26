package fu.hao.trust.utils;

public final class Pair<T1, T2> {
	private T1 first;
	private T2 second;
	
	public Pair(T1 first, T2 second) {
		this.first = first;
		this.second = second;
	}
	
	@Override
	final public boolean equals(Object o) {
		if (o instanceof Pair) {
			Pair<?, ?> u = (Pair<?, ?>)o;
			return first.equals(u.first) && second.equals(u.second);
		}
		return false;
	}
	
	@Override
	final public int hashCode() {
		return 997 * first.hashCode() ^ 991 * second.hashCode();
	}
	
	@Override
	final public String toString() {
		return ("(" + first.toString() + ", " + second.toString() + ")");
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