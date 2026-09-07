package com.storeops.alerts;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class AlertRepository {

    private final Map<String, Alert> store = new ConcurrentHashMap<>();

    public Alert save(Alert alert) {
        store.put(alert.getId(), alert);
        return alert;
    }

    public List<Alert> findAll() {
        return new ArrayList<>(store.values());
    }
}
