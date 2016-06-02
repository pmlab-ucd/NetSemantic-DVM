package android.myclasses;

import android.app.Fragment;
import android.app.ListFragment;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import fu.hao.trust.dvm.DVMObject;
import fu.hao.trust.dvm.DalvikVM;
import fu.hao.trust.utils.Log;
import patdroid.core.ClassInfo;

/**
 * @ClassName: GenInstance
 * @Description: Return an instance of Android API classes
 * @author: Hao Fu
 * @date: May 7, 2016 10:26:38 AM
 */
public class GenInstance {

	private final static String TAG = GenInstance.class.getSimpleName();
	
	public static DVMObject getInstance(DalvikVM vm, ClassInfo type) {
		return null;
	}
	
	public static View getView(DalvikVM vm, ClassInfo type, int id) {
		Log.bb(TAG, "Gen view instance with " + type + ", id " + id);
		ClassInfo oType = type;
		while (type != null) {
			String typeName = type.toString();
			if (typeName.contains("TextView")) {
				return new TextView(vm, oType, id);
			} else if (typeName.contains("ListView")) {
				return new ListView(vm, oType, id);
			} else if (typeName.contains("ImageView")) {
				return new ImageView(vm, oType, id);
			} else if (typeName.contains("ImageButton")) {
				return new ImageButton(vm, oType, id);
			} else if (typeName.startsWith("Button")) {
				return new Button(vm, oType, id);
			}
			
			type = type.getSuperClass();
		}
		
		return new View(vm, oType, id);
	}
	
	public static Fragment getFragment(DalvikVM vm, ClassInfo type) {
		ClassInfo oType = type;
		Log.bb(TAG, "Gen fragment instance with " + type);
		while (type != null) {
			String typeName = type.toString();
			 if (typeName.contains("ListFragment")) {
				 return new ListFragment(vm, oType);
			 }
			 
			 type = type.getSuperClass();
		}
		
		return new Fragment(vm, oType);
	}
	

}
