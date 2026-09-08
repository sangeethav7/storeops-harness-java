package com.storeops.activities;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.storeops.common.exception.AppException;
import com.storeops.common.exception.ResourceNotFoundException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ActivityController.class)
class ActivityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ActivityService activityService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAllActivities_returns200() throws Exception {
        Activity a = new Activity();
        a.setId("act-1");
        a.setName("Restock");

        when(activityService.findAll()).thenReturn(List.of(a));

        mockMvc.perform(get("/api/activities"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value("act-1"));
    }

    @Test
    void createActivity_returns201() throws Exception {
        Activity request = new Activity();
        request.setName("Restock");
        request.setStoreId("store-1");

        Activity response = new Activity();
        response.setId("act-1");
        response.setName("Restock");
        response.setStoreId("store-1");
        response.setStatus("PENDING");

        when(activityService.create(any())).thenReturn(response);

        mockMvc.perform(post("/api/activities")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value("act-1"))
            .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void getActivity_returns200() throws Exception {
        Activity response = new Activity();
        response.setId("act-1");
        response.setName("Restock");
        response.setStatus("PENDING");

        when(activityService.findById("act-1")).thenReturn(response);

        mockMvc.perform(get("/api/activities/act-1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("act-1"));
    }

    @Test
    void getActivity_notFound_returns404WithErrorCode() throws Exception {
        when(activityService.findById("missing"))
            .thenThrow(new ResourceNotFoundException("Activity", "missing"));

        mockMvc.perform(get("/api/activities/missing"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.errorCode").value("RESOURCE_NOT_FOUND"));
    }

    @Test
    void deleteActivity_returns204() throws Exception {
        mockMvc.perform(delete("/api/activities/act-1"))
            .andExpect(status().isNoContent());
    }

    @Test
    void deleteActivity_notFound_returns404() throws Exception {
        doThrow(new ResourceNotFoundException("Activity", "missing"))
            .when(activityService).delete("missing");

        mockMvc.perform(delete("/api/activities/missing"))
            .andExpect(status().isNotFound());
    }

    // AC-1: all tasks updated successfully returns 207 with full succeeded list
    @Test
    void bulkUpdateStatus_allValid_returns207WithFullSucceededList() throws Exception {
        BulkStatusUpdateResponse response = new BulkStatusUpdateResponse(
            List.of("t1", "t2"), List.of());
        when(activityService.bulkUpdateStatus(any(BulkStatusUpdateRequest.class)))
            .thenReturn(response);

        mockMvc.perform(patch("/api/activities/bulk-status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"taskIds\":[\"t1\",\"t2\"],\"status\":\"DONE\"}"))
            .andExpect(status().is(207))
            .andExpect(jsonPath("$.succeeded[0]").value("t1"))
            .andExpect(jsonPath("$.succeeded[1]").value("t2"))
            .andExpect(jsonPath("$.failed").isArray())
            .andExpect(jsonPath("$.failed").isEmpty());
    }

    // AC-2: non-existent task ID appears in failed list with TASK_NOT_FOUND
    @Test
    void bulkUpdateStatus_nonExistentId_returns207WithTaskNotFoundFailure() throws Exception {
        BulkFailureItem failItem = new BulkFailureItem("ghost", "TASK_NOT_FOUND",
            "Activity not found: ghost");
        BulkStatusUpdateResponse response = new BulkStatusUpdateResponse(
            List.of("t1"), List.of(failItem));
        when(activityService.bulkUpdateStatus(any(BulkStatusUpdateRequest.class)))
            .thenReturn(response);

        mockMvc.perform(patch("/api/activities/bulk-status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"taskIds\":[\"t1\",\"ghost\"],\"status\":\"DONE\"}"))
            .andExpect(status().is(207))
            .andExpect(jsonPath("$.succeeded[0]").value("t1"))
            .andExpect(jsonPath("$.failed[0].taskId").value("ghost"))
            .andExpect(jsonPath("$.failed[0].errorCode").value("TASK_NOT_FOUND"));
    }

    // AC-3: invalid target status appears in failed list with INVALID_STATUS
    @Test
    void bulkUpdateStatus_invalidStatus_returns207WithInvalidStatusFailure() throws Exception {
        BulkFailureItem failItem = new BulkFailureItem("t1", "INVALID_STATUS",
            "Status must be DONE or BLOCKED, got: TODO");
        BulkStatusUpdateResponse response = new BulkStatusUpdateResponse(
            List.of(), List.of(failItem));
        when(activityService.bulkUpdateStatus(any(BulkStatusUpdateRequest.class)))
            .thenReturn(response);

        mockMvc.perform(patch("/api/activities/bulk-status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"taskIds\":[\"t1\"],\"status\":\"TODO\"}"))
            .andExpect(status().is(207))
            .andExpect(jsonPath("$.succeeded").isArray())
            .andExpect(jsonPath("$.succeeded").isEmpty())
            .andExpect(jsonPath("$.failed[0].taskId").value("t1"))
            .andExpect(jsonPath("$.failed[0].errorCode").value("INVALID_STATUS"));
    }

    // AC-4: empty taskIds list is rejected with HTTP 400 and EMPTY_REQUEST errorCode
    @Test
    void bulkUpdateStatus_emptyTaskIds_returns400WithEmptyRequestErrorCode() throws Exception {
        when(activityService.bulkUpdateStatus(any(BulkStatusUpdateRequest.class)))
            .thenThrow(new AppException("EMPTY_REQUEST", "Task ID list must not be empty",
                HttpStatus.BAD_REQUEST));

        mockMvc.perform(patch("/api/activities/bulk-status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"taskIds\":[],\"status\":\"DONE\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errorCode").value("EMPTY_REQUEST"))
            .andExpect(jsonPath("$.message").value("Task ID list must not be empty"));
    }
}
