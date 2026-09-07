package com.storeops.alerts;

import com.storeops.common.events.ActivityCreatedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ActivityCreatedListener {

    private final AlertService alertService;

    public ActivityCreatedListener(AlertService alertService) {
        this.alertService = alertService;
    }

    @EventListener
    public void onActivityCreated(ActivityCreatedEvent event) {
        alertService.createAlert(
            "New activity created: " + event.getActivityId()
                + " for store: " + event.getStoreId(),
            "LOW"
        );
    }
}
