package com.storeops.common.events;

public class ShiftHandoverEvent {

    private final String taskId;
    private final String newStatus;

    public ShiftHandoverEvent(String taskId, String newStatus) {
        this.taskId = taskId;
        this.newStatus = newStatus;
    }

    public String getTaskId() {
        return taskId;
    }

    public String getNewStatus() {
        return newStatus;
    }
}
