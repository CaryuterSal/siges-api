package dev.spiffocode.sigesapi.integration.users;

import dev.spiffocode.sigesapi.FixedClockConfig;
import dev.spiffocode.sigesapi.FlushedIntegrationTest;
import dev.spiffocode.sigesapi.IntegrationTestClass;
import dev.spiffocode.sigesapi.auth.infrastructure.JwtService;
import dev.spiffocode.sigesapi.mailsender.application.service.UserManagementEmailPort;
import dev.spiffocode.sigesapi.users.domain.model.PasswordRecoveryToken;
import dev.spiffocode.sigesapi.users.domain.model.RecoveryPlatform;
import dev.spiffocode.sigesapi.users.domain.model.Student;
import dev.spiffocode.sigesapi.users.domain.repository.PasswordRecoveryTokenRepository;
import dev.spiffocode.sigesapi.users.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@IntegrationTestClass
@Import(FixedClockConfig.class)
class PasswordRecoveryIT extends FlushedIntegrationTest {

        @Autowired
        MockMvc mvc;
        @Autowired
        ObjectMapper mapper;
        @Autowired
        UserRepository userRepository;
        @Autowired
        PasswordRecoveryTokenRepository tokenRepository;
        @Autowired
        PasswordEncoder encoder;
        @Autowired
        JwtService jwtService;
        @Autowired
        Clock clock;

        // Mock the email port so @Async recovery requests don't require a real mail
        // server
        @MockitoBean
        UserManagementEmailPort emailPort;

        private static final String VERSION = "1.0.0";
        private static final String BASE = "/password-recovery";

        private Student savedStudent;

        @BeforeEach
        void setup() {
                FixedClockConfig.reset();
                tokenRepository.deleteAll();
                userRepository.deleteAll();

                savedStudent = Student.builder()
                                .email("student@utez.edu.mx")
                                .password(encoder.encode("Admin123!"))
                                .firstName("Test")
                                .lastName("Student")
                                .birthDate(LocalDate.of(2000, 1, 1))
                                .phoneNumber("+525512340001")
                                .registrationNumber("20201ds001")
                                .createdBy("admin@utez.edu.mx")
                                .build();
                savedStudent = userRepository.save(savedStudent);
        }

        // ══════════════════════════════════════════════════════════
        // POST /password-recovery/request
        // ══════════════════════════════════════════════════════════

        @Test
        void requestRecovery_existingEmail_returns202() throws Exception {
                mvc.perform(post(BASE + "/request")
                                .header("X-API-Version", VERSION)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {"email": "student@utez.edu.mx", "platform": "WEB"}
                                                """))
                                .andExpect(status().isAccepted());
        }

        @Test
        void requestRecovery_nonExistentEmail_stillReturns202() throws Exception {
                // Enumeration protection – always 202 regardless of email existence
                mvc.perform(post(BASE + "/request")
                                .header("X-API-Version", VERSION)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {"email": "ghost@utez.edu.mx", "platform": "WEB"}
                                                """))
                                .andExpect(status().isAccepted());
        }

        @Test
        void requestRecovery_invalidEmailDomain_returns400() throws Exception {
                mvc.perform(post(BASE + "/request")
                                .header("X-API-Version", VERSION)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {"email": "student@gmail.com", "platform": "WEB"}
                                                """))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.title").value("Validation failed"));
        }

        @Test
        void requestRecovery_missingPlatform_returns400() throws Exception {
                mvc.perform(post(BASE + "/request")
                                .header("X-API-Version", VERSION)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {"email": "student@utez.edu.mx"}
                                                """))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.title").value("Validation failed"));
        }

        @Test
        void requestRecovery_mobileplatform_returns202() throws Exception {
                mvc.perform(post(BASE + "/request")
                                .header("X-API-Version", VERSION)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {"email": "student@utez.edu.mx", "platform": "MOBILE"}
                                                """))
                                .andExpect(status().isAccepted());
        }

        @Test
        void requestRecovery_noAuthRequired_returns202() throws Exception {
                // Endpoint is public (anyRequest().authenticated matches but 202 is returned
                // before any auth validation since there's no token on a public endpoint)
                mvc.perform(post(BASE + "/request")
                                .header("X-API-Version", VERSION)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {"email": "student@utez.edu.mx", "platform": "WEB"}
                                                """))
                                .andExpect(status().isAccepted());
        }

        // ══════════════════════════════════════════════════════════
        // GET /password-recovery/redirect (permitAll)
        // ══════════════════════════════════════════════════════════

        @Test
        void redirect_withValidToken_returns302WithLocation() throws Exception {
                String tokenValue = seedTokenForStudent(false, false);

                mvc.perform(get(BASE + "/redirect")
                                .header("X-API-Version", VERSION)
                                .param("token", tokenValue))
                                .andExpect(status().isFound())
                                .andExpect(header().exists("Location"))
                                .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("token=")));
        }

        @Test
        void redirect_withExpiredToken_redirectsWithErrorParam() throws Exception {
                String tokenValue = seedTokenForStudent(true, false);

                mvc.perform(get(BASE + "/redirect")
                                .header("X-API-Version", VERSION)
                                .param("token", tokenValue))
                                .andExpect(status().isFound())
                                .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("error=")));
        }

        @Test
        void redirect_withUsedToken_redirectsWithErrorParam() throws Exception {
                String tokenValue = seedTokenForStudent(false, true);

                mvc.perform(get(BASE + "/redirect")
                                .header("X-API-Version", VERSION)
                                .param("token", tokenValue))
                                .andExpect(status().isFound())
                                .andExpect(header().string("Location", org.hamcrest.Matchers.containsString("error=")));
        }

        @Test
        void redirect_noToken_returns400() throws Exception {
                mvc.perform(get(BASE + "/redirect")
                                .header("X-API-Version", VERSION))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void redirect_blankToken_returns400() throws Exception {
                mvc.perform(get(BASE + "/redirect")
                                .header("X-API-Version", VERSION)
                                .param("token", ""))
                                .andExpect(status().isBadRequest());
        }

        @Test
        void redirect_noAuthRequired_returns302() throws Exception {
                // permitAll – should work without any Authorization header
                String tokenValue = seedTokenForStudent(false, false);

                mvc.perform(get(BASE + "/redirect")
                                .header("X-API-Version", VERSION)
                                .param("token", tokenValue))
                                .andExpect(status().isFound());
        }

        // ══════════════════════════════════════════════════════════
        // PATCH /password-recovery/reset
        // ══════════════════════════════════════════════════════════

        @Test
        void resetPassword_withValidToken_returns204() throws Exception {
                String tokenValue = seedTokenForStudent(false, false);

                mvc.perform(patch(BASE + "/reset")
                                .header("X-API-Version", VERSION)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {
                                                  "token": "%s",
                                                  "newPassword": "NewPass123!"
                                                }
                                                """.formatted(tokenValue)))
                                .andExpect(status().isNoContent());
        }

        @Test
        void resetPassword_withExpiredToken_returns410() throws Exception {
                String tokenValue = seedTokenForStudent(true, false);

                mvc.perform(patch(BASE + "/reset")
                                .header("X-API-Version", VERSION)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {
                                                  "token": "%s",
                                                  "newPassword": "NewPass123!"
                                                }
                                                """.formatted(tokenValue)))
                                .andExpect(status().isGone());
        }

        @Test
        void resetPassword_withUsedToken_returns410() throws Exception {
                String tokenValue = seedTokenForStudent(false, true);

                mvc.perform(patch(BASE + "/reset")
                                .header("X-API-Version", VERSION)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {
                                                  "token": "%s",
                                                  "newPassword": "NewPass123!"
                                                }
                                                """.formatted(tokenValue)))
                                .andExpect(status().isGone());
        }

        @Test
        void resetPassword_tokenCanOnlyBeUsedOnce() throws Exception {
                String tokenValue = seedTokenForStudent(false, false);

                // First reset – should succeed
                mvc.perform(patch(BASE + "/reset")
                                .header("X-API-Version", VERSION)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {
                                                  "token": "%s",
                                                  "newPassword": "NewPass123!"
                                                }
                                                """.formatted(tokenValue)))
                                .andExpect(status().isNoContent());

                // Second reset with same token – should fail with 410
                mvc.perform(patch(BASE + "/reset")
                                .header("X-API-Version", VERSION)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {
                                                  "token": "%s",
                                                  "newPassword": "AnotherPass1!"
                                                }
                                                """.formatted(tokenValue)))
                                .andExpect(status().isGone());
        }

        @Test
        void resetPassword_weakPassword_returns400() throws Exception {
                String tokenValue = seedTokenForStudent(false, false);

                mvc.perform(patch(BASE + "/reset")
                                .header("X-API-Version", VERSION)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {
                                                  "token": "%s",
                                                  "newPassword": "weak"
                                                }
                                                """.formatted(tokenValue)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.title").value("Validation failed"));
        }

        @Test
        void resetPassword_missingToken_returns400() throws Exception {
                mvc.perform(patch(BASE + "/reset")
                                .header("X-API-Version", VERSION)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {
                                                  "newPassword": "NewPass123!"
                                                }
                                                """))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.title").value("Validation failed"));
        }

        @Test
        void resetPassword_missingNewPassword_returns400() throws Exception {
                String tokenValue = seedTokenForStudent(false, false);

                mvc.perform(patch(BASE + "/reset")
                                .header("X-API-Version", VERSION)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                                {
                                                  "token": "%s"
                                                }
                                                """.formatted(tokenValue)))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.title").value("Validation failed"));
        }

        // ══════════════════════════════════════════════════════════
        // Helpers
        // ══════════════════════════════════════════════════════════

        /**
         * Seeds a recovery token for the seeded student and returns the JWT string.
         *
         * @param expired whether the token should be considered expired
         * @param used    whether the token should be pre-consumed
         */
        private String seedTokenForStudent(boolean expired, boolean used) {
                Duration expiration = Duration.ofMinutes(30);

                String jti = UUID.randomUUID().toString();
                String tokenValue = jwtService.generateRecoveryToken(
                                jti,
                                savedStudent.getEmail(),
                                // Use immediately-expiring duration so validation sees it as expired if flag
                                // set
                                expired ? Duration.ofSeconds(-1) : expiration);

                LocalDateTime expiresAt = expired
                                ? LocalDateTime.now(clock).minusMinutes(1)
                                : LocalDateTime.now(clock).plus(expiration);

                PasswordRecoveryToken entity = PasswordRecoveryToken.builder()
                                .jti(jti)
                                .platform(RecoveryPlatform.WEB)
                                .user(savedStudent)
                                .expiresAt(expiresAt)
                                .build();

                if (used) {
                        entity.markAsUsed();
                }

                tokenRepository.save(entity);
                return tokenValue;
        }
}
