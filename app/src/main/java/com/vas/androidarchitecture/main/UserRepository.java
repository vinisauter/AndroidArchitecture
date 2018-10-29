package com.vas.androidarchitecture.main;

import android.os.SystemClock;

import com.vas.androidarchitecture.model.User;
import com.vas.architectureandroidannotations.RepositoryARC;
import com.vas.architectureandroidannotations.api.Callback;
import com.vas.architectureandroidannotations.repository.Async;

/**
 * Created by Vinicius Sauter liveData 09/10/2018.
 * .
 */
@RepositoryARC
public class UserRepository {
    public void sendUserLastNameToServer(String userLastName, Callback<User> callback) {
        final User user = new User();
        user.setLastName(userLastName);
        callback.onFinished(user, null);
    }

    @Async
    public User getCurrentUser() throws Throwable {
        final User user = new User();
        user.setUserName("John");
        user.setLastName("Doe");
        SystemClock.sleep(5000);
//        int i = 9 / 0;
        return user;
    }

    @Async
    public User sendUserNameToServer(final String userName) throws Throwable {
        final User user = new User();
        user.setUserName(userName);
        SystemClock.sleep(5000);
//        int i = 9 / 0;
        return user;
    }

    @Async
    public User sendUserToServer(final String userName, final String lastName) throws Throwable {
        final User user = new User();
        user.setUserName(userName);
        user.setLastName(lastName);
        SystemClock.sleep(5000);
//        int i = 9 / 0;
        return user;
    }

    @Async(executor = Async.ExecutorType.THREAD_POOL, allowMultipleCalls = true)
    public void saveUser(final User user) throws Throwable {
        SystemClock.sleep(5000);
//        int i = 9 / 0;
    }

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
