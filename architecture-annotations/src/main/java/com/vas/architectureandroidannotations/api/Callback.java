package com.vas.architectureandroidannotations.api;

public interface Callback<Result> {
    void onFinished(Result result, Throwable error);

    void onStateChanged(TaskStatus status);
}