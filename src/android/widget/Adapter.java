package android.widget;

import android.view.View;
import android.view.ViewGroup;

public abstract interface Adapter {
	  public static final int IGNORE_ITEM_VIEW_TYPE = -1;
	  public static final int NO_SELECTION = Integer.MIN_VALUE;
	  
	  //public abstract void registerDataSetObserver(DataSetObserver paramDataSetObserver);
	  
	  //public abstract void unregisterDataSetObserver(DataSetObserver paramDataSetObserver);
	  
	  public abstract int getCount();
	  
	  public abstract Object getItem(int paramInt);
	  
	  public abstract long getItemId(int paramInt);
	  
	  public abstract boolean hasStableIds();
	  
	  public abstract View getView(int paramInt, View paramView, ViewGroup paramViewGroup);
	  
	  public abstract int getItemViewType(int paramInt);
	  
	  public abstract int getViewTypeCount();
	  
	  public abstract boolean isEmpty();
}
