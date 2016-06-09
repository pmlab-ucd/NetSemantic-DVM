package android.myclasses;

import java.io.OutputStream;

public class ObjectOutputStream {
	
	Object writtenObj;
	ByteArrayOutputStream output;
	
    public ObjectOutputStream(OutputStream output) {
    	if (output instanceof ByteArrayOutputStream) {
    		this.output = (ByteArrayOutputStream) output;
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
