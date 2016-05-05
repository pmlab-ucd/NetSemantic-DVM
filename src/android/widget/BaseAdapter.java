package android.widget;

import android.view.View;
import android.view.ViewGroup;
import patdroid.core.ClassInfo;
import fu.hao.trust.dvm.DVMObject;
import fu.hao.trust.dvm.DalvikVM;

public class BaseAdapter extends DVMObject implements ListAdapter {

	public BaseAdapter(DalvikVM vm, ClassInfo type) {
		super(vm, type);
		// TODO Auto-generated constructor stub
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Object getItem(int paramInt) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int paramInt) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean hasStableIds() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public View getView(int paramInt, View paramView, ViewGroup paramViewGroup) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getItemViewType(int paramInt) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int getViewTypeCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean areAllItemsEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isEnabled(int paramInt) {
		// TODO Auto-generated method stub
		return false;
	}

}
