package com.storeops.reports;

import java.time.Instant;

public class Report {

    private String id;
    private String storeId;
    private String region;
    private int totalActivities;
    private int totalProgrammes;
    private Instant generatedAt;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public int getTotalActivities() {
        return totalActivities;
    }

    public void setTotalActivities(int totalActivities) {
        this.totalActivities = totalActivities;
    }

    public int getTotalProgrammes() {
        return totalProgrammes;
    }

    public void setTotalProgrammes(int totalProgrammes) {
        this.totalProgrammes = totalProgrammes;
    }

    public Instant getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(Instant generatedAt) {
        this.generatedAt = generatedAt;
    }
}
