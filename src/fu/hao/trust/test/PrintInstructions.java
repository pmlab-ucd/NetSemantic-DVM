package fu.hao.trust.test;

import java.io.IOException;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import fu.hao.trust.utils.Log;
import fu.hao.trust.utils.Settings;
import patdroid.core.ClassInfo;
import patdroid.core.MethodInfo;
import patdroid.dalvik.Instruction;
import patdroid.smali.SmaliClassDetailLoader;

public class PrintInstructions {
	public static void main(String[] args) {
		Settings.logLevel = 0;

		// pick an apk
		ZipFile apkFile;
		try {
			//apkFile = new ZipFile(new File("C:/Users/hao/workspace/PJApps/app/663e8eb52c7b4a14e2873b1551748587018661b3.apk"));
			apkFile = new ZipFile("C:/Users/hao/workspace/DroidBenchProj/GeneralJava_Exceptions2/app/Exceptions2.apk");
			// load all classes, methods, fields and instructions from an apk
			// we are using smali as the underlying engine
			new SmaliClassDetailLoader(apkFile, true).loadAll();
			// get the class representation for the MainActivity class in the
			// apk
			//ClassInfo c = ClassInfo.findClass("com.android.main.MainService");
			ClassInfo c = ClassInfo.findClass("de.ecspride.Exceptions2");
			MethodInfo[] ms = c.findMethodsHere("onCreate");
			// find all methods with the name "onCreate", most likely there is
			// only one
			MethodInfo m = c.getStaticInitializer();
			//MethodInfo[] ms = c.findMethodsHere("execTask");
			Log.bb("sss", "ms: " + ms.length);
			m = ms[0];
			// print all instructions
			int counter = 0;
			for (Instruction ins : m.insns) {
				System.out.println("[" + counter + "]" + ins.toString());
				counter++;
			}
		} catch (ZipException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
