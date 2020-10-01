package com.example.progettogio.db;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * View https://github.com/googlesamples/android-architecture-components/blob/master/BasicSample/app/src/main/java/com/example/android/persistence/AppExecutors.java
 */
public class AppExecutors {


    private final ExecutorService mDiskIO;
    private final ExecutorService mNetworkIO;

    /**
     * new instance of AppExecutors
     */
    public AppExecutors() {
        this(Executors.newFixedThreadPool(3), Executors.newFixedThreadPool(3));
    }

    /**
     * get the Disk IO
     * @return disk IO
     */
    public ExecutorService diskIO() {
        return mDiskIO;
    }

    /**
     * get the Network IO
     * @return network IO
     */
    public ExecutorService networkIO() {
        return mNetworkIO;
    }



    private AppExecutors(ExecutorService diskIO, ExecutorService networkIO) {
        this.mDiskIO = diskIO;
        this.mNetworkIO = networkIO;

    }


    private static class MainThreadExecutor implements Executor {
        private Handler mainThreadHandler = new Handler(Looper.getMainLooper());

        @Override
        public void execute(@NonNull Runnable command) {
            mainThreadHandler.post(command);
        }
    }
}
