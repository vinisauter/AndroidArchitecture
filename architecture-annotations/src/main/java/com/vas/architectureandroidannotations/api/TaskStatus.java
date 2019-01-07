package com.vas.architectureandroidannotations.api;

import java.util.Date;

/**
 * Created by Vinicius Sauter liveData 16/10/2018.
 * .
 */
public class TaskStatus<ResultObject> {
    private ResultObject resultObject;
    private final String taskName;
    private final Date createdAt;
    private Date finishedAt;
    private State state;
    private double progress = 0.0;
    private Throwable error;
    private String message;
    private Callback<ResultObject> callback;

    public TaskStatus(String taskName, ResultObject resultObject) {
        this(taskName);
        this.resultObject = resultObject;
    }

    public TaskStatus(String taskName) {
        this.taskName = taskName;
        this.createdAt = new Date();
        this.setState(State.ENQUEUED);
    }

    public void finish(ResultObject resultObject) {
        this.finishedAt = new Date();
        this.resultObject = resultObject;
        setState(State.SUCCEEDED);
    }

    public void finish(State state, String message) {
        this.finishedAt = new Date();
        this.message = message;
        setState(state);
    }

    public void finish(State state, Throwable error) {
        this.error = error;
        this.finishedAt = new Date();
        if (error != null)
            this.message = error.getLocalizedMessage();
        setState(state);
    }

    public boolean isFinished() {
        return this.state.isFinished();
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

    public ResultObject getResult() {
        return resultObject;
    }

    public void setResult(ResultObject resultObject) {
        this.resultObject = resultObject;
    }

    public void setCallback(Callback<ResultObject> callback) {
        this.callback = callback;
    }

    /**
     * The current state of a unit of work.
     */
    public enum State {

        /**
         * The state for work that is enqueued (hasn't completed and isn't running)
         */
        ENQUEUED,

        /**
         * The state for work that is currently being executed
         */
        RUNNING,

        /**
         * The state for work that has completed successfully
         */
        SUCCEEDED,

        /**
         * The state for work that has completed in a failure state
         */
        FAILED,

        /**
         * The state for work that is currently blocked because its prerequisites haven't finished
         * successfully
         */
        BLOCKED,

        /**
         * The state for work that has been cancelled and will not execute
         */
        CANCELLED;

        /**
         * Returns {@code true} if this State is considered finished.
         *
         * @return {@code true} for {@link #SUCCEEDED}, {@link #FAILED}, and {@link #CANCELLED} states
         */
        public boolean isFinished() {
            return (this == SUCCEEDED || this == FAILED || this == CANCELLED);
        }
    }

    @Override
    public String toString() {
        return "TaskStatus{" +
                "taskName='" + taskName + '\'' +
                ", state=" + state +
                ", resultObject=" + resultObject +
                ", createdAt=" + createdAt +
                ", finishedAt=" + finishedAt +
                ", progress=" + progress +
                ", message='" + message + '\'' +
                ", error=" + error +
                '}';
    }
}
