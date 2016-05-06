package fu.hao.trust.test;

import static org.junit.Assert.*;

import org.junit.Test;

import fu.hao.trust.data.Results;
import fu.hao.trust.dvm.Main;
import fu.hao.trust.utils.Settings;

public class WowotuanCtyChoiceTests {

	/**
	* @Title: test7619303
	* @Author: Hao Fu
	* @Description: Search [MSG]: Add fedView instance number 7@android.view.View
	* 且influAPI中包含e.i/b()中的Toast.show()
	* @param   
	* @return void   
	* @throws
	*/
	@Test
	public void test7619303() {
		String[] args = new String[4];
		args[3] = "Full";
		Settings.logLevel = 0;

		args[0] = "C:/Users/hao/workspace/DroidBenchProj/apks/benign/wowotuan/com.wowotuan.appfactory.wangpintaisuniupai/7619303.html.apk";

		args[1] = "com.wowotuan.appfactory.gui.activity.CityChoiceActivity";
		args[2] = "onCreate";
		Object[] initArgs = new Object[2];
		initArgs[0] = "NULL";
		initArgs[1] = "NULL";

		Settings.addCallBlkListElem("com.d.a.j/<init>"); // will lead to at
															// least 369 calls..
		Settings.addCallBlkListElem("com.d.a.b.a.bf/<init>");
		Settings.addCallBlkListElem("com.d.a.d.a");
		Settings.addCallBlkListElem("com.d.a");
		Main.initMI(initArgs);
		Main.main(args);
		System.out.println("REs: " + Results.results);
		System.out.println("REs: " + Results.targetCallRes);
		assertEquals(false, Results.targetCallRes.isEmpty());
		assertEquals(true, Results.targetCallRes.values().iterator().next()
				.getInfluAPIs().toString().contains("show"));
		assertEquals(false, Results.targetCallRes.values().iterator().next()
				.getFedViews().isEmpty());
	}
	
	@Test
	public void test7625128() {
		String[] args = new String[4];
		args[3] = "Full";
		Settings.logLevel = 0;

		args[0] = "C:/Users/hao/workspace/DroidBenchProj/apks/benign/wowotuan/com.wowotuan.appfactory.aiduoliqichemeirong/7625128.html.apk";

		args[1] = "com.wowotuan.appfactory.gui.activity.CityChoiceActivity";
		args[2] = "onCreate";
		Object[] initArgs = new Object[2];
		initArgs[0] = "NULL";
		initArgs[1] = "NULL";

		Settings.addCallBlkListElem("com.d.a.j/<init>"); // will lead to at
															// least 369 calls..
		Settings.addCallBlkListElem("com.d.a.b.a.bf/<init>");
		Settings.addCallBlkListElem("com.d.a.d.a");
		Settings.addCallBlkListElem("com.d.a");
		Main.initMI(initArgs);
		Main.main(args);
		System.out.println("REs: " + Results.results);
		System.out.println("REs: " + Results.targetCallRes);
		assertEquals(false, Results.targetCallRes.isEmpty());
		assertEquals(true, Results.targetCallRes.values().iterator().next()
				.getInfluAPIs().toString().contains("show"));
		assertEquals(false, Results.targetCallRes.values().iterator().next()
				.getFedViews().isEmpty());
	}
	
	@Test
	public void test7625099() {
		String[] args = new String[4];
		args[3] = "Full";
		Settings.logLevel = 0;

		args[0] = "C:/Users/hao/workspace/DroidBenchProj/apks/benign/wowotuan/com.wowotuan.appfactory.aiduoliqichemeirong/7625099.html.apk";

		args[1] = "com.wowotuan.appfactory.gui.activity.CityChoiceActivity";
		args[2] = "onCreate";
		Object[] initArgs = new Object[2];
		initArgs[0] = "NULL";
		initArgs[1] = "NULL";

		Settings.addCallBlkListElem("com.d.a.j/<init>"); // will lead to at
															// least 369 calls..
		Settings.addCallBlkListElem("com.d.a.b.a.bf/<init>");
		Settings.addCallBlkListElem("com.d.a.d.a");
		Settings.addCallBlkListElem("com.d.a");
		Main.initMI(initArgs);
		Main.main(args);
		System.out.println("REs: " + Results.results);
		System.out.println("REs: " + Results.targetCallRes);
		assertEquals(false, Results.targetCallRes.isEmpty());
		assertEquals(true, Results.targetCallRes.values().iterator().next()
				.getInfluAPIs().toString().contains("show"));
		assertEquals(false, Results.targetCallRes.values().iterator().next()
				.getFedViews().isEmpty());
	}
	
	

}
