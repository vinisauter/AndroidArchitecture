package com.vas.androidarchitecture.main;

import com.vas.androidarchitecture.model.User;
import com.vas.architectureandroidannotations.ViewModelARC;
import com.vas.architectureandroidannotations.api.Callback;
import com.vas.architectureandroidannotations.api.TaskResult;
import com.vas.architectureandroidannotations.api.TaskStatus;
import com.vas.architectureandroidannotations.viewmodel.Repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import static com.vas.androidarchitecture.util.LiveDataUtils.asLiveData;

/**
 * Created by Vinicius Sauter liveData 09/10/2018.
 * .
 */
@SuppressWarnings("WeakerAccess")
@ViewModelARC
public class MainViewModel extends ViewModel {

    @Repository
    UserRepositoryARC repository;

    public final MutableLiveData<User> currentUser = new MutableLiveData<>();

    public LiveData<TaskStatus<User>> loadCurrentUser() {
        LiveData<TaskStatus<User>> taskStatusLiveData = repository.loadCurrentUserAsync();
        Transformations.switchMap(taskStatusLiveData, input -> {
            if (input.getValue() != null)
                currentUser.setValue(input.getValue());
            return currentUser;
        });
        return taskStatusLiveData;
    }

    public LiveData<TaskResult<User>> setCurrentUserName(String currentUserName) {
        return asLiveData(repository.sendUserNameToServerRx(currentUserName).cache());
    }

    public MutableLiveData<TaskStatus> setUserLastName(String currentUserLastName) {
        MutableLiveData<TaskStatus> statusUserTask = new MutableLiveData<>();
        repository.sendUserLastNameToServer(currentUserLastName, new Callback<User>() {
            @Override
            public void onStateChanged(TaskStatus<User> status) {
                statusUserTask.setValue(status);
                if (status.isFinished() && status.getError() == null)
                    currentUser.setValue(status.getValue());
            }
        });
        return statusUserTask;
    }

    public MutableLiveData<TaskStatus> setCurrentUser(User user) {
        MutableLiveData<TaskStatus> statusUserTask = new MutableLiveData<>();
        repository.saveUserAsync(user, new Callback<User>() {
            @Override
            public void onStateChanged(TaskStatus<User> status) {
                statusUserTask.setValue(status);
                if (status.isFinished() && status.getError() == null)
                    currentUser.setValue(status.getValue());
            }
        });
        return statusUserTask;
    }
}
