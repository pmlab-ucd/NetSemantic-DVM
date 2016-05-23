package fu.hao.trust.dvm;

import static org.junit.Assert.*;

import org.junit.Test;

import android.content.Intent;
import fu.hao.trust.data.Results;
import fu.hao.trust.utils.Settings;

/**
 * @ClassName: WowotuanCAonReceiveTests
 * @Description: 在 CityChoiceActivity/c() 的t.c()即e.g.c()调用baidu api查询位置
在 e.h中继承了baidu sdk的onReceiveLocation, 用于处理收到baidu反馈的行为: 初始化
一个intent把baidu的结果发送出去.
gui.activity.ca的onReceive接收后显示在ui上.

 * @author: Hao Fu
 * @date: May 7, 2016 9:58:54 AM
 */
public class WowotuanCAonReceiveTests {

	@Test
	public void testWo_() {
		String[] args = new String[4];
		args[3] = "Full";
		Settings.logLevel = 0;
		
		args[0] = "C:/Users/hao/workspace/DroidBenchProj/apks/benign/wowotuan/com.wowotuan.appfactory.wangpintaisuniupai/7619303.html.apk";

		args[1] = "com.wowotuan.appfactory.gui.activity.ca";
		args[2] = "onReceive";
		Object[] initArgs = new Object[2];
		Intent intent = new Intent("com.wowotuan.appfactory.broadcast.location");
		intent.putExtra("location", "121.1, 131.1");
		initArgs[1] = intent; 
		initArgs[0] = "NULL";
		Settings.addCallBlkListElem("com.d.a");
		Settings.addCallBlkListElem("com.wowotuan.appfactory.gui.activity.CityChoiceActivity/d");
		Settings.addCallBlkListElem("com.wowotuan.appfactory.gui.activity.CityChoiceActivity/b");
		Settings.execOnCreate = true;
		Main.main(args, initArgs, null, null);
		assertEquals(true, Results.results.isEmpty());
	}
	
	@Test
	public void test7625393() {
		String[] args = new String[4];
		args[3] = "Full";
		Settings.logLevel = 0;

		args[0] = "C:/Users/hao/workspace/DroidBenchProj/apks/benign/wowotuan/com.wowotuan.appfactory.beiermeiyingtongshishangguan/7625393.html.apk";

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
		Main.main(args, initArgs, null, null);
		System.out.println("REs: " + Results.results);
		System.out.println("REs: " + Results.targetCallRes);
		assertEquals(false, Results.targetCallRes.isEmpty());
		assertEquals(true, Results.targetCallRes.values().iterator().next()
				.getInfluAPIs().toString().contains("show"));
		assertEquals(false, Results.targetCallRes.values().iterator().next()
				.getFedViews().isEmpty());
	}
}
