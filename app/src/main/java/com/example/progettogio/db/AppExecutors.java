package com.example.progettogio.db;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * View https://github.com/googlesamples/android-architecture-components/blob/master/BasicSample/app/src/main/java/com/example/android/persistence/AppExecutors.java
 */
public class AppExecutors {


    private final Executor mDiskIO;
    private final Executor mNetworkIO;
    private final Executor mMainThread;

    /**
     * new instance of AppExecutors
     */
    public AppExecutors() {
        this(Executors.newSingleThreadExecutor(), Executors.newFixedThreadPool(3),
                new MainThreadExecutor());
    }

    /**
     * get the Disk IO
     * @return disk IO
     */
    public Executor diskIO() {
        return mDiskIO;
    }

    /**
     * get the Network IO
     * @return network IO
     */
    public Executor networkIO() {
        return mNetworkIO;
    }

    /**
     * get the main thread
     * @return main thread
     */
    public Executor mainThread() {
        return mMainThread;
    }


    private AppExecutors(Executor diskIO, Executor networkIO, Executor mainThread) {
        this.mDiskIO = diskIO;
        this.mNetworkIO = networkIO;
        this.mMainThread = mainThread;
    }


    private static class MainThreadExecutor implements Executor {
        private Handler mainThreadHandler = new Handler(Looper.getMainLooper());

        @Override
        public void execute(@NonNull Runnable command) {
            mainThreadHandler.post(command);
        }
    }
}
