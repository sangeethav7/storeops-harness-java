package com.storeops.alerts;

import com.storeops.common.events.ShiftHandoverEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ShiftHandoverEventListener {

    private final AlertService alertService;

    public ShiftHandoverEventListener(AlertService alertService) {
        this.alertService = alertService;
    }

    @EventListener
    public void onShiftHandover(ShiftHandoverEvent event) {
        alertService.createAlert(
            "Shift handover: task " + event.getTaskId() + " marked " + event.getNewStatus(),
            "LOW"
        );
    }
}
