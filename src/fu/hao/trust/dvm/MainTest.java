package fu.hao.trust.dvm;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import fu.hao.trust.analysis.Results;
import fu.hao.trust.utils.Settings;

public class MainTest {
	String[] args = new String[3];

	public MainTest() {
		Settings.logLevel = 0;
	}
	
	//@Test
	public void test() {
		args[0] = "C:/Users/hao/workspace/DroidBenchProj/apks/app-debug.apk";
		args[1] = "de.ecspride.LoopExample1";
		Main.main(args);
		Map<String, String> res = new HashMap<>();
		res.put("<android.telephony.SmsManager: void sendTextMessage(java.lang.String,java.lang.String,java.lang.String,android.app.PendingIntent,android.app.PendingIntent)>",
				"3_5_9_8_7_4_0_4_3_1_1_6_9_0_9_");
		assertEquals(Results.results.contains(res), true);
	}

	@Test
	public void testGeneralJava_Loop1() {
		args[0] = "C:/Users/hao/workspace/GeneralJava_Loop1/app/app-debug.apk";
		args[1] = "de.ecspride.LoopExample1";
		args[2] = "onCreate";
		Main.main(args);
		Map<String, String> res = new HashMap<>();
		res.put("<android.telephony.SmsManager: void sendTextMessage(java.lang.String,java.lang.String,java.lang.String,android.app.PendingIntent,android.app.PendingIntent)>",
				"3_5_9_8_7_4_0_4_3_1_1_6_9_0_9_");
		assertEquals(true, Results.results.contains(res));
	}

	@Test
	public void testGeneralJava_Loop2() {
		args[0] = "C:/Users/hao/workspace/GeneralJava_Loop2/app/app-debug.apk";
		args[1] = "de.ecspride.LoopExample2";
		args[2] = "onCreate";
		Main.main(args);
		Map<String, String> res = new HashMap<>();
		res.put("<android.telephony.SmsManager: void sendTextMessage(java.lang.String,java.lang.String,java.lang.String,android.app.PendingIntent,android.app.PendingIntent)>",
				"3_5_9_8_7_4_0_4_3_1_1_6_9_0_9_");
		assertEquals(Results.results.contains(res), true);
	}
	
	@Test
	public void testGeneralJava_SourceCodeSpecific1() {
		args[0] = "C:/Users/hao/workspace/DroidBenchProj/GeneralJava_SourceCodeSpecific1/app/app-debug.apk";
		args[1] = "de.ecspride.MainActivity";
		args[2] = "onCreate";
		Main.main(args);
		Map<String, String> res = new HashMap<>();
		res.put("<android.telephony.SmsManager: void sendTextMessage(java.lang.String,java.lang.String,java.lang.String,android.app.PendingIntent,android.app.PendingIntent)>",
				"359874043116909");
		assertEquals(true, Results.results.contains(res));
	}

	@Test
	public void testGeneralJava_StaticInitialization1() {
		args[0] = "C:/Users/hao/workspace/DroidBenchProj/GeneralJava_StaticInitialization1/app/app-release.apk";
		args[1] = "de.ecspride.MainActivity";
		args[2] = "onCreate";
		Main.main(args);
		Map<String, String> res = new HashMap<>();
		res.put("<android.telephony.SmsManager: void sendTextMessage(java.lang.String,java.lang.String,java.lang.String,android.app.PendingIntent,android.app.PendingIntent)>",
				"359874043116909");
		assertEquals(Results.results.contains(res), true);
	}
	
	@Test
	public void testGeneralJava_StaticInitialization2() {
		args[0] = "C:/Users/hao/workspace/DroidBenchProj/GeneralJava_StaticInitialization2/app/app-release.apk";
		args[1] = "de.ecspride.MainActivity";
		args[2] = "onCreate";
		Main.main(args);
		Map<String, String> res = new HashMap<>();
		res.put("<android.telephony.SmsManager: void sendTextMessage(java.lang.String,java.lang.String,java.lang.String,android.app.PendingIntent,android.app.PendingIntent)>",
				"359874043116909");
		assertEquals(Results.results.contains(res), true);
	}
	
	@Test
	public void testGeneralJava_UnreachableCode() {
		args[0] = "C:/Users/hao/workspace/DroidBenchProj/GeneralJava_UnreachableCode/app/app-release.apk";
		args[1] = "de.ecspride.UnreachableCode";
		args[2] = "onCreate";
		Main.main(args);
		assertEquals(Results.results.isEmpty(), true);
	}
	
	@Test
	public void testGeneralJava_VirtualDispatch1() {
		args[0] = "C:/Users/hao/workspace/DroidBenchProj/GeneralJava_VirtualDispatch1/app/";
		args[1] = "de.ecspride.VirtualDispatch1";
		args[2] = null;
		//args[2] = "clickButton";
		Main.main(args);
		Map<String, String> res = new HashMap<>();
		res.put("<android.telephony.SmsManager: void sendTextMessage(java.lang.String,java.lang.String,java.lang.String,android.app.PendingIntent,android.app.PendingIntent)>",
				"359874043116909");
		assertEquals(Results.results.contains(res), true);
	}
	
	@Test
	public void testFieldAndObjectSensitivity_FieldSensitivity1() {
		args[0] = "C:/Users/hao/workspace/DroidBenchProj/FieldAndObjectSensitivity_FieldSensitivity1/app/app-release.apk";
		args[1] = "de.ecspride.FieldSensitivity1";
		args[2] = "onCreate";
		Main.main(args);
		assertEquals(true, Results.results.isEmpty());
	}
	
	@Test
	public void testFieldAndObjectSensitivity_FieldSensitivity2() {
		args[0] = "C:/Users/hao/workspace/DroidBenchProj/FieldAndObjectSensitivity_FieldSensitivity2/app/app-release.apk";
		args[1] = "de.ecspride.FieldSensitivity2";
		args[2] = "onCreate";
		Main.main(args);
		assertEquals(true, Results.results.isEmpty());
	}
	
	@Test
	public void testFieldAndObjectSensitivity_FieldSensitivity3() {
		args[0] = "C:/Users/hao/workspace/DroidBenchProj/FieldAndObjectSensitivity_FieldSensitivity3/app/app-release.apk";
		args[1] = "de.ecspride.FieldSensitivity3";
		args[2] = "onCreate";
		Main.main(args);
		Map<String, String> res = new HashMap<>();
		res.put("<android.telephony.SmsManager: void sendTextMessage(java.lang.String,java.lang.String,java.lang.String,android.app.PendingIntent,android.app.PendingIntent)>",
				"359874043116909");
		assertEquals(true, Results.results.contains(res));
	}
	
	@Test
	public void testFieldAndObjectSensitivity_FieldSensitivity4() {
		args[0] = "C:/Users/hao/workspace/DroidBenchProj/FieldAndObjectSensitivity_FieldSensitivity4/app/app-release.apk";
		args[1] = "de.ecspride.FieldSensitivity4";
		args[2] = "onCreate";
		Main.main(args);
		assertEquals(true, Results.results.isEmpty());
	}
	
	@Test
	public void testFieldAndObjectSensitivity_InheritedObjects1() {
		args[0] = "C:/Users/hao/workspace/DroidBenchProj/FieldAndObjectSensitivity_InheritedObjects1/app/app-release.apk";
		args[1] = "de.ecspride.InheritedObjects1";
		args[2] = "onCreate";
		Main.main(args);
		Map<String, String> res = new HashMap<>();
		res.put("<android.telephony.SmsManager: void sendTextMessage(java.lang.String,java.lang.String,java.lang.String,android.app.PendingIntent,android.app.PendingIntent)>",
				"359874043116909");
		assertEquals(true, Results.results.contains(res));
	}
	
	@Test
	public void testFieldAndObjectSensitivity_ObjectSensitivity1() {
		args[0] = "C:/Users/hao/workspace/DroidBenchProj/FieldAndObjectSensitivity_ObjectSensitivity1/app/app-release.apk";
		args[1] = "de.ecspride.ObjectSensitivity1";
		args[2] = "onCreate";
		Main.main(args);
		assertEquals(true, Results.results.isEmpty());
	}
	
	@Test
	public void testFieldAndObjectSensitivity_ObjectSensitivity2() {
		args[0] = "C:/Users/hao/workspace/DroidBenchProj/FieldAndObjectSensitivity_ObjectSensitivity2/app/app-release.apk";
		args[1] = "de.ecspride.OverwiteValue";
		args[2] = "onCreate";
		Main.main(args);
		assertEquals(true, Results.results.isEmpty());
	}
	
	//@Test
	public void testDroidKunfu() {
		args[0] = "C:/Users/hao/workspace/DroidBenchProj/apks/droidkungfu/00cf11a8b905e891a454e5b3fcae41f3ed405e3c5d0f9c1fce310de4a88c42d0.apk";
		args[1] = "ru.atools.sytrant.Sytrant";
		
		Main.main(args);
		Map<String, String> res = new HashMap<>();
		res.put("<android.telephony.SmsManager: void sendTextMessage(java.lang.String,java.lang.String,java.lang.String,android.app.PendingIntent,android.app.PendingIntent)>",
				"359874043116909");
		assertEquals(true, Results.results.contains(res));
	}

}
