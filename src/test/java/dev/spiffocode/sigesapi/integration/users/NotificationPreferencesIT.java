package dev.spiffocode.sigesapi.integration.users;

import dev.spiffocode.sigesapi.FlushedIntegrationTest;
import dev.spiffocode.sigesapi.IntegrationTestClass;
import dev.spiffocode.sigesapi.auth.application.service.BearerAuthService;
import dev.spiffocode.sigesapi.auth.presentation.dto.LoginRequest;
import dev.spiffocode.sigesapi.notifications.domain.model.Type;
import dev.spiffocode.sigesapi.users.domain.model.Admin;
import dev.spiffocode.sigesapi.users.domain.model.Student;
import dev.spiffocode.sigesapi.users.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for:
 * GET /users/me/notification-preferences
 * PUT /users/me/notification-preferences
 */
@IntegrationTestClass
class NotificationPreferencesIT extends FlushedIntegrationTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper mapper;
    @Autowired
    UserRepository userRepository;
    @Autowired
    PasswordEncoder encoder;
    @Autowired
    BearerAuthService authService;

    private static final String API = "/users/me/notification-preferences";
    private static final String VERSION = "1.0.0";

    private String studentToken;
    private String student2Token;
    private String adminToken;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();

        Admin admin = Admin.builder()
                .email("admin@utez.edu.mx")
                .password(encoder.encode("Admin123!"))
                .firstName("Admin")
                .lastName("Test")
                .birthDate(LocalDate.of(1980, 1, 1))
                .phoneNumber("+525512340001")
                .createdBy("system")
                .build();
        userRepository.save(admin);

        Student student = Student.builder()
                .email("student@utez.edu.mx")
                .password(encoder.encode("Admin123!"))
                .firstName("Student")
                .lastName("One")
                .birthDate(LocalDate.of(2000, 1, 1))
                .phoneNumber("+525512340002")
                .registrationNumber("20201ds001")
                .createdBy("admin@utez.edu.mx")
                .build();
        userRepository.save(student);

        Student student2 = Student.builder()
                .email("student2@utez.edu.mx")
                .password(encoder.encode("Admin123!"))
                .firstName("Student")
                .lastName("Two")
                .birthDate(LocalDate.of(2001, 5, 14))
                .phoneNumber("+525512340003")
                .registrationNumber("20201ds003")
                .createdBy("admin@utez.edu.mx")
                .build();
        userRepository.save(student2);

        adminToken = authService.login(new LoginRequest("admin@utez.edu.mx", "Admin123!"), "127.0.0.1").accessToken();
        studentToken = authService.login(new LoginRequest("student@utez.edu.mx", "Admin123!"), "127.0.0.1")
                .accessToken();
        student2Token = authService.login(new LoginRequest("student2@utez.edu.mx", "Admin123!"), "127.0.0.1")
                .accessToken();
    }

    // ══════════════════════════════════════════════════════════
    // GET /users/me/notification-preferences
    // ══════════════════════════════════════════════════════════

    @Test
    void getNotificationPreferences_asStudent_returns200WithList() throws Exception {
        mvc.perform(get(API)
                .header("X-API-Version", VERSION)
                .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getNotificationPreferences_asAdmin_returns200WithList() throws Exception {
        mvc.perform(get(API)
                .header("X-API-Version", VERSION)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getNotificationPreferences_unauthenticated_returns403() throws Exception {
        mvc.perform(get(API)
                .header("X-API-Version", VERSION))
                .andExpect(status().isForbidden());
    }

    @Test
    void getNotificationPreferences_eachEntryHasRequiredFields() throws Exception {
        mvc.perform(get(API)
                .header("X-API-Version", VERSION)
                .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].type", everyItem(notNullValue())))
                .andExpect(jsonPath("$[*].emailEnabled", everyItem(notNullValue())))
                .andExpect(jsonPath("$[*].inAppEnabled", everyItem(notNullValue())));
    }

    @Test
    void getNotificationPreferences_hasAllEntries() throws Exception {
        mvc.perform(get(API)
                        .header("X-API-Version", VERSION)
                        .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(Type.values().length));
    }

    @Test
    void updateNotificationPreferences_asStudent_withValidPayload_returns200() throws Exception {
        String body = """
                [
                  { "type": "RESERVATION_REMINDER", "emailEnabled": false, "inAppEnabled": true },
                  { "type": "LOGIN_NEW_DEVICE",       "emailEnabled": true,  "inAppEnabled": false }
                ]
                """;

        mvc.perform(put(API)
                .header("X-API-Version", VERSION)
                .header("Authorization", "Bearer " + studentToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    void updateNotificationPreferences_changesAreReflectedOnGet() throws Exception {
        String updateBody = """
                [
                  { "type": "RESERVATION_REMINDER", "emailEnabled": false, "inAppEnabled": false }
                ]
                """;

        // Apply the update
        mvc.perform(put(API)
                .header("X-API-Version", VERSION)
                .header("Authorization", "Bearer " + studentToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateBody))
                .andExpect(status().isOk());

        // Verify GET reflects the change
        mvc.perform(get(API)
                .header("X-API-Version", VERSION)
                .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.type == 'RESERVATION_REMINDER')].emailEnabled",
                        hasItem(false)))
                .andExpect(jsonPath("$[?(@.type == 'RESERVATION_REMINDER')].inAppEnabled",
                        hasItem(false)));
    }

    @Test
    void updateNotificationPreferences_asAdmin_withValidPayload_returns200() throws Exception {
        String body = """
                [
                  { "type": "RESERVATION_REMINDER", "emailEnabled": true, "inAppEnabled": true }
                ]
                """;

        mvc.perform(put(API)
                .header("X-API-Version", VERSION)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void updateNotificationPreferences_unauthenticated_returns403() throws Exception {
        String body = """
                [
                  { "type": "RESERVATION_REMINDER", "emailEnabled": true, "inAppEnabled": true }
                ]
                """;

        mvc.perform(put(API)
                .header("X-API-Version", VERSION)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateNotificationPreferences_withNullType_returns400() throws Exception {
        String body = """
                [
                  { "emailEnabled": true, "inAppEnabled": true }
                ]
                """;

        mvc.perform(put(API)
                .header("X-API-Version", VERSION)
                .header("Authorization", "Bearer " + studentToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"));
    }

    @Test
    void updateNotificationPreferences_withEmptyList_returns200() throws Exception {
        // Empty list = no updates, the service should still return the current
        // preferences
        mvc.perform(put(API)
                .header("X-API-Version", VERSION)
                .header("Authorization", "Bearer " + studentToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("[]"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void updateNotificationPreferences_doesNotAffectOtherUsers() throws Exception {
        String updateBody = """
                [
                  { "type": "RESERVATION_REMINDER", "emailEnabled": false, "inAppEnabled": false }
                ]
                """;

        // student updates their preferences
        mvc.perform(put(API)
                .header("X-API-Version", VERSION)
                .header("Authorization", "Bearer " + studentToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateBody))
                .andExpect(status().isOk());

        // student2 still gets the default preferences (emailEnabled = true by default)
        mvc.perform(get(API)
                .header("X-API-Version", VERSION)
                .header("Authorization", "Bearer " + student2Token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.type == 'RESERVATION_REMINDER')].emailEnabled",
                        not(hasItem(false))));
    }
}
