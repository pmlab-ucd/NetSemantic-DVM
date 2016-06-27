package fu.hao.trust.test;

import static org.junit.Assert.*;

import org.junit.Test;

import android.content.Intent;
import fu.hao.trust.data.Results;
import fu.hao.trust.dvm.Main;
import fu.hao.trust.utils.Settings;

public class DroidKunfu4_2 {

	@Test
	public void test() {
		String[] args = new String[9];
		args[3] = "ATaint";
		Settings.logLevel = 0;

		args[0] = "D:/malwares/DroidKungFu4/e1c2188a69727bf4ec4a5d72319cfe87428c7f35/";
		args[1] = "com.adwo.adsdk.AdwoSplashAdActivity";
		args[2] = "onCreate";
		args[4] = "--norun";
		args[5] = "com.safesys.remover.JmAdV2/initJu6Ad";
		args[6] = "--norun";
		args[7] = "com.adwo.adsdk.i/c";
	
		Intent intent = new Intent();
		intent.putExtra("Adwo_PID", "ce8a177663264f518fe8727d375d05a7");
		Settings.setTriggerIntent(intent);

		Main.main(args);

		assertEquals(false, Results.results.isEmpty());
	}

}
