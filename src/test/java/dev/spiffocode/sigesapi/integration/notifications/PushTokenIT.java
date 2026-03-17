package dev.spiffocode.sigesapi.integration.notifications;

import dev.spiffocode.sigesapi.FlushedIntegrationTest;
import dev.spiffocode.sigesapi.IntegrationTestClass;
import dev.spiffocode.sigesapi.auth.application.service.BearerAuthService;
import dev.spiffocode.sigesapi.auth.presentation.dto.LoginRequest;
import dev.spiffocode.sigesapi.notifications.application.service.PushNotificationPort;
import dev.spiffocode.sigesapi.notifications.domain.model.Platform;
import dev.spiffocode.sigesapi.users.domain.model.Admin;
import dev.spiffocode.sigesapi.users.domain.model.Student;
import dev.spiffocode.sigesapi.users.domain.repository.UserRepository;
import dev.spiffocode.sigesapi.users.presentation.dto.PushTokenRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTestClass
public class PushTokenIT extends FlushedIntegrationTest {

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

    @MockitoBean
    PushNotificationPort pushNotificationPort;

    private static final String API = "/users";
    private static final String VERSION = "1.0.0";

    private String studentToken;
    private String adminToken;
    private Student testStudent;
    private Admin testAdmin;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();

        testStudent = Student.builder()
                .email("student@siges.com")
                .password(encoder.encode("password123"))
                .firstName("Student")
                .lastName("User")
                .birthDate(LocalDate.of(2000, 1, 1))
                .phoneNumber("+525555555556")
                .registrationNumber("STU001")
                .createdBy("system")
                .build();
        userRepository.save(testStudent);

        testAdmin = Admin.builder()
                .email("admin@siges.com")
                .password(encoder.encode("password123"))
                .firstName("Admin")
                .lastName("User")
                .birthDate(LocalDate.of(1980, 1, 1))
                .phoneNumber("+525555555555")
                .createdBy("system")
                .build();
        userRepository.save(testAdmin);

        studentToken = authService.login(new LoginRequest("student@siges.com", "password123"), "127.0.0.1")
                .accessToken();
        adminToken = authService.login(new LoginRequest("admin@siges.com", "password123"), "127.0.0.1").accessToken();
    }

    @Test
    void registerToken_student_returns204() throws Exception {
        PushTokenRequest req = new PushTokenRequest("some-fcm-token", "device-id-123", Platform.MOBILE);

        mvc.perform(post(API + "/me/push-tokens")
                .header("X-API-Version", VERSION)
                .header("Authorization", "Bearer " + studentToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
                .andExpect(status().isNoContent());

        // Validate getting users returns the token attached inside DB if needed?
        // Let's just trust the HTTP response
    }

    @Test
    void registerToken_admin_returns204() throws Exception {
        PushTokenRequest req = new PushTokenRequest("admin-fcm-token", "device-id-124", Platform.WEB);

        mvc.perform(post(API + "/me/push-tokens")
                .header("X-API-Version", VERSION)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
                .andExpect(status().isNoContent());
    }

    @Test
    void unregisterToken_student_returns204() throws Exception {
        // Register first
        PushTokenRequest req = new PushTokenRequest("some-fcm-token", "device-id-123", Platform.MOBILE);
        mvc.perform(post(API + "/me/push-tokens")
                .header("X-API-Version", VERSION)
                .header("Authorization", "Bearer " + studentToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
                .andExpect(status().isNoContent());

        // Unregister
        mvc.perform(delete(API + "/me/push-tokens/some-fcm-token")
                .header("X-API-Version", VERSION)
                .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isNoContent());
    }
}
