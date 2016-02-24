package fu.hao.trust.data;

import java.util.ArrayList;
import java.util.List;


public class MethodRep {
	private final String methodName;
	private final String className;
	private final String returnType;
	private final List<String> parameters;
	private int hashCode = 0;
	
	public MethodRep
			(String methodName,
			String className,
			String returnType,
			List<String> parameters){
		this.methodName = methodName;
		this.className = className;
		this.returnType = returnType;
		this.parameters = parameters;
	}
	
	public MethodRep(MethodRep methodAndClass) {
		this.methodName = methodAndClass.methodName;
		this.className = methodAndClass.className;
		this.returnType = methodAndClass.returnType;
		this.parameters = new ArrayList<String>(methodAndClass.parameters);
	}

	public String getMethodName() {
		return this.methodName;
	}
	
	public String getClassName() {
		return this.className;
	}
	
	public String getReturnType() {
		return this.returnType;
	}
	
	public List<String> getParameters() {
		return this.parameters;
	}
	
	public String getSubSignature() {
		String s = (this.returnType.length() == 0 ? "" : this.returnType + " ") + this.methodName + "(";
		for (int i = 0; i < this.parameters.size(); i++) {
			if (i > 0)
				s += ",";
			s += this.parameters.get(i).trim();
		}
		s += ")";
		return s;
	}

	public String getSignature() {
		String s = "<" + this.className + ": " + (this.returnType.length() == 0 ? "" : this.returnType + " ")
				+ this.methodName + "(";
		for (int i = 0; i < this.parameters.size(); i++) {
			if (i > 0)
				s += ",";
			s += this.parameters.get(i).trim();
		}
		s += ")>";
		return s;
	}

	@Override
	public boolean equals(Object another) {
		if (super.equals(another))
			return true;
		if (!(another instanceof MethodRep))
			return false;
		MethodRep otherMethod = (MethodRep) another;
		
		if (!this.methodName.equals(otherMethod.methodName))
			return false;
		if (!this.parameters.equals(otherMethod.parameters))
			return false;
		if (!this.className.equals(otherMethod.className))
			return false;
		return true;
	}
	
	@Override
	public int hashCode() {
		if (this.hashCode == 0)
			this.hashCode = this.methodName.hashCode() + this.className.hashCode() * 5;
		// The parameter list is available from the outside, so we can't cache it
		return this.hashCode + this.parameters.hashCode() * 7;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("<");
		sb.append(className);
		sb.append(": ");
		sb.append(returnType);
		sb.append(" ");
		sb.append("methodName(");
		boolean isFirst = true;
		for (String param : parameters) {
			if (!isFirst)
				sb.append(",");
			sb.append(param);
		}
		sb.append(")>");
		return sb.toString();
	}

}
