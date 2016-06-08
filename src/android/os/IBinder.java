//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package android.os;

//import android.os.IInterface;
import android.os.Parcel;
import java.io.FileDescriptor;

public interface IBinder {
    int FIRST_CALL_TRANSACTION = 1;
    int LAST_CALL_TRANSACTION = 16777215;
    int PING_TRANSACTION = 1599098439;
    int DUMP_TRANSACTION = 1598311760;
    int INTERFACE_TRANSACTION = 1598968902;
    int TWEET_TRANSACTION = 1599362900;
    int LIKE_TRANSACTION = 1598835019;
    int FLAG_ONEWAY = 1;

    String getInterfaceDescriptor() ;

    boolean pingBinder();

    boolean isBinderAlive();

    //IInterface queryLocalInterface(String var1);

    void dump(FileDescriptor var1, String[] var2) ;

    void dumpAsync(FileDescriptor var1, String[] var2) ;

    boolean transact(int var1, Parcel var2, Parcel var3, int var4) ;

    void linkToDeath(IBinder.DeathRecipient var1, int var2) ;

    boolean unlinkToDeath(IBinder.DeathRecipient var1, int var2);

    public interface DeathRecipient {
        void binderDied();
    }
}
