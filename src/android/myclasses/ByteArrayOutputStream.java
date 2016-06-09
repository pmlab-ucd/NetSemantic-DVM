package android.myclasses;

import java.io.IOException;
import java.io.OutputStream;

import fu.hao.trust.dvm.DVMObject;

public class ByteArrayOutputStream extends OutputStream {
	Object writtenObj;

	@Override
	public void write(int b) throws IOException {
		// TODO Auto-generated method stub
		
	}
	
    public DVMObject toByteArray() {
    	return (DVMObject) writtenObj;
    }
    
    public void setWrittenObj(Object writtenObj) {
    	this.writtenObj = writtenObj;
    }

}
