package android.view;

import patdroid.core.ClassInfo;
import fu.hao.trust.dvm.DVMObject;
import fu.hao.trust.dvm.DalvikVM;

public class View extends DVMObject {
	int color;
	
	class OnClickListener {
		
	}

	public View(DalvikVM vm, ClassInfo type) {
		super(vm, type);
		// TODO Auto-generated constructor stub
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
}
