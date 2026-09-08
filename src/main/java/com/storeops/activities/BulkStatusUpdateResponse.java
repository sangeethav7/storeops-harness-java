package com.storeops.activities;

import java.util.List;

public class BulkStatusUpdateResponse {

    private final List<String> succeeded;
    private final List<BulkFailureItem> failed;

    public BulkStatusUpdateResponse(List<String> succeeded, List<BulkFailureItem> failed) {
        this.succeeded = succeeded;
        this.failed = failed;
    }

    public List<String> getSucceeded() {
        return succeeded;
    }

    public List<BulkFailureItem> getFailed() {
        return failed;
    }
}
