package android.myclasses;

import java.io.IOException;
import java.io.InputStream;

import fu.hao.trust.dvm.DVMObject;

public class ByteArrayInputStream extends InputStream {
	private Object writtenObj;
	
	public ByteArrayInputStream(DVMObject obj) {
		writtenObj = obj;
	}
	
    public ByteArrayInputStream(byte[] buf) {
        throw new RuntimeException("Stub!");
    }

	@Override
	public int read() throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}
	
    public final Object readObject() {
    	return writtenObj;
    }
    
}
