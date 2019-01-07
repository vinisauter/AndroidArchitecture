package com.vas.architectureandroidannotations.api;

public interface Callback<Result> {
    void onStateChanged(TaskStatus<Result> status);
}