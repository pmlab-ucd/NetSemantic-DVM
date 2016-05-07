package android.os;

import java.util.LinkedList;

import patdroid.core.ClassInfo;
import patdroid.core.MethodInfo;
import patdroid.dalvik.Instruction;
import fu.hao.trust.dvm.DVMObject;
import fu.hao.trust.dvm.DalvikVM;
import fu.hao.trust.dvm.DalvikVM.StackFrame;
import fu.hao.trust.utils.Pair;

public class AsyncTask extends DVMObject {
	
	final String TAG = getClass().getSimpleName(); 
	
	ClassInfo myClass;
	MethodInfo doInBack;
	MethodInfo onPre;
	MethodInfo onPost;
	
	public AsyncTask(DalvikVM vm, ClassInfo type) {
		super(vm, type);
	}

	@SuppressWarnings("unchecked")
	public final AsyncTask execute(Object[] params) {
		Instruction inst = vm.getCurrtInst();
		Object[] extra = (Object[]) inst.extra;
		int[] args = (int[]) extra[1];
		myClass = vm.getReg(args[0]).getType();
		
		onPre = findOnPre(myClass);
		doInBack = findDoInBack(myClass);
		onPost = findOnPost(myClass);
		
		Pair<Object, ClassInfo>[] ctxObjs;
		LinkedList<StackFrame> tmpFrames = new LinkedList<>();
		
		// Push them into the stack
		// onPost
		Object[] params2 = new Object[2];
		params2[0] = this;
		params2[1] = vm.getReturnReg();
		
		ctxObjs = (Pair<Object, ClassInfo>[]) new Pair[2]; 
		ctxObjs[0] = new Pair<Object, ClassInfo>(this, type);
		ctxObjs[1] = new Pair<Object, ClassInfo>(vm.getReturnReg(), doInBack.returnType);
		tmpFrames.add(vm.newStackFrame(type, onPost, ctxObjs, false));
		
		// doIn
		ctxObjs = (Pair<Object, ClassInfo>[]) new Pair[2]; ;
		ctxObjs[0] = new Pair<Object, ClassInfo>(this, type);
		ctxObjs[1] = new Pair<Object, ClassInfo>(params, doInBack.paramTypes[0]);
		tmpFrames.add(vm.newStackFrame(type, doInBack, ctxObjs, false));
		
		// onPre
		ctxObjs = (Pair<Object, ClassInfo>[]) new Pair[1]; 
		ctxObjs[0] = new Pair<Object, ClassInfo>(this, type);
		tmpFrames.add(vm.newStackFrame(type, onPre, ctxObjs, false));
		vm.setTmpFrames(tmpFrames, false);

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
