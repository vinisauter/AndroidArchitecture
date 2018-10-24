package com.vas.androidarchitecture.api;

import android.os.AsyncTask;
import android.util.Pair;

import com.vas.architectureandroidannotations.api.Callback;
import com.vas.architectureandroidannotations.api.TaskStatus;

/**
 * Created by Vinicius Sauter liveData 16/10/2018.
 * .
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public abstract class ApiAsyncTask<Param, Result> extends AsyncTask<Param, TaskStatus, Pair<Throwable, Result>> {

    private final Callback<Result> callback;

    final TaskStatus taskStatus = new TaskStatus(this.getClass().getSimpleName());

    public ApiAsyncTask(String taskName) {
        this.callback = null;
        publishState(taskStatus);
    }

    public ApiAsyncTask(String taskName, Callback<Result> callback) {
        this.callback = callback;
        publishState(taskStatus);
    }

    protected final void publishState(TaskStatus taskStatus) {
        publishProgress(taskStatus);
    }

    @Override
    protected void onPreExecute() {
//        taskStatus.setState(TaskStatus.State.RUNNING);
//        publishState(taskStatus);
    }

    @Override
    protected void onPostExecute(Pair<Throwable, Result> result) {
        onPostExecute(result.second, result.first);
        if (result.first == null)
            taskStatus.finish(TaskStatus.State.SUCCEEDED);
        else {
            taskStatus.finish(TaskStatus.State.FAILED, result.first);
        }
        taskStatus.finish(result.first == null ? TaskStatus.State.SUCCEEDED : TaskStatus.State.FAILED, result.first);
        publishState(taskStatus);
    }

    @Override
    protected void onCancelled() {
        taskStatus.setState(TaskStatus.State.CANCELLED);
        publishState(taskStatus);
    }

    @Override
    protected void onProgressUpdate(TaskStatus... values) {
        onStateUpdate(values[0]);
    }

    @Override
    protected Pair<Throwable, Result> doInBackground(Param[] params) {
        taskStatus.setState(TaskStatus.State.RUNNING);
        publishState(taskStatus);
        try {
            Result result = doInBackground(params[0]);
            return new Pair<>(null, result);
        } catch (Throwable t) {
            return new Pair<>(t, null);
        }
    }

    protected abstract Result doInBackground(Param param) throws Throwable;

    protected void onStateUpdate(TaskStatus state) {
        if (callback != null) callback.onStateChanged(state);
    }

    protected void onPostExecute(Result result, Throwable error) {
        if (callback != null)
            callback.onFinished(result, error);
    }

}