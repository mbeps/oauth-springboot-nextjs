package com.maruf.oauth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.maruf.oauth.config.JwtAuthenticationFilter;
import com.maruf.oauth.dto.ActionRequest;
import com.maruf.oauth.dto.ProtectedDataResponse;
import com.maruf.oauth.dto.UserResponse;
import com.maruf.oauth.support.TestOAuth2Users;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = ApiController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = JwtAuthenticationFilter.class)
)
@AutoConfigureMockMvc(addFilters = false)
class ApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void publicHealthEndpointResponds() throws Exception {
        mockMvc.perform(get("/api/public/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.message").value("Public endpoint is working"));
    }

    @Test
    void getUserRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/user"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getUserReturnsProfileWhenAuthenticated() throws Exception {
        OAuth2User principal = TestOAuth2Users.withAttributes(Map.of(
                "id", "1",
                "login", "octocat",
                "name", "Octo Cat",
                "email", "octo@example.com",
                "avatar_url", "http://example.com/avatar.png"
        ));

        ApiController controller = new ApiController();
        UserResponse response = controller.getUser(principal).getBody();

        assertThat(response).isNotNull();
        assertThat(response.getLogin()).isEqualTo("octocat");
        assertThat(response.getEmail()).isEqualTo("octo@example.com");
    }

    @Test
    void protectedEndpointsRequireAuthentication() throws Exception {
        mockMvc.perform(get("/api/protected/data"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/protected/action")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("action", "demo"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpointsReturnDataWhenAuthenticated() throws Exception {
        OAuth2User principal = TestOAuth2Users.withAttributes(Map.of(
                "id", "1",
                "login", "octocat",
                "name", "Octo Cat",
                "email", "octo@example.com"
        ));

        ApiController controller = new ApiController();

        ProtectedDataResponse dataResponse = controller.getProtectedData(principal).getBody();
        assertThat(dataResponse).isNotNull();
        assertThat(dataResponse.getUser()).isEqualTo("octocat");
        assertThat(dataResponse.getData().getItems()).isNotEmpty();

        ActionRequest request = new ActionRequest();
        request.setAction("demo-action");
        var actionResponse = controller.performAction(principal, request).getBody();
        assertThat(actionResponse).isNotNull();
        assertThat(actionResponse.getUser()).isEqualTo("octocat");
        assertThat(actionResponse.getAction()).isEqualTo("demo-action");
    }
}
