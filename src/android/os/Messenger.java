package android.os;

import patdroid.core.ClassInfo;
import fu.hao.trust.dvm.DalvikVM;
import fu.hao.trust.dvm.DalvikVM.StackFrame;
import fu.hao.trust.utils.Log;
import fu.hao.trust.utils.Pair;
import fu.hao.trust.utils.Settings;

public class Messenger {

	IBinder target;
	Handler handler;

	private String TAG = getClass().getSimpleName();

	public Messenger() {

	}

	public Messenger(Handler target) {
		this.handler = target;
	}

	public void send(Message message) {
		if (handler != null) {
			Log.msg(TAG, "Begin run the methods of the handler " + handler);
			@SuppressWarnings("unchecked")
			Pair<Object, ClassInfo>[] params = (Pair<Object, ClassInfo>[]) new Pair[2];
			params[0] = new Pair<Object, ClassInfo>(handler, handler.getType());
			params[1] = new Pair<Object, ClassInfo>(message, ClassInfo.findClass("android.os.Message"));
			DalvikVM vm = Settings.getVM();
			StackFrame frame = vm.newStackFrame(handler.getType(),
					handler.getClazz().findMethods("handleMessage")[0], params, false);
			vm.runInstrumentedMethods(frame);
		} else {
			Log.err(TAG, "Cannot locate the handler");
		}
	}

	public Messenger(IBinder target) {
		if (target instanceof IBinderInstance) {
			IBinderInstance binder = (IBinderInstance) target;
			handler = binder.getHandler();
		}
		this.target = target;
	}

	public IBinder getBinder() {
		return new IBinderInstance(handler);
	}

}
