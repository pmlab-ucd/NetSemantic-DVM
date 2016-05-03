package android.widget;

import patdroid.core.ClassInfo;
import fu.hao.trust.dvm.DalvikVM;
import android.view.View;

public class TextView extends View {
	public TextView(DalvikVM vm, ClassInfo type) {
		super(vm, type);
		// TODO Auto-generated constructor stub
	}

	CharSequence text;
	
	public CharSequence getText() {
		return text;
	}
	
	public void setText(CharSequence text) {
		this.text = text;
	}
	
}
