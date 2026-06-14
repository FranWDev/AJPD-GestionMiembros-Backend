package org.dubini.gestion.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
class HeartbeatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testHeartbeatIsPublicAndReturnsOk() throws Exception {
        mockMvc.perform(get("/api/heartbeat"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Heartbeat OK - service is active"));
    }
}
