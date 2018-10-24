package com.vas.androidarchitecture.main;


import android.os.AsyncTask;
import android.util.SparseArray;

import com.vas.androidarchitecture.model.User;
import com.vas.architectureandroidannotations.ArcViewModel;
import com.vas.architectureandroidannotations.api.Callback;
import com.vas.architectureandroidannotations.api.TaskStatus;

import java.util.HashMap;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * Created by Vinicius Sauter liveData 09/10/2018.
 * .
 */
@ArcViewModel
public class MainViewModel extends ViewModel {

    //TODO: @Repository
    private UserRepository_ repository = new UserRepository_();

    public final MutableLiveData<User> currentUser = new MutableLiveData<>();
    public final MutableLiveData<TaskStatus> statusUserTask = new MutableLiveData<>();

    public void setCurrentUserName(String currentUserName) {
        UserRepository_.SendUserNameToServerTask task = repository.sendUserNameToServerAsync(currentUserName, new Callback<User>() {
            @Override
            public void onFinished(User user, Throwable error) {
                if (error == null)
                    currentUser.setValue(user);
            }

            @Override
            public void onStateChanged(TaskStatus status) {
                statusUserTask.setValue(status);
            }
        });

        SparseArray<AsyncTask> taskHashMap = new SparseArray<>();
        HashMap<String, AsyncTask> map = new HashMap<>();
        taskHashMap.append(task.hashCode(), task);
    }

    public void setUserLastName(String currentUserLastName) {
        repository.sendUserLastNameToServer(currentUserLastName, new Callback<User>() {
            @Override
            public void onFinished(User user, Throwable error) {
                if (error == null)
                    currentUser.setValue(user);
            }

            @Override
            public void onStateChanged(TaskStatus status) {
                statusUserTask.setValue(status);
            }
        });
    }

    public void setCurrentUser(User user) {
        repository.saveUserAsync(user, new Callback<Void>() {
            @Override
            public void onFinished(Void aVoid, Throwable error) {

            }

            @Override
            public void onStateChanged(TaskStatus status) {
                statusUserTask.setValue(status);
            }
        });
    }
}
