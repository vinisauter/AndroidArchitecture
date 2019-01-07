package com.vas.architectureandroidannotations.api;

public abstract class ResultCallback<Result> implements Callback<Result> {
    public void onStateChanged(TaskStatus<Result> status) {
        if (status.isFinished()) {
            onFinished(status, status.getResult(), status.getError());
        }
    }

    abstract void onFinished(TaskStatus<Result> status, Result result, Throwable error);
}