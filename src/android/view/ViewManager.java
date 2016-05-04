package android.view;

public abstract interface ViewManager {
  public abstract void addView(View paramView, ViewGroup.LayoutParams paramLayoutParams);
  
  public abstract void updateViewLayout(View paramView, ViewGroup.LayoutParams paramLayoutParams);
  
  public abstract void removeView(View paramView);
}