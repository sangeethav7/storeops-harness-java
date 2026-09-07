package com.storeops.programmes;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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

@WebMvcTest(ProgrammeController.class)
class ProgrammeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProgrammeService programmeService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAllProgrammes_returns200() throws Exception {
        Programme p = new Programme();
        p.setId("prog-1");
        p.setName("Summer Sale");

        when(programmeService.findAll()).thenReturn(List.of(p));

        mockMvc.perform(get("/api/programmes"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value("prog-1"));
    }

    @Test
    void createProgramme_returns201() throws Exception {
        Programme request = new Programme();
        request.setName("Summer Sale");

        Programme response = new Programme();
        response.setId("prog-1");
        response.setName("Summer Sale");
        response.setActive(true);

        when(programmeService.create(any())).thenReturn(response);

        mockMvc.perform(post("/api/programmes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value("prog-1"))
            .andExpect(jsonPath("$.active").value(true));
    }

    @Test
    void getProgramme_returns200() throws Exception {
        Programme response = new Programme();
        response.setId("prog-1");
        response.setName("Summer Sale");

        when(programmeService.findById("prog-1")).thenReturn(response);

        mockMvc.perform(get("/api/programmes/prog-1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value("prog-1"));
    }

    @Test
    void updateProgramme_returns200() throws Exception {
        Programme updates = new Programme();
        updates.setName("Winter Sale");

        Programme response = new Programme();
        response.setId("prog-1");
        response.setName("Winter Sale");
        response.setActive(true);

        when(programmeService.update(eq("prog-1"), any())).thenReturn(response);

        mockMvc.perform(put("/api/programmes/prog-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updates)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Winter Sale"));
    }
}
