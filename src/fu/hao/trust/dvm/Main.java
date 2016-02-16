package fu.hao.trust.dvm;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipFile;

import fu.hao.trust.utils.Log;
import patdroid.core.ClassInfo;
import patdroid.core.MethodInfo;
import patdroid.core.ReflectionClassDetailLoader;
import patdroid.dalvik.Instruction;
import patdroid.smali.SmaliClassDetailLoader;


/**
 * @ClassName: Main
 * @Description: TODO
 * @author: hao
 * @date: Feb 15, 2016 12:08:30 AM
 */
public class Main {
	private final static String tag = Main.class.toString();
	
	public static void main(String[] args) {
		// when a class is not loaded, load it with reflection
		ClassInfo.rootDetailLoader = new ReflectionClassDetailLoader();
		// pick an apk
		ZipFile apkFile;
		try {
			apkFile = new ZipFile(new File("C:/Users/hao/workspace/Button1/app/app-debug.apk"));
			// load all classes, methods, fields and instructions from an apk
			// we are using smali as the underlying engine
			new SmaliClassDetailLoader(apkFile, true).loadAll();
			// get the class representation for the MainActivity class in the
			// apk
			ClassInfo c = ClassInfo.findClass("de.ecspride.Activity1");
			// find all methods with the name "onCreate", most likely there is
			// only one
			MethodInfo[] m = c.findMethodsHere("getNaturalEvnCtxs");
			Log.msg(tag,"Start");
			// print all instructions
			int counter = 0;
			for (Instruction ins : m[0].insns) {
				Log.msg(tag, "opcode: " + ins.opcode + " " + ins.opcode_aux);
				Log.msg(tag, "[" + counter + "]" + ins.toString());
			}
			
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
