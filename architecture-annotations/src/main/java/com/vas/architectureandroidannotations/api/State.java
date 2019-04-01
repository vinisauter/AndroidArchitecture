package com.vas.architectureandroidannotations.api;

/**
 * The current state of a unit of work.
 */
public enum State {
    LOADING,
    SUCCEEDED,
    ERROR;

    public boolean isFinished() {
        return (this == SUCCEEDED || this == ERROR);
    }

}
