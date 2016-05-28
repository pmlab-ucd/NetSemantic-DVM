package android.app;

import android.widget.ListView;
import patdroid.core.ClassInfo;
import fu.hao.trust.dvm.DalvikVM;

public class ListFragment extends Fragment {
	
	ListView listView;
	
	public ListFragment(DalvikVM vm, ClassInfo clazz) {
		super(vm, clazz);
		listView = new ListView(vm, ClassInfo.findClass("android.widget.ListView"), -1);
	}
	
    public ListView getListView() {
    	return listView; 
    }
}
