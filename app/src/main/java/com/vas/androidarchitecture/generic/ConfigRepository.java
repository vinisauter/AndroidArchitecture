package com.vas.androidarchitecture.generic;

import android.os.SystemClock;

import com.vas.architectureandroidannotations.RepositoryARC;
import com.vas.architectureandroidannotations.repository.Async;
import com.vas.architectureandroidannotations.repository.AsyncType;
import com.vas.architectureandroidannotations.repository.ExecutorType;

/**
 * Created by Vinicius Sauter liveData 09/10/2018.
 * .
 */
@RepositoryARC
public class ConfigRepository {
    @Async(AsyncType.ASYNC_TASK)
    public void serial() throws Throwable {
        SystemClock.sleep(5000);
    }

    @Async(value = AsyncType.ASYNC_TASK, executor = ExecutorType.SERIAL, allowMultipleCalls = true)
    public void serialMultiple() throws Throwable {
        SystemClock.sleep(5000);
    }

    @Async(value = AsyncType.ASYNC_TASK, executor = ExecutorType.THREAD_POOL)
    public void threadPool() throws Throwable {
        SystemClock.sleep(5000);
    }

    @Async(value = AsyncType.ASYNC_TASK, executor = ExecutorType.THREAD_POOL, allowMultipleCalls = true)
    public void threadPoolMultiple() throws Throwable {
        SystemClock.sleep(5000);
    }
}
