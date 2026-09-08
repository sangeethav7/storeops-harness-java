package com.storeops.activities;

public class BulkFailureItem {

    private final String taskId;
    private final String errorCode;
    private final String message;

    public BulkFailureItem(String taskId, String errorCode, String message) {
        this.taskId = taskId;
        this.errorCode = errorCode;
        this.message = message;
    }

    public String getTaskId() {
        return taskId;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getMessage() {
        return message;
    }
}
