package com.storeops.reports;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService service;

    public ReportController(ReportService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<Report> generate(
            @RequestParam(required = false) String storeId,
            @RequestParam(required = false) String region) {
        return ResponseEntity.ok(service.generate(storeId, region));
    }
}
