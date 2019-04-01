package com.vas.androidarchitecture.util;

import com.vas.architectureandroidannotations.api.TaskResult;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

@SuppressWarnings("unused")
public class LiveDataUtils {
    public static <T> TaskLiveData<T> asLiveData(Observable<T> observable) {
        return new TaskLiveData<T>() {
            Disposable disposable = observable
                    .subscribeOn(Schedulers.io())
                    .subscribe(
                            t -> postValue(new TaskResult<>(t, null)),
                            throwable -> postValue(new TaskResult<>(null, throwable))
                    );

            @Override
            protected void onInactive() {
                super.onInactive();
                if (disposable != null)
                    disposable.dispose();
            }
        };
    }
}
