package fu.hao.trust.dvm;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.junit.Test;

import patdroid.core.ClassInfo;
import patdroid.core.MethodInfo;
import patdroid.core.ReflectionClassDetailLoader;
import patdroid.dalvik.Instruction;
import patdroid.smali.SmaliClassDetailLoader;
import fu.hao.trust.utils.Log;

public class InterpreterTest {

	String tag = "TEST DVM";

	//@Test
	public void testSwitch() {
		try {
			MethodInfo m = prepare("testSwitch");
		} catch (ZipException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	//@Test
	public void testCMP() {
		try {
			MethodInfo m = prepare("testCMP");
		} catch (ZipException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//@Test
	public void testIF() {
		try {
			MethodInfo m = prepare("testIF");
		} catch (ZipException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testArray() {
		try {
			MethodInfo m = prepare("testArray");
		} catch (ZipException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private MethodInfo prepare(String method) throws ZipException, IOException {
		// when a class is not loaded, load it with reflection
		ClassInfo.rootDetailLoader = new ReflectionClassDetailLoader();
		// pick an apk
		ZipFile apkFile;
		apkFile = new ZipFile(new File(
				"C:/Users/hao/workspace/TestDVM/app/app-release.apk"));
		// load all classes, methods, fields and instructions from an apk
		// we are using smali as the underlying engine
		new SmaliClassDetailLoader(apkFile, true).loadAll();
		// get the class representation for the MainActivity class in the
		// apk
		ClassInfo c = ClassInfo.findClass("fu.hao.testdvm.MainActivity");
		// find all methods with the name "onCreate", most likely there is
		// only one
		MethodInfo[] m = c.findMethodsHere(method);

		// print all instructions
		int counter = 0;
		for (Instruction ins : m[0].insns) {
			Log.msg(tag, "opcode: " + ins.opcode + " " + ins.opcode_aux);
			Log.msg(tag, "[" + counter + "]" + ins.toString());
		}

		return m[0];
	}

}
