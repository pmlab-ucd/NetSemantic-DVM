package android.myclasses.java.lang;

public class Enum {
	
	String name;
	int order;
	
	public Enum(String name, int order) {
		this.name = name;
		this.order = order;
	}
	
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		
		return false;
	}
	
	public int ordinal() {
		return order;
	}

}
