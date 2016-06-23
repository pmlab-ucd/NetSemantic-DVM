package android.view;

import android.view.ViewGroup.LayoutParams;

//public abstract interface WindowManager extends ViewManager {
public class WindowManager implements ViewManager {
	Display display = new Display();

	@Override
	public void addView(View paramView, LayoutParams paramLayoutParams) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateViewLayout(View paramView, LayoutParams paramLayoutParams) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeView(View paramView) {
		// TODO Auto-generated method stub
		
	}
	
	public Display getDefaultDisplay() {
		return display;
	}
	

}
