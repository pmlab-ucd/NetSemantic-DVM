package android.widget;

import patdroid.core.ClassInfo;
import fu.hao.trust.dvm.DalvikVM;
import android.view.View;

public class ListView extends View {
	
	View headerView;
	ListAdapter adapter;

	public ListView(DalvikVM vm, ClassInfo type) {
		super(vm, type);
	}
	
	public void addHeaderView(View v) { 
		headerView = v; 
	}
	
	public void setAdapter(ListAdapter adapter) { 
		this.adapter = adapter;
	}
	

}
