package android.view;

import patdroid.core.ClassInfo;
import fu.hao.trust.dvm.DVMObject;
import fu.hao.trust.dvm.DalvikVM;

public class View extends DVMObject {
	int color;
	
	class OnClickListener {
		
	}

	public View(DalvikVM vm, ClassInfo type, int mid) {
		super(vm, type);
		this.mID = mid;
	}

	public int mID;
	
	private int visibility;
	
	public void setVisibility(int visibility) {
		this.visibility = visibility;
	}
	
	public int getVisibility() {
		return visibility;
	}
	
	public void setBackgroundColor(int color) {
		this.color = color;
	}
	
	public void setOnClickListener(OnClickListener l) {
		
	}
	
	public void setType() {
		
	}
}
