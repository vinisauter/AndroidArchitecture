package com.vas.androidarchitecture.generic;

import android.os.SystemClock;

import com.vas.architectureandroidannotations.ArcRepository;
import com.vas.architectureandroidannotations.repository.Async;

/**
 * Created by Vinicius Sauter liveData 09/10/2018.
 * .
 */
@ArcRepository
public class ConfigRepository {
    @Async
    public void serial() throws Throwable {
        SystemClock.sleep(5000);
    }

    @Async(executor = Async.ExecutorType.SERIAL, allowMultipleCalls = true)
    public void serialMultiple() throws Throwable {
        SystemClock.sleep(5000);
    }

    @Async(executor = Async.ExecutorType.THREAD_POOL)
    public void threadPool() throws Throwable {
        SystemClock.sleep(5000);
    }

    @Async(executor = Async.ExecutorType.THREAD_POOL, allowMultipleCalls = true)
    public void threadPoolMultiple() throws Throwable {
        SystemClock.sleep(5000);
    }
}
