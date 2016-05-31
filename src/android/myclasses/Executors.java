package android.myclasses;

public class Executors {
	
	static ExecutorService service = new ExecutorService();
	
    public static ExecutorService newCachedThreadPool() {
        return service;
    }
}
