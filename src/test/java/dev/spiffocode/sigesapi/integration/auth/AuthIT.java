package dev.spiffocode.sigesapi.integration.auth;

import dev.spiffocode.sigesapi.FixedClockConfig;
import dev.spiffocode.sigesapi.FlushedIntegrationTest;
import dev.spiffocode.sigesapi.IntegrationTestClass;
import dev.spiffocode.sigesapi.auth.application.service.BearerAuthService;
import dev.spiffocode.sigesapi.auth.domain.model.LogInAttemptsRepository;
import dev.spiffocode.sigesapi.auth.presentation.dto.AuthenticatedResponse;
import dev.spiffocode.sigesapi.auth.presentation.dto.LoginRequest;
import dev.spiffocode.sigesapi.auth.presentation.dto.RefreshRequest;
import dev.spiffocode.sigesapi.users.domain.model.Student;
import dev.spiffocode.sigesapi.users.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import tools.jackson.databind.ObjectMapper;

import java.time.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@IntegrationTestClass
@Import({FixedClockConfig.class})
class AuthIT extends FlushedIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(AuthIT.class);
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired UserRepository repo;
    @Autowired PasswordEncoder encoder;
    @Autowired
    BearerAuthService bearerAuthService;

    @Autowired
    LogInAttemptsRepository logInAttemptsRepository;

    private static final String API = "/auth";
    private static final String VERSION = "1.0.0";

    @Qualifier("controllerEndpointHandlerMapping")
    @Autowired
    RequestMappingHandlerMapping handlerMapping;


    @Qualifier("requestMappingHandlerMapping")
    @Autowired
    RequestMappingHandlerMapping mapping;

    @Disabled("only used for debug")
    @Test
    void printMappings() {
        log.debug("PRINTING METHODS CONTROLLER");
        handlerMapping.getHandlerMethods().forEach((info, method) -> log.debug("{} -> {}", info, method));
        log.debug("PRINTING REQUEST METHODS CONTROLLER");
        mapping.getHandlerMethods().forEach((info, method) -> log.debug("{} -> {}", info, method));
    }


    @BeforeEach
    void setup() {
        FixedClockConfig.reset();
        repo.deleteAll();

        Student s = Student.builder()
                .email("user@mail.com")
                .phoneNumber("5551111111")
                .firstName("U")
                .lastName("S")
                .birthDate(LocalDate.of(2000,1,1))
                .password(encoder.encode("123456"))
                .registrationNumber("REG1")
                .createdBy("user@example.com")
                .build();

        repo.save(s);
    }

    @Test
    void login_ok_returnsTokens() throws Exception {

        LoginRequest request = new LoginRequest("user@mail.com", "123456");


        mvc.perform(post(API + "/login")
                        .header("X-API-Version", VERSION)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andExpect(jsonPath("$.refreshToken").exists())
                .andExpect(jsonPath("$.role").value("STUDENT"))
                .andExpect(jsonPath("$.claims").isArray())
                .andExpect(jsonPath("$.claims.length()").value(2))
                .andExpect(jsonPath("$.claims[0].authority").value("ROLE_STUDENT"))
                .andExpect(jsonPath("$.claims[1].authority").value("FACTOR_PASSWORD"));
    }

    @Test
    void login_badCredentials_returns401_problemDetail() throws Exception {

        mvc.perform(
                post(API + "/login")
                        .header("X-API-Version", VERSION)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"identifier":"user@mail.com","password":"bad"}
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("Invalid credentials"));
    }

    @Test
    void refresh_ok_returnsNewAccessToken() throws Exception {

        String loginJson = mvc.perform(post(API + "/login")
                        .header("X-API-Version", VERSION)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"identifier":"user@mail.com","password":"123456"}
                                """))
                .andReturn().getResponse().getContentAsString();

        String refresh = mapper.readTree(loginJson).get("refreshToken").asString();

        RefreshRequest request = new RefreshRequest(refresh);
        mvc.perform(post(API + "/refresh")
                        .header("X-API-Version", VERSION)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists());
    }

    @Test
    void refresh_invalidToken_returns401() throws Exception {


        RefreshRequest request = new RefreshRequest("bad.token");

        mvc.perform(post(API + "/refresh")
                        .header("X-API-Version", VERSION)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.title").value("Invalid token"));
    }

    @Test
    void logout_ok_returns204() throws Exception {


        LoginRequest request = new LoginRequest("user@mail.com", "123456");

        String loginJson = mvc.perform(post(API + "/login")
                        .header("X-API-Version", VERSION)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(request)))
                .andReturn().getResponse().getContentAsString();

        var node = mapper.readTree(loginJson);

        String access = node.get("accessToken").asString();
        String refresh = node.get("refreshToken").asString();

        RefreshRequest requestRefresh = new RefreshRequest(refresh);

        mvc.perform(post(API + "/logout")
                        .header("X-API-Version", VERSION)
                        .header("Authorization", "Bearer " + access)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(requestRefresh)))
                .andExpect(status().isNoContent());
    }


    @Test
    void login_invalidBody_returns400_problemDetail() throws Exception {

        mvc.perform(post(API + "/login")
                        .header("X-API-Version", VERSION)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"));
    }

    @Test
    void logout_invalidBody_returns400_problemDetail() throws Exception {

        AuthenticatedResponse authenticatedResponse = bearerAuthService.login(new LoginRequest("user@mail.com", "123456"), "197.168.1.1");
        mvc.perform(post(API + "/logout")
                        .header("X-API-Version", VERSION)
                        .header("Authorization", "Bearer " + authenticatedResponse.accessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"));
    }

    @Test
    void refresh_invalidBody_returns400_problemDetail() throws Exception {

        mvc.perform(post(API + "/refresh")
                        .header("X-API-Version", VERSION)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"));
    }

    @Test
    void login_after3Failures_returns429_withRemainingAttempts() throws Exception {
        String badLogin = """
            {"identifier":"user@mail.com","password":"bad"}
            """;

        // 3 fallos
        for (int i = 0; i < 3; i++) {
            var s =  mvc.perform(post(API + "/login")
                    .header("X-API-Version", VERSION)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(badLogin));
            System.out.println(s);
        }

        // el 4to debería estar bloqueado
        var s = mvc.perform(post(API + "/login")
                        .header("X-API-Version", VERSION)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badLogin))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string(HttpHeaders.RETRY_AFTER, "900"))
                .andExpect(jsonPath("$.title").value("Too many login attempts"))
                .andReturn();
        System.out.println(s);
    }

    @Test
    void login_failuresThenSuccess_resetsAttempts() throws Exception {
        String badLogin = """
            {"identifier":"user@mail.com","password":"bad"}
            """;
        String goodLogin = """
            {"identifier":"user@mail.com","password":"123456"}
            """;

        // 2 fallos
        mvc.perform(post(API + "/login")
                .header("X-API-Version", VERSION)
                .contentType(MediaType.APPLICATION_JSON)
                .content(badLogin));
        mvc.perform(post(API + "/login")
                .header("X-API-Version", VERSION)
                .contentType(MediaType.APPLICATION_JSON)
                .content(badLogin));

        // login exitoso reset
        mvc.perform(post(API + "/login")
                        .header("X-API-Version", VERSION)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(goodLogin))
                .andExpect(status().isOk());

        FixedClockConfig.delegate = Clock.offset(
                FixedClockConfig.delegate,
                Duration.ofSeconds(1));

        // 2 fallos
        mvc.perform(post(API + "/login")
                .header("X-API-Version", VERSION)
                .contentType(MediaType.APPLICATION_JSON)
                .content(badLogin))
                .andExpect(jsonPath("$.remainingAttempts").value(2));
        mvc.perform(post(API + "/login")
                .header("X-API-Version", VERSION)
                .contentType(MediaType.APPLICATION_JSON)
                .content(badLogin))
                .andExpect(jsonPath("$.remainingAttempts").value(1));
    }

    @Test
    void login_badCredentials_returnsRemainingAttempts() throws Exception {
        String badLogin = """
            {"identifier":"user@mail.com","password":"bad"}
            """;

        mvc.perform(post(API + "/login")
                        .header("X-API-Version", VERSION)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badLogin))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.remainingAttempts").value(2));

        mvc.perform(post(API + "/login")
                        .header("X-API-Version", VERSION)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badLogin))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.remainingAttempts").value(1));
    }



    @Test
    void login_blockedAccount_unblocksAfter15Minutes() throws Exception {
        String badLogin = """
                {"identifier":"user@mail.com","password":"bad"}
                """;
        String goodLogin = """
                {"identifier":"user@mail.com","password":"123456"}
                """;

        // 3 fallos
        for (int i = 0; i < 3; i++) {
            mvc.perform(post(API + "/login")
                    .header("X-API-Version", VERSION)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(badLogin));
        }

        mvc.perform(post(API + "/login")
                        .header("X-API-Version", VERSION)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badLogin))
                .andExpect(status().isTooManyRequests());

        FixedClockConfig.delegate = Clock.offset(
                FixedClockConfig.delegate,
                Duration.ofMinutes(15).plusSeconds(1));

        mvc.perform(post(API + "/login")
                        .header("X-API-Version", VERSION)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(goodLogin))
                .andExpect(status().isOk());
    }

    @Test
    void login_blockedAccount_stillBlockedBefore10Minutes() throws Exception {
        String badLogin = """
                {"identifier":"user@mail.com","password":"bad"}
                """;

        for (int i = 0; i < 3; i++) {
            mvc.perform(post(API + "/login")
                    .header("X-API-Version", VERSION)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(badLogin));
        }


        FixedClockConfig.delegate = Clock.offset(
                FixedClockConfig.delegate,
                Duration.ofMinutes(9));

        mvc.perform(post(API + "/login")
                        .header("X-API-Version", VERSION)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badLogin))
                .andExpect(status().isTooManyRequests());
    }
}

