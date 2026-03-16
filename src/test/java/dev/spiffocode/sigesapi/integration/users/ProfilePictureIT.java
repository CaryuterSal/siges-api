package dev.spiffocode.sigesapi.integration.users;

import dev.spiffocode.sigesapi.FlushedIntegrationTest;
import dev.spiffocode.sigesapi.IntegrationTestClass;
import dev.spiffocode.sigesapi.auth.application.service.BearerAuthService;
import dev.spiffocode.sigesapi.auth.presentation.dto.LoginRequest;
import dev.spiffocode.sigesapi.users.domain.model.Admin;
import dev.spiffocode.sigesapi.users.domain.model.Student;
import dev.spiffocode.sigesapi.users.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTestClass
class ProfilePictureIT extends FlushedIntegrationTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    UserRepository userRepository;
    @Autowired
    PasswordEncoder encoder;
    @Autowired
    BearerAuthService authService;

    private static final String VERSION = "1.0.0";

    private String studentToken;
    private String student2Token;
    private String adminToken;

    private static final byte[] PNG_BYTES;

    static {
        URL fileURL = ProfilePictureIT.class.getClassLoader().getResource("avatar.jpg");
        try {
            Path path = Path.of(fileURL.toURI());
            PNG_BYTES = Files.readAllBytes(path);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

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
    // PUT /users/me/profile-picture
    // ══════════════════════════════════════════════════════════

    @Test
    void updateProfilePicture_asStudent_withValidPng_returns200() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "avatar.png", MediaType.IMAGE_PNG_VALUE, PNG_BYTES);

        mvc.perform(multipart("/users/me/profile-picture")
                .file(file)
                .with(request -> {
                    request.setMethod("PUT");
                    return request;
                })
                .header("X-API-Version", VERSION)
                .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profilePictureUrl", notNullValue()))
                .andExpect(jsonPath("$.profilePictureUrl", containsString("cdn.test.local")));
    }

    @Test
    void updateProfilePicture_asAdmin_withValidPng_returns200() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "avatar.png", MediaType.IMAGE_PNG_VALUE, PNG_BYTES);

        mvc.perform(multipart("/users/me/profile-picture")
                .file(file)
                .with(request -> {
                    request.setMethod("PUT");
                    return request;
                })
                .header("X-API-Version", VERSION)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profilePictureUrl", notNullValue()));
    }

    @Test
    void updateProfilePicture_unauthenticated_returns403() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "avatar.png", MediaType.IMAGE_PNG_VALUE, PNG_BYTES);

        mvc.perform(multipart("/users/me/profile-picture")
                .file(file)
                .with(request -> {
                    request.setMethod("PUT");
                    return request;
                })
                .header("X-API-Version", VERSION))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateProfilePicture_withEmptyFile_returns400() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "empty.png", MediaType.IMAGE_PNG_VALUE, new byte[0]);

        mvc.perform(multipart("/users/me/profile-picture")
                .file(file)
                .with(request -> {
                    request.setMethod("PUT");
                    return request;
                })
                .header("X-API-Version", VERSION)
                .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateProfilePicture_withTextFile_returns400() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "hack.txt", MediaType.TEXT_PLAIN_VALUE, "not an image".getBytes());

        mvc.perform(multipart("/users/me/profile-picture")
                .file(file)
                .with(request -> {
                    request.setMethod("PUT");
                    return request;
                })
                .header("X-API-Version", VERSION)
                .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateProfilePicture_urlIsUpdatedInDatabase_afterUpload() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "avatar.png", MediaType.IMAGE_PNG_VALUE, PNG_BYTES);

        // Upload the profile picture
        mvc.perform(multipart("/users/me/profile-picture")
                .file(file)
                .with(request -> {
                    request.setMethod("PUT");
                    return request;
                })
                .header("X-API-Version", VERSION)
                .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk());

        // Verify the URL now appears when fetching this user
        mvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .get("/users/lookup")
                .param("identifier", "student@utez.edu.mx")
                .header("X-API-Version", VERSION)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.profilePictureUrl", notNullValue()))
                .andExpect(jsonPath("$.profilePictureUrl", containsString("cdn.test.local")));
    }
}
