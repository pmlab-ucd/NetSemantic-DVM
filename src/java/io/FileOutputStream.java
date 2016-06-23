package java.io;

import fu.hao.trust.utils.Settings;
import patdroid.fs.EmulatedFS;

public class FileOutputStream {
	String path = "";
	
	static EmulatedFS fileSystem = Settings.getFileSystem();
	
	public FileOutputStream(String path) {
		this.path = path;
	}
	
}
