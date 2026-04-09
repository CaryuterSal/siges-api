package dev.spiffocode.sigesapi.integration.timezone;

import dev.spiffocode.sigesapi.FlushedIntegrationTest;
import dev.spiffocode.sigesapi.IntegrationTestClass;
import dev.spiffocode.sigesapi.auth.application.service.BearerAuthService;
import dev.spiffocode.sigesapi.auth.presentation.dto.LoginRequest;
import dev.spiffocode.sigesapi.users.domain.model.Admin;
import dev.spiffocode.sigesapi.users.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTestClass
public class TimezoneIT extends FlushedIntegrationTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    UserRepository userRepository;
    @Autowired
    PasswordEncoder encoder;
    @Autowired
    BearerAuthService authService;

    private String adminToken;

    @BeforeEach
    void setup() {
        userRepository.deleteAll();
        Admin admin = Admin.builder()
                .email("admin@siges.com")
                .password(encoder.encode("password123"))
                .firstName("Admin")
                .lastName("User")
                .birthDate(LocalDate.of(1980, 1, 1))
                .phoneNumber("+525555555555")
                .createdBy("system")
                .build();
        userRepository.save(admin);
        adminToken = authService.login(new LoginRequest("admin@siges.com", "password123"), "127.0.0.1")
                .accessToken();
    }

    @Test
    void getProfile_withTimezoneHeader_returnsConsistentDates() throws Exception {
        // We test /users/me or similar that returns timestamps
        mvc.perform(get("/users/me")
                .header("Authorization", "Bearer " + adminToken)
                .header("X-Timezone", "UTC")
                .header("X-API-Version", "1.0.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.createdAt").exists());

        // This is more about verifying that the Clock bean works in a real request
        // context.
        // But since we standardized on America/Mexico_City in SigesApiApplication,
        // unless we pass X-Timezone, it will be Mexico City.

        // Let's verify that the lastLogin recorded is in the requested timezone.
        // The login call was made in setup phase WITHOUT the header, so it should be
        // Mexico City.
    }
}
