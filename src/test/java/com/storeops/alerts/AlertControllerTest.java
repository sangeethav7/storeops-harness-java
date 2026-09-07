package com.storeops.alerts;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AlertController.class)
class AlertControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AlertService alertService;

    @Test
    void getAlerts_returnsEmptyList() throws Exception {
        when(alertService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/api/alerts"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getAlerts_returnsList() throws Exception {
        Alert alert = new Alert();
        alert.setId("alert-1");
        alert.setMessage("Test alert");
        alert.setSeverity("LOW");
        alert.setResolved(false);

        when(alertService.findAll()).thenReturn(List.of(alert));

        mockMvc.perform(get("/api/alerts"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value("alert-1"))
            .andExpect(jsonPath("$[0].severity").value("LOW"));
    }
}
