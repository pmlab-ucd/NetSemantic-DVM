package android.os;

import java.io.FileDescriptor;

public class IBinderInstance implements IBinder {
	
	Handler handler;
	
	public IBinderInstance(Handler handler) {
		this.handler = handler;
	}
	
	public Handler getHandler() {
		return handler;
	}

	@Override
	public String getInterfaceDescriptor() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean pingBinder() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isBinderAlive() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void dump(FileDescriptor var1, String[] var2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void dumpAsync(FileDescriptor var1, String[] var2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean transact(int var1, Parcel var2, Parcel var3, int var4) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void linkToDeath(DeathRecipient var1, int var2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean unlinkToDeath(DeathRecipient var1, int var2) {
		// TODO Auto-generated method stub
		return false;
	}

}
