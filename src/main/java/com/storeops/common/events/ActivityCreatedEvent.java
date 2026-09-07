package com.storeops.common.events;

public class ActivityCreatedEvent {

    private final String activityId;
    private final String storeId;

    public ActivityCreatedEvent(String activityId, String storeId) {
        this.activityId = activityId;
        this.storeId = storeId;
    }

    public String getActivityId() {
        return activityId;
    }

    public String getStoreId() {
        return storeId;
    }
}
