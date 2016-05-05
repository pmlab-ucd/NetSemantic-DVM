package android.os;

import patdroid.core.ClassInfo;
import patdroid.core.MethodInfo;
import patdroid.dalvik.Instruction;
import fu.hao.trust.dvm.DVMObject;
import fu.hao.trust.dvm.DalvikVM;

public class AsyncTask extends DVMObject {
	
	final String TAG = getClass().getSimpleName(); 
	
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
		vm.runMethod(type, onPost, params2, false);
		
		// doIn
		Object[] params3 = new Object[2];
		params3[0] = this;
		params3[1] = params;
		vm.runMethod(type, doInBack, params3, false);
		
		// onPre
		Object[] params1 = new Object[1];
		params1[0] = this;
		vm.runMethod(type, onPre, params1, false);

		return this;
	}

	private MethodInfo findDoInBack(ClassInfo clazz) {
		for (MethodInfo mi : clazz.getAllMethods()) {
			if (mi.toString().contains("doInBack")) {
				return mi;
			}
		}

		return null;
	}

	public Object doInBackground(Object[] paramVarArgs) {
		return null;
	}

	private MethodInfo findOnPre(ClassInfo clazz) {
		for (MethodInfo mi : clazz.getAllMethods()) {
			if (mi.toString().contains("onPre")) {
				return mi;
			}
		}

		return null;
	}


	private MethodInfo findOnPost(ClassInfo clazz) {
		for (MethodInfo mi : clazz.getAllMethods()) {
			if (mi.toString().contains("onPost")) {
				return mi;
			}
		}

		return null;
	}

	public void onPreExecute() {

	}

	public void onPostExecute(Object result) {
		
	}

	protected void onProgressUpdate(Object... values) {

	}

}
