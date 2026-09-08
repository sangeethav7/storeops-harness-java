package com.storeops.alerts;

import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.storeops.common.events.ShiftHandoverEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ShiftHandoverEventListenerTest {

    @Mock
    private AlertService alertService;

    @InjectMocks
    private ShiftHandoverEventListener listener;

    // AC-1: one audit notification per event, severity LOW
    @Test
    void onShiftHandover_withValidEvent_createsOneAuditNotification() {
        ShiftHandoverEvent event = new ShiftHandoverEvent("t1", "DONE");

        listener.onShiftHandover(event);

        verify(alertService, times(1)).createAlert(contains("t1"), eq("LOW"));
    }

    // AC-2: notification message contains both task ID and new status
    @Test
    void onShiftHandover_messageContainsTaskIdAndNewStatus() {
        ShiftHandoverEvent event = new ShiftHandoverEvent("act-99", "BLOCKED");

        listener.onShiftHandover(event);

        verify(alertService, times(1)).createAlert(contains("act-99"), eq("LOW"));
        verify(alertService, times(1)).createAlert(contains("BLOCKED"), eq("LOW"));
    }
}
