package android.app;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.opencsv.CSVReader;

import android.content.ContextWrapper;
import android.content.Intent;
import android.location.LocationListener;
import android.myclasses.GenInstance;
import android.view.View;
import patdroid.core.ClassInfo;
import fu.hao.trust.dvm.DVMObject;
import fu.hao.trust.dvm.DalvikVM;
import fu.hao.trust.dvm.DalvikVM.StackFrame;
import fu.hao.trust.utils.Log;
import fu.hao.trust.utils.Pair;
import fu.hao.trust.utils.Settings;

public class Activity extends ContextWrapper implements LocationListener {

	Map<Integer, View> views;
	final String TAG = getClass().getSimpleName();
	Intent intent;
	FragmentManager fragmentManager;
	private static Map<Integer, ClassInfo> widgetPool;
	private View tmpView;
	
	public Activity(DalvikVM vm, ClassInfo type, Intent intent) {
		super(vm, type);
		init(vm, type, intent);
	}
	
	public Activity(DalvikVM vm, ClassInfo type) {
		super(vm, type);
		init(vm, type, null);
	}
	
	public void init(DalvikVM vm, ClassInfo type, Intent intent) {
		Log.bb(TAG, "New Activity Created with type " + type);
		this.intent = intent;
		views = new HashMap<>();
		if (Settings.execOnCreate) {
			if (vm.getCurrStackFrame() != null && type.findMethods("onCreate")[0].equals(vm.getCurrStackFrame().getMethod())) {
				return;
			}
			@SuppressWarnings("unchecked")
			Pair<Object, ClassInfo>[] params = (Pair<Object, ClassInfo>[]) new Pair[2]; 
			params[0] = new Pair<Object, ClassInfo>(this, type);
			params[1] = null;
			StackFrame frame = vm.newStackFrame(type, type.findMethods("onCreate")[0], params, false);
			frame.setIntent(intent);
			Log.bb(TAG, "Intent " + intent);
			vm.runInstrumentedMethods(frame);
		}
	}
	
	public void setContentView(int view) {

	}
	
    public Intent getIntent() {
    	return intent;
    }

	public View findViewById(int id) {
		if (views.containsKey(id)) {
			return views.get(id);
		}
	
		View view = null;
		if (widgetPool.containsKey(id)) {
			view = GenInstance.getView(vm, widgetPool.get(id), id);
			view.callDefaultConstructor();
		} else {
			Log.err(TAG, "Cannot find the view with id " + id);
		}
		
		views.put(id, view);
		
		return views.get(id);
	}
	
	public static void xmlViewDefs() {
		try {
			widgetPool = new HashMap<>();
			String csv = Settings.getOutdir() + Settings.getApkName() + "_nid-views.csv";
			File file = new File(csv);
			if (file.exists()) {
				CSVReader reader = new CSVReader(new FileReader(csv));
				for (String[] id_view : reader.readAll()) {
					id_view[0] = id_view[0].replace("x", "");
					int nid = Integer.parseInt(id_view[0], 16); 
					System.out.println(nid);
					ClassInfo clazz = ClassInfo.findClass(id_view[1]);
					if (clazz == null) {
						clazz = ClassInfo.findClass("android.view." + id_view[1]);
						if (clazz == null) {
							clazz = ClassInfo.findClass("android.widget." + id_view[1]);
						}
					}
					System.out.println(id_view[1]);
					System.out.println(clazz);
					widgetPool.put(nid, clazz);
				}

				reader.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public String toString() {
		return "Activity:" + super.toString();
	}
	
    public FragmentManager getFragmentManager() {
        if (fragmentManager == null) {
        	fragmentManager = new FragmentManager(this);
        }
        
        return fragmentManager;
    }

	public Collection<Fragment> getFragments() {
		return getFragmentManager().getFragments();
	}

	
	public Set<DVMObject> getAllUIs() {
		Set<DVMObject> items = new HashSet<>();
		items.addAll(getFragments());
		items.addAll(views.values());
		return items;
	}
	
	public static Map<Integer, ClassInfo> getWidgetPool() {
		return widgetPool;
	}

	public View getTmpView() {
		return tmpView;
	}

	public void setTmpView(View tmpView) {
		this.tmpView = tmpView;
	}
	
}
