package com.vas.androidarchitecture.generic;

import com.vas.architectureandroidannotations.ArcViewModel;
import com.vas.architectureandroidannotations.api.Callback;
import com.vas.architectureandroidannotations.api.TaskStatus;
import com.vas.architectureandroidannotations.viewmodel.Repository;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * Created by Vinicius Sauter liveData 09/10/2018.
 * .
 */
@ArcViewModel
public class ConfigViewModel extends ViewModel {

    @Repository
    ConfigRepositoryARC repository;
    public final MutableLiveData<TaskStatus> statusTask = new MutableLiveData<>();

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
