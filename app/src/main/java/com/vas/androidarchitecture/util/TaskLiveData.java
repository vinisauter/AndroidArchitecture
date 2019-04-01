package com.vas.androidarchitecture.util;

import com.vas.architectureandroidannotations.api.State;
import com.vas.architectureandroidannotations.api.TaskResult;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.Transformations;

@SuppressWarnings({"unused", "WeakerAccess"})
public class TaskLiveData<T> extends LiveData<TaskResult<T>> {

    public static <T> TaskLiveData<T> error(Throwable throwable) {
        return new TaskLiveData<>(new TaskResult<>(null, throwable));
    }

    public static <T> TaskLiveData<T> success(T t) {
        return new TaskLiveData<>(new TaskResult<>(t, null));
    }

    public void setError(Throwable throwable) {
        postValue(new TaskResult<>(null, throwable));
    }

    public void setSuccess(T t) {
        postValue(new TaskResult<>(t, null));
    }

    public TaskLiveData(TaskResult<T> pair) {
        postValue(pair);
    }

    public TaskLiveData() {
    }

    public LiveData<State> state() {
        final MutableLiveData<State> statusLiveData = new MutableLiveData<>();
        statusLiveData.postValue(State.LOADING);
        return Transformations.switchMap(this, input -> {
            if (input != null) {
                if (input.error != null) {
                    statusLiveData.postValue(State.ERROR);
                } else {
                    statusLiveData.postValue(State.SUCCEEDED);
                }
            }
            return statusLiveData;
        });
    }

    public TaskLiveData<T> observeState(@NonNull LifecycleOwner owner, @NonNull Observer<State> observer) {
        state().observe(owner, observer);
        return this;
    }

    public TaskLiveData<T> observeForeverState(@NonNull Observer<State> observer) {
        state().observeForever(observer);
        return this;
    }

    public LiveData<T> value() {
        final MutableLiveData<T> tLiveData = new MutableLiveData<>();
        return Transformations.switchMap(this, input -> {
            if (input != null && input.value != null) {
                tLiveData.postValue(input.value);
            }
            return tLiveData;
        });
    }

    public TaskLiveData<T> observeValue(@NonNull LifecycleOwner owner, @NonNull Observer<T> observer) {
        value().observe(owner, observer);
        return this;
    }

    public TaskLiveData<T> observeForeverValue(@NonNull Observer<T> observer) {
        value().observeForever(observer);
        return this;
    }

    public LiveData<Throwable> error() {
        final MutableLiveData<Throwable> throwableLiveData = new MutableLiveData<>();
        return Transformations.switchMap(this, input -> {
            if (input != null && input.error != null) {
                throwableLiveData.postValue(input.error);
            }
            return throwableLiveData;
        });
    }

    public TaskLiveData<T> observeError(@NonNull LifecycleOwner owner, @NonNull Observer<Throwable> observer) {
        error().observe(owner, observer);
        return this;
    }

    public TaskLiveData<T> observeForeverError(@NonNull Observer<Throwable> observer) {
        error().observeForever(observer);
        return this;
    }

    @Override
    protected void onActive() {
        super.onActive();
    }

    @Override
    protected void onInactive() {
        super.onInactive();
    }
}