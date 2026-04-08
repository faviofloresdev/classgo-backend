package com.classgo.backend;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.classgo.backend.application.auth.GoogleTokenVerifier;
import com.classgo.backend.domain.enums.AuthProvider;
import com.classgo.backend.domain.repository.ParentRepository;
import com.classgo.backend.domain.repository.TeacherRepository;
import com.classgo.backend.domain.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest(properties = {
    "app.google.enabled=true",
    "app.google.client-id=test-google-client-id"
})
@AutoConfigureMockMvc
class GoogleAuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TeacherRepository teacherRepository;

    @Autowired
    private ParentRepository parentRepository;

    @MockBean
    private GoogleTokenVerifier googleTokenVerifier;

    @Test
    @DirtiesContext
    void googleLoginCreatesTeacherAndAllowsAuthenticatedProfileLookup() throws Exception {
        when(googleTokenVerifier.verify("valid-google-token"))
            .thenReturn(new GoogleTokenVerifier.GoogleUser("google.teacher@classgo.test", "Google Teacher", true));

        MvcResult loginResult = mockMvc.perform(post("/auth/google")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "idToken": "valid-google-token",
                      "role": "TEACHER"
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.accessToken").isString())
            .andExpect(jsonPath("$.refreshToken").isString())
            .andExpect(jsonPath("$.user.email").value("google.teacher@classgo.test"))
            .andExpect(jsonPath("$.user.role").value("TEACHER"))
            .andExpect(jsonPath("$.user.fullName").value("Google Teacher"))
            .andReturn();

        JsonNode root = objectMapper.readTree(loginResult.getResponse().getContentAsString());
        String accessToken = root.get("accessToken").asText();
        var user = userRepository.findByEmailIgnoreCase("google.teacher@classgo.test").orElseThrow();

        org.assertj.core.api.Assertions.assertThat(user.getAuthProvider()).isEqualTo(AuthProvider.GOOGLE);
        org.assertj.core.api.Assertions.assertThat(teacherRepository.findByUserId(user.getId())).isPresent();
        org.assertj.core.api.Assertions.assertThat(parentRepository.findByUserId(user.getId())).isEmpty();

        mockMvc.perform(get("/auth/me").header("Authorization", "Bearer " + accessToken))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("google.teacher@classgo.test"))
            .andExpect(jsonPath("$.role").value("TEACHER"))
            .andExpect(jsonPath("$.fullName").value("Google Teacher"));
    }

    @Test
    @DirtiesContext
    void googleLoginRejectsExistingAccountWithDifferentRole() throws Exception {
        when(googleTokenVerifier.verify(anyString()))
            .thenReturn(new GoogleTokenVerifier.GoogleUser("google.parent@classgo.test", "Google Parent", true));

        mockMvc.perform(post("/auth/google")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "idToken": "parent-token",
                      "role": "PARENT"
                    }
                    """))
            .andExpect(status().isOk());

        mockMvc.perform(post("/auth/google")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "idToken": "teacher-token",
                      "role": "TEACHER"
                    }
                    """))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("$.error.message").value("This Google account is already registered with a different role"));
    }
}
