package com.vas.architectureandroidannotations.api;

import java.util.Date;

/**
 * Created by Vinicius Sauter liveData 16/10/2018.
 * .
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class TaskStatus<Value> {
    private Value value;
    private final String taskName;
    private final Date createdAt;
    private Date finishedAt;
    private State state;
    private double progress = 0.0;
    private Throwable error;
    private String message;
    private Callback<Value> callback;

    public TaskStatus(String taskName, Value value) {
        this.taskName = taskName;
        this.createdAt = new Date();
        this.value = value;
        this.setState(State.SUCCEEDED);
    }

    public TaskStatus(String taskName) {
        this.taskName = taskName;
        this.createdAt = new Date();
        this.setState(State.LOADING);
    }

    public void finish(Value value) {
        this.value = value;
        setFinishedAt(new Date());
        setState(State.SUCCEEDED);
    }

    public void finish(State state, String message) {
        this.message = message;
        setFinishedAt(new Date());
        setState(state);
    }

    public void finish(State state, Throwable error) {
        this.error = error;
        if (error != null)
            this.message = error.getMessage();
        setFinishedAt(new Date());
        setState(state);
    }

    public void finish(Throwable error) {
        this.error = error;
        if (error != null)
            this.message = error.getMessage();
        setFinishedAt(new Date());
        setState(State.ERROR);
    }

    public boolean isFinished() {
        return this.state.isFinished();
    }

    public boolean isSucceeded() {
        return this.state == State.SUCCEEDED;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public Date getFinishedAt() {
        return finishedAt;
    }

    public void setFinishedAt(Date finishedAt) {
        this.finishedAt = finishedAt;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
        if (this.callback != null) {
            this.callback.onStateChanged(this);
        }
        if (state == State.SUCCEEDED)
            error = null;
    }

    public double getProgress() {
        return progress;
    }

    public void setProgress(double progress) {
        this.progress = progress;
    }

    public Throwable getError() {
        return error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTaskName() {
        return taskName;
    }

    public Value getValue() {
        return value;
    }

    public void setValue(Value value) {
        this.value = value;
    }

    public void setCallback(Callback<Value> callback) {
        this.callback = callback;
        if (callback != null)
            callback.onStateChanged(this);
    }

    @Override
    public String toString() {
        return "TaskStatus{" +
                "taskName='" + taskName + '\'' +
                ", state=" + state +
                ", value=" + value +
                ", createdAt=" + createdAt +
                ", finishedAt=" + finishedAt +
                ", progress=" + progress +
                ", message='" + message + '\'' +
                ", error=" + error +
                '}';
    }
}
