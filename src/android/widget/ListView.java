package android.widget;

import patdroid.core.ClassInfo;
import fu.hao.trust.dvm.DalvikVM;
import android.view.View;

public class ListView extends View {
	
	View headerView;
	ListAdapter adapter;
	
	private int choiceMode;

	public ListView(DalvikVM vm, ClassInfo type, int mid) {
		super(vm, type, mid);
	}
	
	public void addHeaderView(View v) { 
		headerView = v; 
	}
	
	public void setAdapter(ListAdapter adapter) { 
		this.adapter = adapter;
	}
	
    public void setChoiceMode(int choiceMode) {
       this.choiceMode = choiceMode; 
    }
	
    public int getChoiceMode() {
       return choiceMode; 
    }
}
