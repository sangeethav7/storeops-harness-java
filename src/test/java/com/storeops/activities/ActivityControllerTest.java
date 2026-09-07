package com.storeops.activities;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.storeops.common.exception.ResourceNotFoundException;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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
    void getActivity_notFound_returns404() throws Exception {
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
}
