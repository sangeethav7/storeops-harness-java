package com.storeops.activities;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class ActivityRepository {

    private final Map<String, Activity> store = new ConcurrentHashMap<>();

    public Activity save(Activity activity) {
        store.put(activity.getId(), activity);
        return activity;
    }

    public Optional<Activity> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    public boolean existsById(String id) {
        return store.containsKey(id);
    }

    public void deleteById(String id) {
        store.remove(id);
    }

    public List<Activity> findAll() {
        return new ArrayList<>(store.values());
    }
}
