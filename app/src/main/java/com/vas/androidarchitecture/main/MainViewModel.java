package com.vas.androidarchitecture.main;

import com.vas.androidarchitecture.model.User;
import com.vas.architectureandroidannotations.ViewModelARC;
import com.vas.architectureandroidannotations.api.Callback;
import com.vas.architectureandroidannotations.api.TaskStatus;
import com.vas.architectureandroidannotations.viewmodel.Repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

/**
 * Created by Vinicius Sauter liveData 09/10/2018.
 * .
 */
@ViewModelARC
public class MainViewModel extends ViewModel {

    @Repository
    UserRepositoryARC repository;

    public final MutableLiveData<User> currentUser = new MutableLiveData<>();

    public LiveData<TaskStatus<User>> loadCurrentUser() {
        LiveData<TaskStatus<User>> taskStatusLiveData = repository.loadCurrentUserAsync();
        Transformations.switchMap(taskStatusLiveData, input -> {
            if (input.getResult() != null)
                currentUser.setValue(input.getResult());
            return currentUser;
        });
        return taskStatusLiveData;
    }

    public MutableLiveData<TaskStatus> setCurrentUserName(String currentUserName) {
        MutableLiveData<TaskStatus> statusUserTask = new MutableLiveData<>();
        repository.sendUserNameToServerAsync(currentUserName, new Callback<User>() {
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
        return statusUserTask;
    }

    public MutableLiveData<TaskStatus> setUserLastName(String currentUserLastName) {
        MutableLiveData<TaskStatus> statusUserTask = new MutableLiveData<>();
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
        return statusUserTask;
    }

    public MutableLiveData<TaskStatus> setCurrentUser(User user) {
        MutableLiveData<TaskStatus> statusUserTask = new MutableLiveData<>();
        repository.saveUserAsync(user, new Callback<User>() {
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
        return statusUserTask;
    }
}
