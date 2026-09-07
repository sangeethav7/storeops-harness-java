package com.storeops.activities;

import com.storeops.common.events.ActivityCreatedEvent;
import com.storeops.common.exception.ResourceNotFoundException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
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
}
