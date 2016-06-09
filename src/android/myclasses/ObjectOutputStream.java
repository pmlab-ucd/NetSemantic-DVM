package android.myclasses;

import java.io.OutputStream;

public class ObjectOutputStream {
	
	Object writtenObj;
	MyByteArrayOutputStream output;
	
    public ObjectOutputStream(OutputStream output) {
    	if (output instanceof MyByteArrayOutputStream) {
    		this.output = (MyByteArrayOutputStream) output;
    	}
    }
	
	public ObjectOutputStream() {
	}
	
    public final void writeObject(Object object) {
    	writtenObj = object;
    	if (output != null) {
    		output.setWrittenObj(writtenObj);
    	}
    }
    
    public void close() {
    	
    }
    
    public final Object readObject() {
    	return writtenObj;
    }
}
