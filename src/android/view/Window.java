//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package android.view;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
//import android.view.accessibility.AccessibilityEvent;

public class Window {
    public static final int FEATURE_OPTIONS_PANEL = 0;
    public static final int FEATURE_NO_TITLE = 1;
    public static final int FEATURE_PROGRESS = 2;
    public static final int FEATURE_LEFT_ICON = 3;
    public static final int FEATURE_RIGHT_ICON = 4;
    public static final int FEATURE_INDETERMINATE_PROGRESS = 5;
    public static final int FEATURE_CONTEXT_MENU = 6;
    public static final int FEATURE_CUSTOM_TITLE = 7;
    public static final int PROGRESS_VISIBILITY_ON = -1;
    public static final int PROGRESS_VISIBILITY_OFF = -2;
    public static final int PROGRESS_INDETERMINATE_ON = -3;
    public static final int PROGRESS_INDETERMINATE_OFF = -4;
    public static final int PROGRESS_START = 0;
    public static final int PROGRESS_END = 10000;
    public static final int PROGRESS_SECONDARY_START = 20000;
    public static final int PROGRESS_SECONDARY_END = 30000;
    protected static final int DEFAULT_FEATURES = 65;
    public static final int ID_ANDROID_CONTENT = 16908290;

    Context context;
    
    public Window(Context context) {
    	this.context = context;
    }

    public final Context getContext() {
    	return context;
    }


    public void setContainer(Window container) {
        throw new RuntimeException("Stub!");
    }

    public final Window getContainer() {
        throw new RuntimeException("Stub!");
    }

    public final boolean hasChildren() {
        throw new RuntimeException("Stub!");
    }

    public void setWindowManager(WindowManager wm, IBinder appToken, String appName) {
        throw new RuntimeException("Stub!");
    }

    public WindowManager getWindowManager() {
        throw new RuntimeException("Stub!");
    }

    public void setCallback(Window.Callback callback) {
        throw new RuntimeException("Stub!");
    }

    public final Window.Callback getCallback() {
        throw new RuntimeException("Stub!");
    }


    public boolean isFloating() {
    	return false;
    }

    public void setLayout(int width, int height) {
        throw new RuntimeException("Stub!");
    }

    public void setGravity(int gravity) {
        throw new RuntimeException("Stub!");
    }

    public void setType(int type) {
        throw new RuntimeException("Stub!");
    }

    public void setFormat(int format) {
        throw new RuntimeException("Stub!");
    }

    public void setWindowAnimations(int resId) {
        throw new RuntimeException("Stub!");
    }

    public void setSoftInputMode(int mode) {
        throw new RuntimeException("Stub!");
    }

    public void addFlags(int flags) {
        throw new RuntimeException("Stub!");
    }

    public void clearFlags(int flags) {
        throw new RuntimeException("Stub!");
    }

    public void setFlags(int flags, int mask) {
        throw new RuntimeException("Stub!");
    }

    public void setAttributes(LayoutParams a) {
        throw new RuntimeException("Stub!");
    }

    private LayoutParams layoutParams;
    
    public final LayoutParams getAttributes() {
    	if (layoutParams == null) {
    		layoutParams = new LayoutParams();
    	}
    	
    	return layoutParams;
    }

    protected final int getForcedWindowFlags() {
        throw new RuntimeException("Stub!");
    }

    protected final boolean hasSoftInputMode() {
        throw new RuntimeException("Stub!");
    }

    public boolean requestFeature(int featureId) {
        throw new RuntimeException("Stub!");
    }

    public final void makeActive() {
        throw new RuntimeException("Stub!");
    }

    public final boolean isActive() {
        throw new RuntimeException("Stub!");
    }

    public View findViewById(int id) {
        throw new RuntimeException("Stub!");
    }

    public void setContentView(int var1) {
    	
    }

    public void setContentView(View var1) {
    	
    }

    public void setContentView(View var1, android.view.ViewGroup.LayoutParams var2) {
    	
    }

    public void addContentView(View var1, android.view.ViewGroup.LayoutParams var2) {
    	
    }

    public View getCurrentFocus() {
    	return null;
    }

    public LayoutInflater getLayoutInflater() {
    	return null;
    }

    public void setTitle(CharSequence var1) {
    }

    public void setTitleColor(int var1) {
    }

    public void openPanel(int var1, KeyEvent var2) {
    	
    }

    public void closePanel(int var1) {
    	
    }

    public void togglePanel(int var1, KeyEvent var2) {
    	
    }

    public boolean performPanelShortcut(int var1, int var2, KeyEvent var3, int var4) {
    	return true;
    }

    public boolean performPanelIdentifierAction(int var1, int var2, int var3) {
    	return true;
    }

    public void closeAllPanels() {
    	
    }

    public boolean performContextMenuIdentifierAction(int var1, int var2) {
    	return true;
    }

    public void onConfigurationChanged(Configuration var1) {
    	
    }

    public void setBackgroundDrawableResource(int resid) {
        throw new RuntimeException("Stub!");
    }

    public void setBackgroundDrawable(Drawable var1) {
    	
    }

    public void setFeatureDrawableResource(int var1, int var2) {
    	
    }

    public void setFeatureDrawableUri(int var1, Uri var2) {
    	
    }

    public void setFeatureDrawable(int var1, Drawable var2) {
    	
    }

    public void setFeatureDrawableAlpha(int var1, int var2) {
    	
    }

    public void setFeatureInt(int var1, int var2) {
    	
    }

    public void takeKeyEvents(boolean var1) {
    	
    }

    public boolean superDispatchKeyEvent(KeyEvent var1) {
    	return true;
    }

    public boolean superDispatchTouchEvent(MotionEvent var1) {
    	return true;
    }

    public boolean superDispatchTrackballEvent(MotionEvent var1) {
    	return true;
    }

    public View getDecorView() {
    	return null;
    }

    public View peekDecorView() {
    	return null;
    }

    public Bundle saveHierarchyState() {
    	return null;
    }

    public void restoreHierarchyState(Bundle var1) {
    	
    }

    protected void onActive() {
    	
    }

    protected final int getFeatures() {
        throw new RuntimeException("Stub!");
    }

    protected final int getLocalFeatures() {
        throw new RuntimeException("Stub!");
    }

    protected void setDefaultWindowFormat(int format) {
        throw new RuntimeException("Stub!");
    }

    public void setChildDrawable(int var1, Drawable var2) {
    	
    }

    public void setChildInt(int var1, int var2) {
    	
    }

    public boolean isShortcutKey(int var1, KeyEvent var2) {
    	return true;
    }

    public void setVolumeControlStream(int var1) {
    	
    }

    public int getVolumeControlStream() {
    	return 1;
    }

    public interface Callback {
        boolean dispatchKeyEvent(KeyEvent var1);

        boolean dispatchTouchEvent(MotionEvent var1);

        boolean dispatchTrackballEvent(MotionEvent var1);

        //boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent var1);

        View onCreatePanelView(int var1);

        boolean onCreatePanelMenu(int var1, Menu var2);

        boolean onPreparePanel(int var1, View var2, Menu var3);

        boolean onMenuOpened(int var1, Menu var2);

        boolean onMenuItemSelected(int var1, MenuItem var2);

        //void onWindowAttributesChanged(LayoutParams var1);

        void onContentChanged();

        void onWindowFocusChanged(boolean var1);

        void onAttachedToWindow();

        void onDetachedFromWindow();

        void onPanelClosed(int var1, Menu var2);

        boolean onSearchRequested();
    }
}
