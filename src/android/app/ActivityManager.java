//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package android.app;

import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ConfigurationInfo;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import java.io.FileDescriptor;
import java.util.List;

public class ActivityManager {
    public static final String META_HOME_ALTERNATE = "android.app.home.alternate";
    public static final int MOVE_TASK_NO_USER_ACTION = 2;
    public static final int MOVE_TASK_WITH_HOME = 1;
    public static final int RECENT_IGNORE_UNAVAILABLE = 2;
    public static final int RECENT_WITH_EXCLUDED = 1;

    ActivityManager() {
        throw new RuntimeException("Stub!");
    }

    public int getMemoryClass() {
        throw new RuntimeException("Stub!");
    }

    public int getLargeMemoryClass() {
        throw new RuntimeException("Stub!");
    }

    public boolean isLowRamDevice() {
        throw new RuntimeException("Stub!");
    }

    /** @deprecated */
    @Deprecated
    public List<ActivityManager.RecentTaskInfo> getRecentTasks(int maxNum, int flags) throws SecurityException {
        throw new RuntimeException("Stub!");
    }

    public List<ActivityManager.AppTask> getAppTasks() {
        throw new RuntimeException("Stub!");
    }

    /** @deprecated */
    @Deprecated
    public List<ActivityManager.RunningTaskInfo> getRunningTasks(int maxNum) throws SecurityException {
        throw new RuntimeException("Stub!");
    }

    public void moveTaskToFront(int taskId, int flags) {
        throw new RuntimeException("Stub!");
    }

    public void moveTaskToFront(int taskId, int flags, Bundle options) {
        throw new RuntimeException("Stub!");
    }

    public List<ActivityManager.RunningServiceInfo> getRunningServices(int maxNum) throws SecurityException {
        throw new RuntimeException("Stub!");
    }

    public PendingIntent getRunningServiceControlPanel(ComponentName service) throws SecurityException {
        throw new RuntimeException("Stub!");
    }

    public void getMemoryInfo(ActivityManager.MemoryInfo outInfo) {
    }

    public boolean clearApplicationUserData() {
        throw new RuntimeException("Stub!");
    }

    public List<ActivityManager.ProcessErrorStateInfo> getProcessesInErrorState() {
        throw new RuntimeException("Stub!");
    }

    public List<ActivityManager.RunningAppProcessInfo> getRunningAppProcesses() {
        throw new RuntimeException("Stub!");
    }

    public static void getMyMemoryState(ActivityManager.RunningAppProcessInfo outState) {
        throw new RuntimeException("Stub!");
    }

    /** @deprecated */
    @Deprecated
    public void restartPackage(String packageName) {
        throw new RuntimeException("Stub!");
    }

    public void killBackgroundProcesses(String packageName) {
        throw new RuntimeException("Stub!");
    }

    public ConfigurationInfo getDeviceConfigurationInfo() {
        throw new RuntimeException("Stub!");
    }

    public int getLauncherLargeIconDensity() {
        throw new RuntimeException("Stub!");
    }

    public int getLauncherLargeIconSize() {
        throw new RuntimeException("Stub!");
    }

    public static boolean isUserAMonkey() {
        throw new RuntimeException("Stub!");
    }

    public static boolean isRunningInTestHarness() {
        throw new RuntimeException("Stub!");
    }

    public void dumpPackageState(FileDescriptor fd, String packageName) {
        throw new RuntimeException("Stub!");
    }

    public boolean isInLockTaskMode() {
        throw new RuntimeException("Stub!");
    }

    public static class AppTask {
        AppTask() {
            throw new RuntimeException("Stub!");
        }

        public void finishAndRemoveTask() {
            throw new RuntimeException("Stub!");
        }

        public ActivityManager.RecentTaskInfo getTaskInfo() {
            throw new RuntimeException("Stub!");
        }

        public void moveToFront() {
            throw new RuntimeException("Stub!");
        }

        public void startActivity(Context context, Intent intent, Bundle options) {
            throw new RuntimeException("Stub!");
        }

        public void setExcludeFromRecents(boolean exclude) {
            throw new RuntimeException("Stub!");
        }
    }

    public static class RunningAppProcessInfo implements Parcelable {
        public static final Creator<ActivityManager.RunningAppProcessInfo> CREATOR = null;
        public static final int IMPORTANCE_BACKGROUND = 400;
        public static final int IMPORTANCE_EMPTY = 500;
        public static final int IMPORTANCE_FOREGROUND = 100;
        public static final int IMPORTANCE_GONE = 1000;
        public static final int IMPORTANCE_PERCEPTIBLE = 130;
        public static final int IMPORTANCE_SERVICE = 300;
        public static final int IMPORTANCE_VISIBLE = 200;
        public static final int REASON_PROVIDER_IN_USE = 1;
        public static final int REASON_SERVICE_IN_USE = 2;
        public static final int REASON_UNKNOWN = 0;
        public int importance;
        public int importanceReasonCode;
        public ComponentName importanceReasonComponent;
        public int importanceReasonPid;
        public int lastTrimLevel;
        public int lru;
        public int pid;
        public String[] pkgList = null;
        public String processName;
        public int uid;

        public RunningAppProcessInfo() {
            throw new RuntimeException("Stub!");
        }

        public RunningAppProcessInfo(String pProcessName, int pPid, String[] pArr) {
            throw new RuntimeException("Stub!");
        }

        public int describeContents() {
            throw new RuntimeException("Stub!");
        }

        public void writeToParcel(Parcel dest, int flags) {
            throw new RuntimeException("Stub!");
        }

        public void readFromParcel(Parcel source) {
            throw new RuntimeException("Stub!");
        }
    }

    public static class ProcessErrorStateInfo implements Parcelable {
        public static final int CRASHED = 1;
        public static final Creator<ActivityManager.ProcessErrorStateInfo> CREATOR = null;
        public static final int NOT_RESPONDING = 2;
        public static final int NO_ERROR = 0;
        public int condition;
        public byte[] crashData = null;
        public String longMsg;
        public int pid;
        public String processName;
        public String shortMsg;
        public String stackTrace;
        public String tag;
        public int uid;

        public ProcessErrorStateInfo() {
            throw new RuntimeException("Stub!");
        }

        public int describeContents() {
            throw new RuntimeException("Stub!");
        }

        public void writeToParcel(Parcel dest, int flags) {
            throw new RuntimeException("Stub!");
        }

        public void readFromParcel(Parcel source) {
            throw new RuntimeException("Stub!");
        }
    }

    public static class MemoryInfo implements Parcelable {
        public static final Creator<ActivityManager.MemoryInfo> CREATOR = null;
        public long availMem;
        public boolean lowMemory;
        public long threshold;
        public long totalMem;

        public MemoryInfo() {
        	availMem = 100000;
        	totalMem = 200000;
        	lowMemory = false;
        	threshold = 5000;
        }

        public int describeContents() {
            throw new RuntimeException("Stub!");
        }

        public void writeToParcel(Parcel dest, int flags) {
            throw new RuntimeException("Stub!");
        }

        public void readFromParcel(Parcel source) {
            throw new RuntimeException("Stub!");
        }
    }

    public static class RunningServiceInfo implements Parcelable {
        public static final Creator<ActivityManager.RunningServiceInfo> CREATOR = null;
        public static final int FLAG_FOREGROUND = 2;
        public static final int FLAG_PERSISTENT_PROCESS = 8;
        public static final int FLAG_STARTED = 1;
        public static final int FLAG_SYSTEM_PROCESS = 4;
        public long activeSince;
        public int clientCount;
        public int clientLabel;
        public String clientPackage;
        public int crashCount;
        public int flags;
        public boolean foreground;
        public long lastActivityTime;
        public int pid;
        public String process;
        public long restarting;
        public ComponentName service;
        public boolean started;
        public int uid;

        public RunningServiceInfo() {
            throw new RuntimeException("Stub!");
        }

        public int describeContents() {
            throw new RuntimeException("Stub!");
        }

        public void writeToParcel(Parcel dest, int flags) {
            throw new RuntimeException("Stub!");
        }

        public void readFromParcel(Parcel source) {
            throw new RuntimeException("Stub!");
        }
    }

    public static class RunningTaskInfo implements Parcelable {
        public static final Creator<ActivityManager.RunningTaskInfo> CREATOR = null;
        public ComponentName baseActivity;
        public CharSequence description;
        public int id;
        public int numActivities;
        public int numRunning;
        public ComponentName topActivity;

        public RunningTaskInfo() {
            throw new RuntimeException("Stub!");
        }

        public int describeContents() {
            throw new RuntimeException("Stub!");
        }

        public void writeToParcel(Parcel dest, int flags) {
            throw new RuntimeException("Stub!");
        }

        public void readFromParcel(Parcel source) {
            throw new RuntimeException("Stub!");
        }
    }

    public static class RecentTaskInfo implements Parcelable {
        public static final Creator<ActivityManager.RecentTaskInfo> CREATOR = null;
        public int affiliatedTaskId;
        public Intent baseIntent;
        public CharSequence description;
        public int id;
        public ComponentName origActivity;
        public int persistentId;
        public ActivityManager.TaskDescription taskDescription;

        public RecentTaskInfo() {
            throw new RuntimeException("Stub!");
        }

        public int describeContents() {
            throw new RuntimeException("Stub!");
        }

        public void writeToParcel(Parcel dest, int flags) {
            throw new RuntimeException("Stub!");
        }

        public void readFromParcel(Parcel source) {
            throw new RuntimeException("Stub!");
        }
    }

    public static class TaskDescription implements Parcelable {
        public static final Creator<ActivityManager.TaskDescription> CREATOR = null;

        public TaskDescription(String label) {
            throw new RuntimeException("Stub!");
        }

        public TaskDescription() {
            throw new RuntimeException("Stub!");
        }

        public TaskDescription(ActivityManager.TaskDescription td) {
            throw new RuntimeException("Stub!");
        }

        public String getLabel() {
            throw new RuntimeException("Stub!");
        }

        public int getPrimaryColor() {
            throw new RuntimeException("Stub!");
        }

        public int describeContents() {
            throw new RuntimeException("Stub!");
        }

        public void writeToParcel(Parcel dest, int flags) {
            throw new RuntimeException("Stub!");
        }

        public void readFromParcel(Parcel source) {
            throw new RuntimeException("Stub!");
        }

        public String toString() {
            throw new RuntimeException("Stub!");
        }
    }
}
