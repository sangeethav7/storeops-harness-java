package com.storeops.programmes;

import com.storeops.common.exception.ResourceNotFoundException;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ProgrammeService {

    private final ProgrammeRepository repository;

    public ProgrammeService(ProgrammeRepository repository) {
        this.repository = repository;
    }

    public Programme create(Programme programme) {
        programme.setId(UUID.randomUUID().toString());
        programme.setActive(true);
        return repository.save(programme);
    }

    public Programme findById(String id) {
        return repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Programme", id));
    }

    public List<Programme> findAll() {
        return repository.findAll();
    }

    public Programme update(String id, Programme updates) {
        Programme existing = repository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Programme", id));
        if (updates.getName() != null) {
            existing.setName(updates.getName());
        }
        if (updates.getDescription() != null) {
            existing.setDescription(updates.getDescription());
        }
        if (updates.getStartDate() != null) {
            existing.setStartDate(updates.getStartDate());
        }
        if (updates.getEndDate() != null) {
            existing.setEndDate(updates.getEndDate());
        }
        existing.setActive(updates.isActive());
        return repository.save(existing);
    }
}
