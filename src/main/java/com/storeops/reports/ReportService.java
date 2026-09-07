package com.storeops.reports;

import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class ReportService {

    private final ReportRepository repository;

    public ReportService(ReportRepository repository) {
        this.repository = repository;
    }

    public Report generate(String storeId, String region) {
        Report report = new Report();
        report.setId(UUID.randomUUID().toString());
        report.setStoreId(storeId != null ? storeId : "all");
        report.setRegion(region != null ? region : "all");
        report.setTotalActivities(0);
        report.setTotalProgrammes(0);
        report.setGeneratedAt(Instant.now());
        return repository.save(report);
    }
}
