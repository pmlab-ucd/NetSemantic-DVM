package android.location;

import patdroid.core.ClassInfo;
import fu.hao.trust.dvm.DVMObject;
import fu.hao.trust.dvm.DalvikVM;

public class MyLocationListener extends DVMObject implements LocationListener {

	public MyLocationListener(DalvikVM vm, ClassInfo type) {
		super(vm, type);
	}

}
