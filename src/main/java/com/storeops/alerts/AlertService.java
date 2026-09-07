package com.storeops.alerts;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class AlertService {

    private final AlertRepository repository;

    public AlertService(AlertRepository repository) {
        this.repository = repository;
    }

    public List<Alert> findAll() {
        return repository.findAll();
    }

    public Alert createAlert(String message, String severity) {
        Alert alert = new Alert();
        alert.setId(UUID.randomUUID().toString());
        alert.setMessage(message);
        alert.setSeverity(severity);
        alert.setCreatedAt(Instant.now());
        alert.setResolved(false);
        return repository.save(alert);
    }
}
