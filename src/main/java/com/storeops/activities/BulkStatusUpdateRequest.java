package com.storeops.activities;

import java.util.List;

public class BulkStatusUpdateRequest {

    private List<String> taskIds;
    private String status;

    public List<String> getTaskIds() {
        return taskIds;
    }

    public void setTaskIds(List<String> taskIds) {
        this.taskIds = taskIds;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
