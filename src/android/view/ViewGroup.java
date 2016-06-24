package android.view;

import patdroid.core.ClassInfo;
import fu.hao.trust.dvm.DalvikVM;

public class ViewGroup extends View {

	public ViewGroup(DalvikVM vm, ClassInfo type, int mid) {
		super(vm, type, mid);
		// TODO Auto-generated constructor stub
	}
	
    public static class LayoutParams {
    	public LayoutParams() {
    		
    	}
    	
    	public LayoutParams(int _type) {
    		
    	}
    	
		public LayoutParams(android.view.ViewGroup.LayoutParams layoutParams) {
			// TODO Auto-generated constructor stub
		}
		public static final int FILL_PARENT = -1;
        public static final int MATCH_PARENT = -1;
        public static final int WRAP_CONTENT = -2;
        public int width;
        public int height;
    }

}
