package android.os;

import patdroid.core.ClassInfo;
import patdroid.core.MethodInfo;
import patdroid.dalvik.Instruction;
import fu.hao.trust.dvm.DVMObject;
import fu.hao.trust.dvm.DalvikVM;

public class AsyncTask extends DVMObject {
	
	ClassInfo myClass;
	MethodInfo doInBack;
	MethodInfo onPre;
	MethodInfo onPost;
	
	public AsyncTask(DalvikVM vm, ClassInfo type) {
		super(vm, type);
	}

	public final AsyncTask execute(Object[] params) {
		Instruction inst = vm.getCurrtInst();
		Object[] extra = (Object[]) inst.extra;
		int[] args = (int[]) extra[1];
		myClass = vm.getReg(args[0]).getType();
		
		onPre = findOnPre(myClass);
		doInBack = findDoInBack(myClass);
		onPost = findOnPost(myClass);
		
		// Push them into the stack
		// onPost
		Object[] params2 = new Object[2];
		params2[0] = this;
		params2[1] = vm.getReturnReg();
		vm.runMethod(onPost, null, false);
		
		// doIn
		Object[] params3 = new Object[2];
		params3[0] = this;
		params3[1] = params;
		vm.runMethod(doInBack, params3, false);
		
		// onPre
		Object[] params1 = new Object[1];
		params1[0] = this;
		vm.runMethod(onPre, params1, false);


		return this;
	}

	private MethodInfo findDoInBack(ClassInfo clazz) {
		Instruction inst = vm.getCurrtInst();
		Object[] extra = (Object[]) inst.extra;
		int[] args = (int[]) extra[1];
		for (MethodInfo mi : clazz.getAllMethods()) {
			if (containOn(mi.toString())) {
				continue;
			}
			if (mi.toString().contains("doInBack")) {
				return mi;
			}
			boolean res = true;
			for (int i = 0; i < mi.paramTypes.length; i++) {
				if (vm.getReg(args[i + 1]).getType() != mi.paramTypes[i]) {
					res = false;
				}
			}

			if (res) {
				for (Instruction insn : mi.insns) {
					if (containOn(insn.toString())) {
						res = false;
					}
				}
			}

			if (res) {
				return mi;
			}
		}

		return null;
	}

	private boolean containOn(String str) {
		if (str.contains("onPre") || str.contains("onPost")
				|| str.contains("onProgress")) {
			return true;
		}

		return false;
	}

	public Object doInBackground(Object[] paramVarArgs) {
		return null;
	}

	private MethodInfo findOnPre(ClassInfo clazz) {
		for (MethodInfo mi : clazz.getAllMethods()) {
			if (containNotOnPre(mi.toString())) {
				continue;
			}
			if (mi.toString().contains("onPre")) {
				return mi;
			}
			boolean res = true;

			for (Instruction insn : mi.insns) {
				if (containNotOnPre(insn.toString())) {
					res = false;
				}
			}

			if (res) {
				return mi;
			}
		}

		return null;
	}

	private boolean containNotOnPre(String str) {
		if (str.contains("doInBack") || str.contains("onPost")
				|| str.contains("onProgress")) {
			return true;
		}

		return false;
	}

	private MethodInfo findOnPost(ClassInfo clazz) {
		for (MethodInfo mi : clazz.getAllMethods()) {
			if (containNotOnPost(mi.toString())) {
				continue;
			}
			if (mi.toString().contains("onPost")) {
				return mi;
			}
			boolean res = true;

			for (Instruction insn : mi.insns) {
				if (containNotOnPost(insn.toString())) {
					res = false;
				}
			}

			if (res) {
				return mi;
			}
		}

		return null;
	}

	private boolean containNotOnPost(String str) {
		if (str.contains("doInBack") || str.contains("onPre")
				|| str.contains("onProgress")) {
			return true;
		}

		return false;
	}

	public void onPreExecute() {

	}

	public void onPostExecute(Object result) {
		
	}

	protected void onProgressUpdate(Object... values) {

	}

}
