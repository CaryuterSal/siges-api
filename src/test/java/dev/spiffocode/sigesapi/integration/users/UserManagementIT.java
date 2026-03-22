package dev.spiffocode.sigesapi.integration.users;

import dev.spiffocode.sigesapi.FlushedIntegrationTest;
import dev.spiffocode.sigesapi.IntegrationTestClass;
import dev.spiffocode.sigesapi.auth.application.service.BearerAuthService;
import dev.spiffocode.sigesapi.auth.presentation.dto.LoginRequest;
import dev.spiffocode.sigesapi.users.domain.model.Admin;
import dev.spiffocode.sigesapi.users.domain.model.InstitutionalStaff;
import dev.spiffocode.sigesapi.users.domain.model.Student;
import dev.spiffocode.sigesapi.users.domain.repository.InstitutionalStaffRepository;
import dev.spiffocode.sigesapi.users.domain.repository.StudentRepository;
import dev.spiffocode.sigesapi.users.domain.repository.UserRepository;
import dev.spiffocode.sigesapi.users.presentation.dto.PasswordUpdateRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTestClass
class UserManagementIT extends FlushedIntegrationTest {

        @Autowired
        MockMvc mvc;
        @Autowired
        ObjectMapper mapper;
        @Autowired
        UserRepository userRepository;
        @Autowired
        StudentRepository studentRepository;
        @Autowired
        InstitutionalStaffRepository staffRepository;
        @Autowired
        PasswordEncoder encoder;
        @Autowired
        BearerAuthService authService;

        private static final String VERSION = "1.0.0";

        private String adminToken;
        private String studentToken;
        private String student2Token;
        private String staffToken;

        private Long adminId;
        private Long studentId;
        private Long student2Id;
        private Long staffId;

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
                adminId = userRepository.save(admin).getId();

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
                studentId = userRepository.save(student).getId();

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
                student2Id = userRepository.save(student2).getId();

                InstitutionalStaff staff = InstitutionalStaff.builder()
                                .email("staff@utez.edu.mx")
                                .password(encoder.encode("Admin123!"))
                                .firstName("Staff")
                                .lastName("Member")
                                .birthDate(LocalDate.of(1990, 3, 20))
                                .phoneNumber("+525512340004")
                                .employeeNumber("IN-001")
                                .createdBy("admin@utez.edu.mx")
                                .build();
                staffId = userRepository.save(staff).getId();

        adminToken = authService.login(new LoginRequest("admin@utez.edu.mx", "Admin123!"), "127.0.0.1").accessToken();
                studentToken = authService.login(new LoginRequest("student@utez.edu.mx", "Admin123!"), "127.0.0.1")
                                .accessToken();
                student2Token = authService.login(new LoginRequest("student2@utez.edu.mx", "Admin123!"), "127.0.0.1")
                                .accessToken();
        staffToken = authService.login(new LoginRequest("staff@utez.edu.mx", "Admin123!"), "127.0.0.1").accessToken();
        }

        // ══════════════════════════════════════════════════════════
        // GET /users (admin only)
        // ══════════════════════════════════════════════════════════

        @Test
        void getUsers_asAdmin_returns200Page() throws Exception {

                mvc.perform(get("/users")
                                .header("X-API-Version", VERSION)
                                .header("Authorization", "Bearer " + adminToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content", notNullValue()))
                                .andExpect(jsonPath("$.content.length()").value(4));
        }

        @Test
        void getUsers_return_deleted_users() throws Exception {

            userRepository.softDeleteById(student2Id);

            mvc.perform(get("/users")
                            .header("X-API-Version", VERSION)
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", notNullValue()))
                    .andExpect(jsonPath("$.content.length()").value(4))
                    .andReturn();
        }

        @Test
        void getUsers_asStudent_returns403() throws Exception {
                mvc.perform(get("/users")
                                .header("X-API-Version", VERSION)
                                .header("Authorization", "Bearer " + studentToken))
                                .andExpect(status().isForbidden());
        }

        @Test
        void getUsers_unauthenticated_returns403() throws Exception {
                mvc.perform(get("/users")
                                .header("X-API-Version", VERSION))
                                .andExpect(status().isForbidden());
        }

        @Test
        void getUsers_withSearchQuery_filtersResults() throws Exception {
                mvc.perform(get("/users")
                                .header("X-API-Version", VERSION)
                                .header("Authorization", "Bearer " + adminToken)
                .param("q", "student@utez.edu.mx"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content[0].email").value("student@utez.edu.mx"));
        }

        @Test
        void getUsers_withUserTypeFilter_filtersStudents() throws Exception {
                mvc.perform(get("/users")
                                .header("X-API-Version", VERSION)
                                .header("Authorization", "Bearer " + adminToken)
                                .param("userTypes", "STUDENT"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content[*].role").value(org.hamcrest.Matchers.everyItem(
                                                org.hamcrest.Matchers.is("STUDENT"))));
        }

        @Test
        void getUsers_showModeDeleted_returnsOnlyDeletedUsers() throws Exception {
                userRepository.softDeleteById(studentId);

                mvc.perform(get("/users")
                                .header("X-API-Version", VERSION)
                                .header("Authorization", "Bearer " + adminToken)
                                .param("showMode", "INACTIVE"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content", hasSize(1)))
                                .andExpect(jsonPath("$.content[0].email").value("student@utez.edu.mx"));
        }

        // ══════════════════════════════════════════════════════════
        // GET /users/{id} (APPLICANT or same user via @PostAuthorize)
        // ══════════════════════════════════════════════════════════

        @Test
        void getUserById_asAdmin_returns200() throws Exception {
                mvc.perform(get("/users/" + studentId)
                                .header("X-API-Version", VERSION)
                                .header("Authorization", "Bearer " + adminToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(studentId))
                                .andExpect(jsonPath("$.email").value("student@utez.edu.mx"));
        }

        @Test
        void getUserById_asOwnerStudent_returns200() throws Exception {
                mvc.perform(get("/users/" + studentId)
                                .header("X-API-Version", VERSION)
                                .header("Authorization", "Bearer " + studentToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(studentId));
        }

        @Test
        void getUserById_asOtherStudent_returns403() throws Exception {
                // student2 tries to read student1's profile → @PostAuthorize should deny it
                mvc.perform(get("/users/" + studentId)
                                .header("X-API-Version", VERSION)
                                .header("Authorization", "Bearer " + student2Token))
                                .andExpect(status().isForbidden());
        }

        @Test
        void getUserById_notFound_returns404() throws Exception {
                mvc.perform(get("/users/999999")
                                .header("X-API-Version", VERSION)
                                .header("Authorization", "Bearer " + adminToken))
                                .andExpect(status().isNotFound());
        }

        @Test
        void getUserById_unauthenticated_returns403() throws Exception {
                mvc.perform(get("/users/" + studentId)
                                .header("X-API-Version", VERSION))
                                .andExpect(status().isForbidden());
        }

        // ══════════════════════════════════════════════════════════
        // GET /users/lookup (admin only via @PostAuthorize)
        // ══════════════════════════════════════════════════════════

        @Test
        void lookupUser_byEmail_asAdmin_returns200() throws Exception {
                mvc.perform(get("/users/lookup")
                                .header("X-API-Version", VERSION)
                                .header("Authorization", "Bearer " + adminToken)
                                .param("identifier", "student@utez.edu.mx"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.email").value("student@utez.edu.mx"));
        }

        @Test
        void lookupUser_byPhone_asAdmin_returns200() throws Exception {
                mvc.perform(get("/users/lookup")
                                .header("X-API-Version", VERSION)
                                .header("Authorization", "Bearer " + adminToken)
                                .param("identifier", "+525512340002"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.phoneNumber", notNullValue()));
        }

        @Test
        void lookupUser_notFound_returns404() throws Exception {
                mvc.perform(get("/users/lookup")
                                .header("X-API-Version", VERSION)
                                .header("Authorization", "Bearer " + adminToken)
                                .param("identifier", "nobody@utez.edu.mx"))
                                .andExpect(status().isNotFound());
        }

        @Test
        void lookupUser_asStudent_lookupOwnEmail_returns200() throws Exception {
                // @PostAuthorize allows when the returned user id matches the current user
                mvc.perform(get("/users/lookup")
                                .header("X-API-Version", VERSION)
                                .header("Authorization", "Bearer " + studentToken)
                                .param("identifier", "student@utez.edu.mx"))
                                .andExpect(status().isOk());
        }

        @Test
        void lookupUser_asStudent_lookupOtherEmail_returns403() throws Exception {
                mvc.perform(get("/users/lookup")
                                .header("X-API-Version", VERSION)
                                .header("Authorization", "Bearer " + studentToken)
                                .param("identifier", "student2@utez.edu.mx"))
                                .andExpect(status().isForbidden());
        }

        // ══════════════════════════════════════════════════════════
        // PATCH /users/{id} (admin or self — service enforced)
        // ══════════════════════════════════════════════════════════

        private static final String VALID_UPDATE_BODY = """
                        {
                          "phoneNumber": "5599990001",
                          "firstName": "Actualizado",
                          "lastName": "Nombre",
                          "birthDate": "2000-01-01"
                        }
                        """;

        @Test
        void updateCommonInfo_asAdmin_returns200() throws Exception {
                mvc.perform(patch("/users/" + studentId)
                                .header("X-API-Version", VERSION)
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(VALID_UPDATE_BODY))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.firstName").value("Actualizado"));
        }

        @Test
        void updateCommonInfo_asOwner_returns200() throws Exception {
                mvc.perform(patch("/users/" + studentId)
                                .header("X-API-Version", VERSION)
                                .header("Authorization", "Bearer " + studentToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(VALID_UPDATE_BODY))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.firstName").value("Actualizado"));
        }

        @Test
        void updateCommonInfo_asOtherStudent_returns403() throws Exception {
                mvc.perform(patch("/users/" + studentId)
                                .header("X-API-Version", VERSION)
                                .header("Authorization", "Bearer " + student2Token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(VALID_UPDATE_BODY))
                                .andExpect(status().isForbidden());
        }

        @Test
        void updateCommonInfo_userNotFound_returns404() throws Exception {
                mvc.perform(patch("/users/999999")
                                .header("X-API-Version", VERSION)
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(VALID_UPDATE_BODY))
                                .andExpect(status().isNotFound());
        }

        @Test
        void updateCommonInfo_duplicatePhone_returns409() throws Exception {
                // Try to set the phone to one already owned by another user (student2)
                String body = """
                                {
                                  "phoneNumber": "+525512340003",
                                  "firstName": "Student",
                                  "lastName": "One",
                                  "birthDate": "2000-01-01"
                                }
                                """;
                mvc.perform(patch("/users/" + studentId)
                                .header("X-API-Version", VERSION)
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                                .andExpect(status().isConflict());
        }

        @Test
        void updateCommonInfo_invalidBody_returns400() throws Exception {
                mvc.perform(patch("/users/" + studentId)
                                .header("X-API-Version", VERSION)
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{}"))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.title").value("Validation failed"));
        }

        // ══════════════════════════════════════════════════════════
        // PUT /users/{id}/email (admin only)
        // ══════════════════════════════════════════════════════════

        @Test
        void updateEmail_asAdmin_returns200() throws Exception {
                String body = """
                                {"email": "updated.student@utez.edu.mx"}
                                """;
                mvc.perform(put("/users/" + studentId + "/email")
                                .header("X-API-Version", VERSION)
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.email").value("updated.student@utez.edu.mx"));
        }

        @Test
        void updateEmail_asStudent_returns403() throws Exception {
                String body = """
                                {"email": "updated.student@utez.edu.mx"}
                                """;
                mvc.perform(put("/users/" + studentId + "/email")
                                .header("X-API-Version", VERSION)
                                .header("Authorization", "Bearer " + studentToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                                .andExpect(status().isForbidden());
        }

        @Test
        void updateEmail_duplicateEmail_returns409() throws Exception {
                // student2's email already belongs to them
                String body = """
                                {"email": "student2@utez.edu.mx"}
                                """;
                mvc.perform(put("/users/" + studentId + "/email")
                                .header("X-API-Version", VERSION)
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                                .andExpect(status().isConflict());
        }

        @Test
        void updateEmail_invalidDomain_returns400() throws Exception {
                String body = """
                                {"email": "bad@gmail.com"}
                                """;
                mvc.perform(put("/users/" + studentId + "/email")
                                .header("X-API-Version", VERSION)
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.title").value("Validation failed"));
        }

        @Test
        void updateEmail_notFound_returns404() throws Exception {
                String body = """
                                {"email": "ghost@utez.edu.mx"}
                                """;
                mvc.perform(put("/users/999999/email")
                                .header("X-API-Version", VERSION)
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                                .andExpect(status().isNotFound());
        }

        // ══════════════════════════════════════════════════════════
        // PUT /students/{id}/registration-number (admin only)
        // ══════════════════════════════════════════════════════════

        @Test
        void updateRegNumber_asAdmin_returns200() throws Exception {
                String body = """
                                {"registrationNumber": "20221ds999"}
                                """;
                mvc.perform(put("/students/" + studentId + "/registration-number")
                                .header("X-API-Version", VERSION)
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.registrationNumber").value("20221ds999"));
        }

        @Test
        void updateRegNumber_asStudent_returns403() throws Exception {
                String body = """
                                {"registrationNumber": "20221ds999"}
                                """;
                mvc.perform(put("/students/" + studentId + "/registration-number")
                                .header("X-API-Version", VERSION)
                                .header("Authorization", "Bearer " + studentToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                                .andExpect(status().isForbidden());
        }

        @Test
        void updateRegNumber_duplicateRegNumber_returns409() throws Exception {
                // "20201ds003" already belongs to student2
                String body = """
                                {"registrationNumber": "20201ds003"}
                                """;
                mvc.perform(put("/students/" + studentId + "/registration-number")
                                .header("X-API-Version", VERSION)
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                                .andExpect(status().isConflict());
        }

        @Test
        void updateRegNumber_invalidFormat_returns400() throws Exception {
                String body = """
                                {"registrationNumber": "BADFORMAT"}
                                """;
                mvc.perform(put("/students/" + studentId + "/registration-number")
                                .header("X-API-Version", VERSION)
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.title").value("Validation failed"));
        }

        @Test
        void updateRegNumber_studentNotFound_returns404() throws Exception {
                String body = """
                                {"registrationNumber": "20221ds999"}
                                """;
                mvc.perform(put("/students/999999/registration-number")
                                .header("X-API-Version", VERSION)
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                                .andExpect(status().isNotFound());
        }

        // ══════════════════════════════════════════════════════════
        // PUT /institutional-staff/{id}/employee-number (admin only)
        // ══════════════════════════════════════════════════════════

        @Test
        void updateEmpNumber_asAdmin_returns200() throws Exception {
                String body = """
                                {"employeeNumber": "IN-999"}
                                """;
                mvc.perform(put("/institutional-staff/" + staffId + "/employee-number")
                                .header("X-API-Version", VERSION)
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.employeeNumber").value("IN-999"));
        }

        @Test
        void updateEmpNumber_asStudent_returns403() throws Exception {
                String body = """
                                {"employeeNumber": "IN-999"}
                                """;
                mvc.perform(put("/institutional-staff/" + staffId + "/employee-number")
                                .header("X-API-Version", VERSION)
                                .header("Authorization", "Bearer " + studentToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                                .andExpect(status().isForbidden());
        }

        @Test
        void updateEmpNumber_blankEmployeeNumber_returns400() throws Exception {
                String body = """
                                {"employeeNumber": ""}
                                """;
                mvc.perform(put("/institutional-staff/" + staffId + "/employee-number")
                                .header("X-API-Version", VERSION)
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.title").value("Validation failed"));
        }

        @Test
        void updateEmpNumber_staffNotFound_returns404() throws Exception {
                String body = """
                                {"employeeNumber": "IN-999"}
                                """;
                mvc.perform(put("/institutional-staff/999999/employee-number")
                                .header("X-API-Version", VERSION)
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body))
                                .andExpect(status().isNotFound());
        }

        // ══════════════════════════════════════════════════════════
        // DELETE /users/{id} (admin only — soft delete)
        // ══════════════════════════════════════════════════════════

        @Test
        void deactivateUser_asAdmin_returns204() throws Exception {
                mvc.perform(delete("/users/" + studentId)
                                .header("X-API-Version", VERSION)
                                .header("Authorization", "Bearer " + adminToken))
                                .andExpect(status().isNoContent());
        }

        @Test
        void deactivateUser_asStudent_returns403() throws Exception {
                mvc.perform(delete("/users/" + studentId)
                                .header("X-API-Version", VERSION)
                                .header("Authorization", "Bearer " + studentToken))
                                .andExpect(status().isForbidden());
        }

        @Test
        void deactivateUser_notFound_returns404() throws Exception {
                mvc.perform(delete("/users/999999")
                                .header("X-API-Version", VERSION)
                                .header("Authorization", "Bearer " + adminToken))
                                .andExpect(status().isNotFound());
        }

        // ══════════════════════════════════════════════════════════
        // PATCH /users/{id}/restore (admin only)
        // ══════════════════════════════════════════════════════════

        @Test
        void restoreUser_asAdmin_returns204() throws Exception {
                // First soft-delete
                userRepository.softDeleteById(studentId);

                mvc.perform(patch("/users/" + studentId + "/restore")
                                .header("X-API-Version", VERSION)
                                .header("Authorization", "Bearer " + adminToken))
                                .andExpect(status().isNoContent());
        }

        @Test
        void restoreUser_asStudent_returns403() throws Exception {
                userRepository.softDeleteById(studentId);

                mvc.perform(patch("/users/" + studentId + "/restore")
                                .header("X-API-Version", VERSION)
                                .header("Authorization", "Bearer " + studentToken))
                                .andExpect(status().isForbidden());
        }

        @Test
        void restoreUser_notFound_returns404() throws Exception {
                mvc.perform(patch("/users/999999/restore")
                                .header("X-API-Version", VERSION)
                                .header("Authorization", "Bearer " + adminToken))
                                .andExpect(status().isNotFound());
        }

        @Test
        void updatePassword_returnsNewToken() throws Exception {
            PasswordUpdateRequest request = PasswordUpdateRequest.builder()
                    .oldPassword("Admin123!")
                    .newPassword("Student123!")
                    .build();
            mvc.perform(patch("/users/me/password")
                            .header("X-API-Version", VERSION)
                            .header("Authorization", "Bearer " + studentToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.accessToken").exists())
                    .andExpect(jsonPath("$.refreshToken").exists());
        }


        @Test
        void updatePassword_returnsValidTokens() throws Exception {
            PasswordUpdateRequest request = PasswordUpdateRequest.builder()
                    .oldPassword("Admin123!")
                    .newPassword("Student123!")
                    .build();


            String returnedToken  = mapper.readTree(mvc.perform(patch("/users/me/password")
                            .header("X-API-Version", VERSION)
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(mapper.writeValueAsString(request)))
                    .andReturn().getResponse().getContentAsString()).get("accessToken").asString();

            mvc.perform(get("/users/me")
                    .header("X-API-Version", VERSION)
                    .header("Authorization", "Bearer " + returnedToken))
                    .andExpect(status().isOk());
        }

        @Test
        void updatePassword_invalidatesOldToken() throws Exception {
            PasswordUpdateRequest request = PasswordUpdateRequest.builder()
                    .oldPassword("Admin123!")
                    .newPassword("Student123!")
                    .build();


            mvc.perform(patch("/users/me/password")
                                    .header("X-API-Version", VERSION)
                                    .header("Authorization", "Bearer " + adminToken)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .content(mapper.writeValueAsString(request)));

            mvc.perform(get("/users/me")
                            .header("X-API-Version", VERSION)
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isForbidden());
        }

    @Test
    void updatePassword_updatesPassword() throws Exception {
        PasswordUpdateRequest request = PasswordUpdateRequest.builder()
                .oldPassword("Admin123!")
                .newPassword("Student123!")
                .build();


        mvc.perform(patch("/users/me/password")
                .header("X-API-Version", VERSION)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(request)));

        LoginRequest loginRequest = new LoginRequest("admin@utez.edu.mx", "Student123!");
        mvc.perform(post("/auth/login")
                        .header("X-API-Version", VERSION)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk());
    }
}
