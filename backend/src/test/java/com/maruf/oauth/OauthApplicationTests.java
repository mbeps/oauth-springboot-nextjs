package com.maruf.oauth;

import com.maruf.oauth.config.JwksKeyLoader;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Basic integration coverage to ensure the Spring context boots
 * and public endpoints stay reachable without authentication.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OauthApplicationTests {

    @MockBean
    private JwksKeyLoader jwksKeyLoader;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void contextLoads() {
        // Verifies the Spring application context starts successfully with the provided
        // Testcontainer.
    }

    @Test
    void publicHealthEndpointRespondsOk() throws Exception {
        mockMvc.perform(get("/api/public/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("Public endpoint is working"));
    }
}
