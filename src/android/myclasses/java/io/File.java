package android.myclasses.java.io;

import fu.hao.trust.utils.Settings;

/**
 * @ClassName: File
 * @Description: Simulation of java.io.File
 * @author: Hao Fu
 * @date: Jun 22, 2016 5:11:40 PM
 */
public class File {
	String path = "";
	
	public File(String path) {
		this.path = path;
	}
	
	public boolean exists() {
		return Settings.getFileSystem().exists(path);
	}
	
	public void close() {
		
	}
}
