package fu.hao.netsemantic.test.droidbench;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import android.telephony.TelephonyManager;
import fu.hao.trust.data.Results;
import fu.hao.trust.dvm.Main;
import fu.hao.trust.utils.Settings;

public class BenchAliasing {
	String[] args = new String[4];
	TelephonyManager tm;

	@Before
	public void prepare() {
		Settings.logLevel = 0;
		args[3] = "ATaint";
		tm = new TelephonyManager();
	}

	@Test
	public void Merge1() {
		args[0] = "C:/Users/hao/workspace/DroidBenchProj/Merge1/app/";
		args[1] = "srcEventChains";
		Main.main(args);
		assertEquals(true, Results.results.isEmpty());
	}

}
