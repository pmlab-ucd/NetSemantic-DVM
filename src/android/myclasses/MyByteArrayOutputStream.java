package android.myclasses;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import fu.hao.trust.dvm.DVMObject;

public class MyByteArrayOutputStream extends OutputStream {
	Object writtenObj;
	ByteArrayOutputStream byteArrayOutputStream;

	@Override
	public void write(int b) throws IOException {
		byteArrayOutputStream.write(b);
	}
	
    public DVMObject toByteArray() {
    	return (DVMObject) writtenObj;
    }
    
    public void setWrittenObj(Object writtenObj) {
    	this.writtenObj = writtenObj;
    }
    
    public synchronized void write(byte[] buffer, int offset, int len) {
    	if (byteArrayOutputStream == null) {
    		byteArrayOutputStream = new ByteArrayOutputStream();
    	}
    	byteArrayOutputStream.write(buffer, offset, len);
    }
    
    @Override
    public String toString() {
    	if (byteArrayOutputStream != null) {
    		return byteArrayOutputStream.toString();
    	} else if (writtenObj != null) {
    		return writtenObj.toString();
    	}
    	
    	return super.toString();
    }
    
}
