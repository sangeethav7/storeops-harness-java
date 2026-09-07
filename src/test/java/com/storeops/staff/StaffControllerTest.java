package com.storeops.staff;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(StaffController.class)
class StaffControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StaffService staffService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAllStaff_returns200() throws Exception {
        Staff s = new Staff();
        s.setId("staff-1");
        s.setUsername("admin");
        s.setRole("MANAGER");

        when(staffService.findAll()).thenReturn(List.of(s));

        mockMvc.perform(get("/api/staff"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value("staff-1"))
            .andExpect(jsonPath("$[0].role").value("MANAGER"));
    }

    @Test
    void authenticate_returns200() throws Exception {
        AuthRequest request = new AuthRequest();
        request.setUsername("admin");
        request.setPassword("admin123");

        AuthResponse response = new AuthResponse("token-staff-1", "staff-1", "MANAGER");

        when(staffService.authenticate(any())).thenReturn(response);

        mockMvc.perform(post("/api/staff/auth")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.token").value("token-staff-1"))
            .andExpect(jsonPath("$.role").value("MANAGER"));
    }

    @Test
    void authenticate_invalidCredentials_returns401() throws Exception {
        AuthRequest request = new AuthRequest();
        request.setUsername("admin");
        request.setPassword("wrong");

        when(staffService.authenticate(any()))
            .thenThrow(new com.storeops.common.exception.AppException(
                "AUTH_FAILED", "Invalid credentials",
                org.springframework.http.HttpStatus.UNAUTHORIZED));

        mockMvc.perform(post("/api/staff/auth")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isUnauthorized())
            .andExpect(jsonPath("$.errorCode").value("AUTH_FAILED"));
    }
}
