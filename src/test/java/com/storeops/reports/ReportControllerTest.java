package com.storeops.reports;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ReportController.class)
class ReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReportService reportService;

    @Test
    void generateReport_returns200() throws Exception {
        Report report = new Report();
        report.setId("rep-1");
        report.setStoreId("store-1");
        report.setRegion("north");
        report.setTotalActivities(5);
        report.setTotalProgrammes(2);

        when(reportService.generate(any(), any())).thenReturn(report);

        mockMvc.perform(get("/api/reports")
                .param("storeId", "store-1")
                .param("region", "north"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("rep-1"))
            .andExpect(jsonPath("$.storeId").value("store-1"))
            .andExpect(jsonPath("$.totalActivities").value(5));
    }

    @Test
    void generateReport_noParams_returns200() throws Exception {
        Report report = new Report();
        report.setId("rep-2");
        report.setStoreId("all");
        report.setRegion("all");

        when(reportService.generate(any(), any())).thenReturn(report);

        mockMvc.perform(get("/api/reports"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.storeId").value("all"));
    }
}
