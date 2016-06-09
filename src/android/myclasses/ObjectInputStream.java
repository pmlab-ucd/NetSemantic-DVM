package android.myclasses;

import java.io.IOException;
import java.io.InputStream;

import fu.hao.trust.utils.Log;

public class ObjectInputStream extends InputStream {
	
	private ByteArrayInputStream input;
	private final String TAG = getClass().getSimpleName();
	private Object writtenObj;
	
    public ObjectInputStream(InputStream input) {
    	if (input instanceof ByteArrayInputStream) {
    		this.setInput((ByteArrayInputStream) input);
    		writtenObj = ((ByteArrayInputStream) input).readObject();
    	} else {
    		Log.err(TAG, "input is not a myClass.ByteArrayInputStream");
    	}
    }

	@Override
	public int read() throws IOException {
		// TODO Auto-generated method stub
		return 0;
	}
	
    public final Object readObject() {
    	return writtenObj;
    }
    
    public void close() {
    	
    }

	public ByteArrayInputStream getInput() {
		return input;
	}

	public void setInput(ByteArrayInputStream input) {
		this.input = input;
	}

}
