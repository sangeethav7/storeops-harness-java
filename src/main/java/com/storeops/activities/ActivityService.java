package com.storeops.activities;

import com.storeops.common.events.ActivityCreatedEvent;
import com.storeops.common.events.ShiftHandoverEvent;
import com.storeops.common.exception.AppException;
import com.storeops.common.exception.ResourceNotFoundException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class ActivityService {

    private final ActivityRepository repository;
    private final ApplicationEventPublisher publisher;

    public ActivityService(ActivityRepository repository, ApplicationEventPublisher publisher) {
        this.repository = repository;
        this.publisher = publisher;
    }

    public Activity create(Activity activity) {
        activity.setId(UUID.randomUUID().toString());
        activity.setCreatedAt(Instant.now());
        activity.setStatus("PENDING");
        Activity saved = repository.save(activity);
        publisher.publishEvent(new ActivityCreatedEvent(saved.getId(), saved.getStoreId()));
        return saved;
    }

    public Activity findById(String id) {
        return repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Activity", id));
    }

    public List<Activity> findAll() {
        return repository.findAll();
    }

    public void delete(String id) {
        if (!repository.existsById(id)) {
            throw new ResourceNotFoundException("Activity", id);
        }
        repository.deleteById(id);
    }

    public BulkStatusUpdateResponse bulkUpdateStatus(BulkStatusUpdateRequest request) {
        if (request.getTaskIds() == null || request.getTaskIds().isEmpty()) {
            throw new AppException("EMPTY_REQUEST", "Task ID list must not be empty",
                HttpStatus.BAD_REQUEST);
        }
        List<String> succeeded = new ArrayList<>();
        List<BulkFailureItem> failed = new ArrayList<>();
        for (String taskId : request.getTaskIds()) {
            String newStatus = request.getStatus();
            if (!"DONE".equals(newStatus) && !"BLOCKED".equals(newStatus)) {
                failed.add(new BulkFailureItem(taskId, "INVALID_STATUS",
                    "Status must be DONE or BLOCKED, got: " + newStatus));
                continue;
            }
            Optional<Activity> opt = repository.findById(taskId);
            if (opt.isEmpty()) {
                failed.add(new BulkFailureItem(taskId, "TASK_NOT_FOUND",
                    "Activity not found: " + taskId));
                continue;
            }
            Activity activity = opt.get();
            activity.setStatus(newStatus);
            repository.save(activity);
            publisher.publishEvent(new ShiftHandoverEvent(taskId, newStatus));
            succeeded.add(taskId);
        }
        return new BulkStatusUpdateResponse(succeeded, failed);
    }
}
