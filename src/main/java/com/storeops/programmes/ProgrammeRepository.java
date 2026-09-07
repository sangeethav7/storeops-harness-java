package com.storeops.programmes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class ProgrammeRepository {

    private final Map<String, Programme> store = new ConcurrentHashMap<>();

    public Programme save(Programme programme) {
        store.put(programme.getId(), programme);
        return programme;
    }

    public Optional<Programme> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    public boolean existsById(String id) {
        return store.containsKey(id);
    }

    public List<Programme> findAll() {
        return new ArrayList<>(store.values());
    }
}
