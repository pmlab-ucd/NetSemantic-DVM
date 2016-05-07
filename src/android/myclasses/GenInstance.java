package android.myclasses;

import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import fu.hao.trust.dvm.DVMObject;
import fu.hao.trust.dvm.DalvikVM;
import patdroid.core.ClassInfo;

/**
 * @ClassName: GenInstance
 * @Description: Return an instance of Android API classes
 * @author: Hao Fu
 * @date: May 7, 2016 10:26:38 AM
 */
public class GenInstance {
	
	public static DVMObject getInstance(DalvikVM vm, ClassInfo type) {
		// TODO
		return null;
	}
	
	public static View getView(DalvikVM vm, ClassInfo type) {
		ClassInfo oType = type;
		String typeName = type.toString();
		while (type.getSuperClass() != null) {
			if (typeName.contains("TextView")) {
				return new TextView(vm, oType);
			} else if (typeName.contains("ListView")) {
				return new ListView(vm, oType);
			} else if (typeName.contains("ImageView")) {
				return new ImageView(vm, oType);
			} else if (typeName.contains("ImageButton")) {
				return new ImageButton(vm, oType);
			}
			
			type = type.getSuperClass();
			typeName = type.toString();
		}
		
		return new View(vm, type);
	}
	

}
