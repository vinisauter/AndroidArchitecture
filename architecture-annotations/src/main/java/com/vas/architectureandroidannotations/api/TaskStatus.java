package com.vas.architectureandroidannotations.api;

import java.util.Date;

/**
 * Created by Vinicius Sauter liveData 16/10/2018.
 * .
 */
public class TaskStatus {
    private final String taskName;
    private final Date createdAt;
    private Date finishedAt;
    private State state;
    private Throwable error;
    private String message;

    public TaskStatus(String taskName) {
        this.taskName = taskName;
        this.createdAt = new Date();
        this.setState(State.ENQUEUED);
    }

    public void finish(State state) {
        this.finishedAt = new Date();
        setState(state);
    }

    public void finish(State state, String message) {
        setState(state);
        this.finishedAt = new Date();
        this.message = message;
    }

    public void finish(State state, Throwable error) {
        setState(state);
        this.finishedAt = new Date();
        this.error = error;
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

}
