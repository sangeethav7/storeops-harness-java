package com.storeops.activities;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.storeops.common.events.ShiftHandoverEvent;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class ActivityServiceTest {

    @Mock
    private ActivityRepository repository;

    @Mock
    private ApplicationEventPublisher publisher;

    @InjectMocks
    private ActivityService activityService;

    // AC-5: publishEvent called once per successfully updated task
    @Test
    void bulkUpdateStatus_allValidIds_publishesOneEventPerSuccess() {
        Activity t1 = new Activity();
        t1.setId("t1");
        t1.setStatus("IN_PROGRESS");

        Activity t2 = new Activity();
        t2.setId("t2");
        t2.setStatus("IN_PROGRESS");

        when(repository.findById("t1")).thenReturn(Optional.of(t1));
        when(repository.findById("t2")).thenReturn(Optional.of(t2));
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        BulkStatusUpdateRequest request = new BulkStatusUpdateRequest();
        request.setTaskIds(List.of("t1", "t2"));
        request.setStatus("DONE");

        BulkStatusUpdateResponse response = activityService.bulkUpdateStatus(request);

        assertThat(response.getSucceeded()).containsExactlyInAnyOrder("t1", "t2");
        assertThat(response.getFailed()).isEmpty();
        verify(publisher, times(2)).publishEvent(any(ShiftHandoverEvent.class));
    }

    // AC-6: failed tasks do not trigger events, only succeeded tasks do
    @Test
    void bulkUpdateStatus_withNonExistentId_publishesEventOnlyForSucceeded() {
        Activity t1 = new Activity();
        t1.setId("t1");
        t1.setStatus("IN_PROGRESS");

        when(repository.findById("t1")).thenReturn(Optional.of(t1));
        when(repository.findById("ghost")).thenReturn(Optional.empty());
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        BulkStatusUpdateRequest request = new BulkStatusUpdateRequest();
        request.setTaskIds(List.of("t1", "ghost"));
        request.setStatus("BLOCKED");

        BulkStatusUpdateResponse response = activityService.bulkUpdateStatus(request);

        assertThat(response.getSucceeded()).containsExactly("t1");
        assertThat(response.getFailed()).hasSize(1);
        assertThat(response.getFailed().get(0).getTaskId()).isEqualTo("ghost");
        assertThat(response.getFailed().get(0).getErrorCode()).isEqualTo("TASK_NOT_FOUND");
        verify(publisher, times(1)).publishEvent(any(ShiftHandoverEvent.class));
    }
}
