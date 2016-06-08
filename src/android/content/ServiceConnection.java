package android.content;

import android.content.ComponentName;
import android.os.IBinder;

public interface ServiceConnection {
    void onServiceConnected(ComponentName var1, IBinder var2);

    void onServiceDisconnected(ComponentName var1);
}
