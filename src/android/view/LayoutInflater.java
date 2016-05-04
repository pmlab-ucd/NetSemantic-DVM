package android.view;

import patdroid.core.ClassInfo;
import fu.hao.trust.dvm.DVMObject;
import fu.hao.trust.dvm.DalvikVM;
import android.content.Context;

public class LayoutInflater extends DVMObject {
	public LayoutInflater(DalvikVM vm, ClassInfo type) {
		super(vm, type);
		// TODO Auto-generated constructor stub
	}

	public static LayoutInflater from(Context context) {

		return new LayoutInflater(context.getVM(),
				ClassInfo.findClass("android.view.LayoutInflater"));

	}

	public View inflate(int resource, ViewGroup root) {
		return new View(vm, type);
	}

}
