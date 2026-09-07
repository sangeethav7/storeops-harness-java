package com.storeops.reports;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class ReportRepository {

    private final Map<String, Report> store = new ConcurrentHashMap<>();

    public Report save(Report report) {
        store.put(report.getId(), report);
        return report;
    }

    public List<Report> findAll() {
        return new ArrayList<>(store.values());
    }
}
