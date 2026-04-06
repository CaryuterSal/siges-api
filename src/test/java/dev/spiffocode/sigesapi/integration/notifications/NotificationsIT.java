package dev.spiffocode.sigesapi.integration.notifications;

import dev.spiffocode.sigesapi.FlushedIntegrationTest;
import dev.spiffocode.sigesapi.IntegrationTestClass;
import dev.spiffocode.sigesapi.auth.application.service.BearerAuthService;
import dev.spiffocode.sigesapi.auth.presentation.dto.LoginRequest;
import dev.spiffocode.sigesapi.notifications.domain.model.Notification;
import dev.spiffocode.sigesapi.notifications.domain.model.ReadStatus;
import dev.spiffocode.sigesapi.notifications.domain.model.Type;
import dev.spiffocode.sigesapi.notifications.domain.repository.NotificationRepository;
import dev.spiffocode.sigesapi.notifications.presentation.NotificationStatusChangeRequest;
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
import java.time.LocalDateTime;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTestClass
public class NotificationsIT extends FlushedIntegrationTest {

        @Autowired
        MockMvc mvc;
        @Autowired
        ObjectMapper mapper;
        @Autowired
        UserRepository userRepository;
        @Autowired
        NotificationRepository notificationRepository;
        @Autowired
        PasswordEncoder encoder;
        @Autowired
        BearerAuthService authService;

        private static final String API = "/notifications";
        private static final String VERSION = "1.0.0";

        private String studentToken;
        private Student testStudent;

        @BeforeEach
        void setup() {
                notificationRepository.deleteAll();
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

                studentToken = authService.login(new LoginRequest("student@siges.com", "password123"), "127.0.0.1")
                                .accessToken();
        }

        @Test
        void getNotifications_authenticated_returns200() throws Exception {
                Notification n1 = Notification.builder()
                                .title("Test")
                                .body("Test message")
                                .type(Type.LOGIN_NEW_DEVICE)
                                .readStatus(ReadStatus.UNREAD)
                                .sentAt(LocalDateTime.now())
                                .user(testStudent)
                                .build();
                notificationRepository.save(n1);

                mvc.perform(get(API)
                                .header("X-API-Version", VERSION)
                                .header("Authorization", "Bearer " + studentToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content", hasSize(1)))
                                .andExpect(jsonPath("$.content[0].title").value("Test"));
        }

        @Test
        void changeNotificationStatus_authenticated_returns200() throws Exception {
                Notification n1 = Notification.builder()
                                .title("Test")
                                .body("Test message")
                                .type(Type.LOGIN_NEW_DEVICE)
                                .readStatus(ReadStatus.UNREAD)
                                .sentAt(LocalDateTime.now())
                                .user(testStudent)
                                .build();
                n1 = notificationRepository.save(n1);

                NotificationStatusChangeRequest req = new NotificationStatusChangeRequest(ReadStatus.READ);

                mvc.perform(patch(API + "/" + n1.getId() + "/status")
                                .header("X-API-Version", VERSION)
                                .header("Authorization", "Bearer " + studentToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(req)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.readStatus").value(ReadStatus.READ.name()));
        }

        @Test
        void changeAllNotificationsStatus_authenticated_returns204() throws Exception {
                Notification n1 = Notification.builder()
                                .title("Test 1")
                                .body("Test message 1")
                                .type(Type.LOGIN_NEW_DEVICE)
                                .readStatus(ReadStatus.UNREAD)
                                .sentAt(LocalDateTime.now())
                                .user(testStudent)
                                .build();

                Notification n2 = Notification.builder()
                                .title("Test 2")
                                .body("Test message 2")
                                .type(Type.LOGIN_NEW_DEVICE)
                                .readStatus(ReadStatus.UNREAD)
                                .sentAt(LocalDateTime.now())
                                .user(testStudent)
                                .build();

                notificationRepository.save(n1);
                notificationRepository.save(n2);

                NotificationStatusChangeRequest req = new NotificationStatusChangeRequest(ReadStatus.READ);

                mvc.perform(patch(API + "/status")
                                .header("X-API-Version", VERSION)
                                .header("Authorization", "Bearer " + studentToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(req)))
                                .andExpect(status().isNoContent());

                mvc.perform(get(API + "?status=UNREAD")
                                .header("X-API-Version", VERSION)
                                .header("Authorization", "Bearer " + studentToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content", hasSize(0)));
        }

        @Test
        void getNotifications_unauthenticated_returns403() throws Exception {
                mvc.perform(get(API)
                                .header("X-API-Version", VERSION))
                                .andExpect(status().isForbidden());
        }
}
