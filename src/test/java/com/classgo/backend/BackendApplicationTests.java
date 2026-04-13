package com.classgo.backend;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class BackendApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void teacherLoginAndProfileFlowWorks() throws Exception {
        String token = login("teacher@classgo.test", "password");

        mockMvc.perform(get("/api/auth/me").header("Authorization", bearer(token)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("teacher@classgo.test"))
            .andExpect(jsonPath("$.role").value("TEACHER"))
            .andExpect(jsonPath("$.studentAvatarId").isEmpty())
            .andExpect(jsonPath("$.parentAvatarId").isEmpty());

        mockMvc.perform(patch("/api/users/me")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"name":"Profesor Garcia","avatarId":"robot-1"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Profesor Garcia"))
            .andExpect(jsonPath("$.avatarId").value("robot-1"))
            .andExpect(jsonPath("$.studentAvatarId").isEmpty())
            .andExpect(jsonPath("$.parentAvatarId").isEmpty());
    }

    @Test
    @DirtiesContext
    void studentProfileUpdateUnlocksAvatarAndProfileAchievements() throws Exception {
        String token = login("student@classgo.test", "password");

        mockMvc.perform(patch("/api/users/me")
                .header("Authorization", bearer(token))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"name":"Maria Lopez","avatarId":"animal-2"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.achievements.newlyUnlockedAchievements[*].code")
                .value(org.hamcrest.Matchers.hasItems("EXPLORATION_FEATURE_1", "EXPLORATION_AVATAR_STUDENT", "EXPLORATION_PROFILE_COMPLETE")))
            .andExpect(jsonPath("$.achievements.updatedProgress.distinctFeatures").value(2));
    }

    @Test
    @DirtiesContext
    void logoutRevokesJwt() throws Exception {
        String token = login("teacher@classgo.test", "password");

        mockMvc.perform(post("/api/auth/logout").header("Authorization", bearer(token)))
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/auth/me").header("Authorization", bearer(token)))
            .andExpect(status().isForbidden());
    }

    @Test
    @DirtiesContext
    void seededStudentCanReadGameplayContext() throws Exception {
        String token = login("student@classgo.test", "password");

        mockMvc.perform(get("/api/auth/me").header("Authorization", bearer(token)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.role").value("STUDENT"))
            .andExpect(jsonPath("$.studentAvatarId").isString())
            .andExpect(jsonPath("$.parentAvatarId").value("parent-1"));

        mockMvc.perform(get("/api/gameplay/context")
                .param("classroomId", "77777777-7777-7777-7777-777777777777")
                .header("Authorization", bearer(token)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.classroom.name").value("Matematicas 3A"))
            .andExpect(jsonPath("$.topic.name").value("Sumas Basicas"))
            .andExpect(jsonPath("$.attemptAllowed").value(true));
    }

    @Test
    void avatarsEndpointIncludesParentAvatar() throws Exception {
        mockMvc.perform(get("/api/avatars"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[?(@.id == 'parent-1')]").exists())
            .andExpect(jsonPath("$[?(@.id == 'parent-1')].name").value("Padre"));
    }

    @Test
    @DirtiesContext
    void studentSubmissionAppearsOnLeaderboard() throws Exception {
        String studentToken = login("student@classgo.test", "password");
        String teacherToken = login("teacher@classgo.test", "password");

        mockMvc.perform(post("/api/classrooms/77777777-7777-7777-7777-777777777777/topics/44444444-4444-4444-4444-444444444444/results")
                .header("Authorization", bearer(studentToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "weekNumber": 1,
                      "score": 95,
                      "timeSpent": 180,
                      "correctAnswers": 2,
                      "totalQuestions": 2,
                      "answers": [
                        {
                          "questionId": "q1",
                          "question": "Cuanto es 3 + 4?",
                          "correct": true,
                          "selected": "7"
                        }
                      ]
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.score").value(95))
            .andExpect(jsonPath("$.achievements.newlyUnlockedAchievements[0].code").value("PROGRESS_CHALLENGE_1"))
            .andExpect(jsonPath("$.achievements.newlyUnlockedAchievements[1].code").value("PERFORMANCE_HIGH_1"))
            .andExpect(jsonPath("$.achievements.newlyUnlockedAchievements[2].code").value("SPECIAL_FIRST_COMPLETION"))
            .andExpect(jsonPath("$.achievements.updatedProgress.completedChallenges").value(1))
            .andExpect(jsonPath("$.achievements.updatedProgress.highScoreChallenges").value(1));

        mockMvc.perform(get("/api/classrooms/77777777-7777-7777-7777-777777777777/leaderboard")
                .param("weekNumber", "1")
                .header("Authorization", bearer(teacherToken)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].student.name").value("Maria Lopez"))
            .andExpect(jsonPath("$[0].totalScore").value(95))
            .andExpect(jsonPath("$[0].rank").value(1));
    }

    @Test
    @DirtiesContext
    void repeatedChallengeResultDoesNotDuplicateProgressButCanAddPerfectScore() throws Exception {
        String studentToken = login("student@classgo.test", "password");

        mockMvc.perform(post("/api/classrooms/77777777-7777-7777-7777-777777777777/topics/44444444-4444-4444-4444-444444444444/results")
                .header("Authorization", bearer(studentToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "weekNumber": 1,
                      "score": 95,
                      "timeSpent": 180,
                      "correctAnswers": 2,
                      "totalQuestions": 2,
                      "answers": []
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.achievements.updatedProgress.completedChallenges").value(1))
            .andExpect(jsonPath("$.achievements.updatedProgress.highScoreChallenges").value(1))
            .andExpect(jsonPath("$.achievements.updatedProgress.perfectChallenges").value(0));

        mockMvc.perform(post("/api/classrooms/77777777-7777-7777-7777-777777777777/topics/44444444-4444-4444-4444-444444444444/results")
                .header("Authorization", bearer(studentToken))
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                      "weekNumber": 1,
                      "score": 100,
                      "timeSpent": 150,
                      "correctAnswers": 2,
                      "totalQuestions": 2,
                      "answers": []
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.achievements.newlyUnlockedAchievements[0].code").value("PERFORMANCE_PERFECT_1"))
            .andExpect(jsonPath("$.achievements.updatedProgress.completedChallenges").value(1))
            .andExpect(jsonPath("$.achievements.updatedProgress.highScoreChallenges").value(1))
            .andExpect(jsonPath("$.achievements.updatedProgress.perfectChallenges").value(1));
    }

    private String login(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"email":"%s","password":"%s"}
                    """.formatted(email, password)))
            .andExpect(status().isOk())
            .andReturn();

        JsonNode root = objectMapper.readTree(result.getResponse().getContentAsString());
        return root.get("token").asText();
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
