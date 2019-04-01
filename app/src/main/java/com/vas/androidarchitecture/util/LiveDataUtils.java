package com.vas.androidarchitecture.util;

import com.vas.architectureandroidannotations.api.TaskResult;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.LiveDataReactiveStreams;
import io.reactivex.BackpressureStrategy;
import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

@SuppressWarnings("unused")
public class LiveDataUtils {
    public static <T> LiveData<TaskResult<T>> asLiveData(Observable<T> observable) {
        return LiveDataReactiveStreams.fromPublisher(observable
                .subscribeOn(Schedulers.io())
//              .observeOn(AndroidSchedulers.mainThread())
                .map(TaskResult::success)
                .onErrorReturn(TaskResult::error)
                .toFlowable(BackpressureStrategy.LATEST)
        );
    }

    public static <T> TaskLiveData<T> asTaskLiveData(Observable<T> observable) {
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
