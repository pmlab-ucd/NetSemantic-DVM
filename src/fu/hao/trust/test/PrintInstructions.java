package fu.hao.trust.test;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import patdroid.core.ClassInfo;
import patdroid.core.MethodInfo;
import patdroid.dalvik.Instruction;
import patdroid.smali.SmaliClassDetailLoader;

public class PrintInstructions {
	public static void main(String[] args) {

		// pick an apk
		ZipFile apkFile;
		try {
			apkFile = new ZipFile(new File("C:/Users/hao/workspace/PJApps/app/PJApps.apk"));

			// load all classes, methods, fields and instructions from an apk
			// we are using smali as the underlying engine
			new SmaliClassDetailLoader(apkFile, true).loadAll();
			// get the class representation for the MainActivity class in the
			// apk
			ClassInfo c = ClassInfo.findClass("fu.hao.pjapps.MainActivity");
			// find all methods with the name "onCreate", most likely there is
			// only one
			MethodInfo[] m = c.findMethodsHere("testConnection");
			// print all instructions
			int counter = 0;
			for (Instruction ins : m[0].insns) {
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
