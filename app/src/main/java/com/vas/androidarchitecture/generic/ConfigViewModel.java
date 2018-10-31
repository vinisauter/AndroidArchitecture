package com.vas.androidarchitecture.generic;

import android.app.Application;

import com.vas.architectureandroidannotations.ViewModelARC;
import com.vas.architectureandroidannotations.api.Callback;
import com.vas.architectureandroidannotations.api.TaskStatus;
import com.vas.architectureandroidannotations.viewmodel.Repository;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

/**
 * Created by Vinicius Sauter liveData 09/10/2018.
 * .
 */
@ViewModelARC
public class ConfigViewModel extends AndroidViewModel {

    @Repository
    ConfigRepositoryARC repository;
    public final MutableLiveData<TaskStatus> statusTask = new MutableLiveData<>();

    public ConfigViewModel(Application application) {
        super(application);
    }

    public void serialAsync() {
        repository.serialAsync(new Callback<Void>() {
            @Override
            public void onFinished(Void aVoid, Throwable error) {

            }

            @Override
            public void onStateChanged(TaskStatus status) {
                statusTask.setValue(status);
            }
        });
    }

    public void serialMultipleAsync() {
        repository.serialMultipleAsync(new Callback<Void>() {
            @Override
            public void onFinished(Void aVoid, Throwable error) {

            }

            @Override
            public void onStateChanged(TaskStatus status) {
                statusTask.setValue(status);
            }
        });
    }

    public void threadPoolAsync() {
        repository.threadPoolAsync(new Callback<Void>() {
            @Override
            public void onFinished(Void aVoid, Throwable error) {

            }

            @Override
            public void onStateChanged(TaskStatus status) {
                statusTask.setValue(status);
            }
        });
    }

    public void threadPoolMultipleAsync() {
        repository.threadPoolMultipleAsync(new Callback<Void>() {
            @Override
            public void onFinished(Void aVoid, Throwable error) {
            }

            @Override
            public void onStateChanged(TaskStatus status) {
                statusTask.setValue(status);
            }
        });
    }
}
