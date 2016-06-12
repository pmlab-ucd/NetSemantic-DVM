package android.os;

import java.util.HashMap;
import java.util.Map;

import patdroid.core.ClassInfo;
import fu.hao.trust.dvm.DVMObject;

public class Parcel  {
	
	Map<ClassInfo, DVMObject> values;

	public Parcel() {
		values = new HashMap<>();
	}
	
    public static Parcel obtain() {
    	return new Parcel();
    }
    
    public final void writeValue(Object v) {
    	values.put(((DVMObject)v).getClazz(), (DVMObject) v);
    }
    
   // public final byte[] marshall() {
    //    throw new RuntimeException("Stub!");
   // }
    
    public final Map<ClassInfo, DVMObject> marshall() {
    	return values;
    }
    
    public final void unmarshall(byte[] data, int offest, int length) {
        throw new RuntimeException("Stub!");
    }
    
    public final void unmarshall(HashMap<ClassInfo, DVMObject> data, int offest, int length) {
    	values = data;
    }
    
    public final Object readValue(ClassLoader loader) {
        throw new RuntimeException("Stub!");
    }
    
    public final Object readValue(ClassInfo loader) {
    	return values.get(loader);
    }
    
    public final void setDataPosition(int pos) {
    }
}
