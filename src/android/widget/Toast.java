package android.widget;

import android.content.Context;

public class Toast {
	
	CharSequence text;
	int duration;

	public void show() { 
		System.out.println("[Toast: SHOW!]");
	}
	
	public static Toast makeText(Context context, CharSequence text, int duration) {
		System.out.println("[Toast: " + text + "]");
		return new Toast();
	}
	
	public void setGravity(int gravity, int xOffset, int yOffset) { 
		
	}
	
	public void setText(CharSequence s) { 
		text = s;
	}
	
	public void setDuration(int duration) {
		this.duration = duration;
	}

}
