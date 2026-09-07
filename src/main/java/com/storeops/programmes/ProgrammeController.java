package com.storeops.programmes;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/programmes")
public class ProgrammeController {

    private final ProgrammeService service;

    public ProgrammeController(ProgrammeService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Programme> create(@RequestBody Programme programme) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(programme));
    }

    @GetMapping
    public ResponseEntity<List<Programme>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Programme> getById(@PathVariable String id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Programme> update(@PathVariable String id,
            @RequestBody Programme updates) {
        return ResponseEntity.ok(service.update(id, updates));
    }
}
