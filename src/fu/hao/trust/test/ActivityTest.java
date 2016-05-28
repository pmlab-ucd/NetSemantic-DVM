package fu.hao.trust.test;

import static org.junit.Assert.*;

import org.junit.Test;

import patdroid.core.ClassInfo;
import android.app.Activity;
import fu.hao.trust.utils.Settings;

public class ActivityTest {

	@Test
	public void testGenViewsByIds() {
		ClassInfo clazz = ClassInfo.findClass("java.lang.String");
		System.out.println(clazz);
		Settings.setApkName("FragmentLifecycle2.apk");
		Activity.xmlViewDefs();
		assertEquals(true, Activity.getWidgetPool().containsKey(2131034113));
	}

}
