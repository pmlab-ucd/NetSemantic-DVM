package android.widget;

import patdroid.core.ClassInfo;
import fu.hao.trust.dvm.DalvikVM;
import android.view.View;

public class TextView extends View {
	public TextView(DalvikVM vm, ClassInfo type, int mid) {
		super(vm, type, mid);
	}

	CharSequence text;
	
	public CharSequence getText() {
		return text;
	}
	
	public void setText(CharSequence text) {
		this.text = text;
	}
	
}
