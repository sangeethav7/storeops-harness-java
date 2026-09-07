package com.storeops.staff;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class StaffRepository {

    private final Map<String, Staff> store = new ConcurrentHashMap<>();

    public StaffRepository() {
        Staff admin = new Staff();
        admin.setId("staff-1");
        admin.setUsername("admin");
        admin.setPassword("admin123");
        admin.setRole("MANAGER");
        admin.setStoreId("store-1");
        store.put(admin.getId(), admin);

        Staff associate = new Staff();
        associate.setId("staff-2");
        associate.setUsername("jdoe");
        associate.setPassword("pass456");
        associate.setRole("ASSOCIATE");
        associate.setStoreId("store-1");
        store.put(associate.getId(), associate);
    }

    public Staff save(Staff staff) {
        store.put(staff.getId(), staff);
        return staff;
    }

    public Optional<Staff> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    public Optional<Staff> findByUsername(String username) {
        return store.values().stream()
            .filter(s -> s.getUsername().equals(username))
            .findFirst();
    }

    public List<Staff> findAll() {
        return new ArrayList<>(store.values());
    }
}
