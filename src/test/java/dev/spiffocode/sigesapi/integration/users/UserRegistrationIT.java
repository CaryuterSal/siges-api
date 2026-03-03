package dev.spiffocode.sigesapi.integration.users;

import dev.spiffocode.sigesapi.FlushedIntegrationTest;
import dev.spiffocode.sigesapi.IntegrationTestClass;
import dev.spiffocode.sigesapi.auth.application.service.BearerAuthService;
import dev.spiffocode.sigesapi.auth.presentation.dto.LoginRequest;
import dev.spiffocode.sigesapi.users.domain.model.Admin;
import dev.spiffocode.sigesapi.users.domain.model.Student;
import dev.spiffocode.sigesapi.users.domain.repository.StudentRepository;
import dev.spiffocode.sigesapi.users.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@IntegrationTestClass
class UserRegistrationIT extends FlushedIntegrationTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper mapper;
    @Autowired
    UserRepository userRepository;
    @Autowired
    StudentRepository studentRepository;
    @Autowired
    PasswordEncoder encoder;
    @Autowired
    BearerAuthService authService;

    private static final String VERSION = "1.0.0";

    private String adminToken;
    private String studentToken;

    // ── valid registration payloads ─────────────────────────────────────────

    private static final String VALID_ADMIN_BODY = """
            {
              "email": "new.admin@utez.edu.mx",
              "phoneNumber": "5599887766",
              "firstName": "Nuevo",
              "lastName": "Admin",
              "birthDate": "1985-06-15"
            }
            """;

    private static final String VALID_STUDENT_BODY = """
            {
              "email": "new.student@utez.edu.mx",
              "phoneNumber": "5511223344",
              "firstName": "Nuevo",
              "lastName": "Estudiante",
              "birthDate": "2002-03-10",
              "registrationNumber": "20201ds001"
            }
            """;

    private static final String VALID_STAFF_BODY = """
            {
              "email": "new.staff@utez.edu.mx",
              "phoneNumber": "5566778899",
              "firstName": "Nuevo",
              "lastName": "Personal",
              "birthDate": "1990-09-20",
              "employeeNumber": "IN-999"
            }
            """;

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
                .lastName("Test")
                .birthDate(LocalDate.of(2000, 1, 1))
                .phoneNumber("+525512340002")
                .registrationNumber("20201ds002")
                .createdBy("admin@utez.edu.mx")
                .build();
        userRepository.save(student);

        adminToken = authService.login(new LoginRequest("admin@utez.edu.mx", "Admin123!"), "127.0.0.1").accessToken();
        studentToken = authService.login(new LoginRequest("student@utez.edu.mx", "Admin123!"), "127.0.0.1")
                .accessToken();
    }

    // ══════════════════════════════════════════════════════════
    // POST /admins
    // ══════════════════════════════════════════════════════════

    @Test
    void registerAdmin_asAdmin_returns201() throws Exception {
        mvc.perform(post("/admins")
                .header("X-API-Version", VERSION)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_ADMIN_BODY))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.email").value("new.admin@utez.edu.mx"))
                .andExpect(jsonPath("$.firstName").value("Nuevo"));
    }

    @Test
    void registerAdmin_asStudent_returns403() throws Exception {
        mvc.perform(post("/admins")
                .header("X-API-Version", VERSION)
                .header("Authorization", "Bearer " + studentToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_ADMIN_BODY))
                .andExpect(status().isForbidden());
    }

    @Test
    void registerAdmin_unauthenticated_returns403() throws Exception {
        mvc.perform(post("/admins")
                .header("X-API-Version", VERSION)
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_ADMIN_BODY))
                .andExpect(status().isForbidden());
    }

    @Test
    void registerAdmin_duplicateEmail_returns409() throws Exception {
        // Email already belongs to the existing admin
        String body = """
                {
                  "email": "admin@utez.edu.mx",
                  "phoneNumber": "5533330001",
                  "firstName": "Otro",
                  "lastName": "Admin",
                  "birthDate": "1985-06-15"
                }
                """;
        mvc.perform(post("/admins")
                .header("X-API-Version", VERSION)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void registerAdmin_duplicatePhone_returns409() throws Exception {
        // Phone already belongs to the existing admin
        String body = """
                {
                  "email": "another.admin@utez.edu.mx",
                  "phoneNumber": "+525512340001",
                  "firstName": "Otro",
                  "lastName": "Admin",
                  "birthDate": "1985-06-15"
                }
                """;
        mvc.perform(post("/admins")
                .header("X-API-Version", VERSION)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void registerAdmin_invalidEmail_domain_returns400() throws Exception {
        String body = """
                {
                  "email": "admin@gmail.com",
                  "phoneNumber": "5577441122",
                  "firstName": "Admin",
                  "lastName": "Invalid",
                  "birthDate": "1990-01-01"
                }
                """;
        mvc.perform(post("/admins")
                .header("X-API-Version", VERSION)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"));
    }

    @Test
    void registerAdmin_missingRequiredFields_returns400() throws Exception {
        mvc.perform(post("/admins")
                .header("X-API-Version", VERSION)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"));
    }

    @Test
    void registerAdmin_futureBirthDate_returns400() throws Exception {
        String body = """
                {
                  "email": "future@utez.edu.mx",
                  "phoneNumber": "5577441199",
                  "firstName": "Future",
                  "lastName": "Person",
                  "birthDate": "2099-01-01"
                }
                """;
        mvc.perform(post("/admins")
                .header("X-API-Version", VERSION)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"));
    }

    // ══════════════════════════════════════════════════════════
    // POST /students
    // ══════════════════════════════════════════════════════════

    @Test
    void registerStudent_asAdmin_returns201() throws Exception {
        mvc.perform(post("/students")
                .header("X-API-Version", VERSION)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_STUDENT_BODY))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.email").value("new.student@utez.edu.mx"))
                .andExpect(jsonPath("$.registrationNumber").value("20201ds001"));
    }

    @Test
    void registerStudent_asStudent_returns403() throws Exception {
        mvc.perform(post("/students")
                .header("X-API-Version", VERSION)
                .header("Authorization", "Bearer " + studentToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_STUDENT_BODY))
                .andExpect(status().isForbidden());
    }

    @Test
    void registerStudent_duplicateRegistrationNumber_returns409() throws Exception {
        // "20201ds002" is already used by the student seeded in @BeforeEach
        String body = """
                {
                  "email": "other.student@utez.edu.mx",
                  "phoneNumber": "5522334455",
                  "firstName": "Otro",
                  "lastName": "Estudiante",
                  "birthDate": "2003-01-01",
                  "registrationNumber": "20201ds002"
                }
                """;
        mvc.perform(post("/students")
                .header("X-API-Version", VERSION)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void registerStudent_invalidRegistrationNumberFormat_returns400() throws Exception {
        String body = """
                {
                  "email": "format.student@utez.edu.mx",
                  "phoneNumber": "5522334456",
                  "firstName": "Bad",
                  "lastName": "Format",
                  "birthDate": "2003-01-01",
                  "registrationNumber": "BADFORMAT"
                }
                """;
        mvc.perform(post("/students")
                .header("X-API-Version", VERSION)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"));
    }

    @Test
    void registerStudent_missingRegistrationNumber_returns400() throws Exception {
        String body = """
                {
                  "email": "missing.reg@utez.edu.mx",
                  "phoneNumber": "5544556677",
                  "firstName": "Sin",
                  "lastName": "Matrícula",
                  "birthDate": "2000-06-01"
                }
                """;
        mvc.perform(post("/students")
                .header("X-API-Version", VERSION)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"));
    }

    // ══════════════════════════════════════════════════════════
    // POST /institutional-staff
    // ══════════════════════════════════════════════════════════

    @Test
    void registerStaff_asAdmin_returns201() throws Exception {
        mvc.perform(post("/institutional-staff")
                .header("X-API-Version", VERSION)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_STAFF_BODY))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.email").value("new.staff@utez.edu.mx"))
                .andExpect(jsonPath("$.employeeNumber").value("IN-999"));
    }

    @Test
    void registerStaff_asStudent_returns403() throws Exception {
        mvc.perform(post("/institutional-staff")
                .header("X-API-Version", VERSION)
                .header("Authorization", "Bearer " + studentToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_STAFF_BODY))
                .andExpect(status().isForbidden());
    }

    @Test
    void registerStaff_unauthenticated_returns403() throws Exception {
        mvc.perform(post("/institutional-staff")
                .header("X-API-Version", VERSION)
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_STAFF_BODY))
                .andExpect(status().isForbidden());
    }

    @Test
    void registerStaff_blankEmployeeNumber_returns400() throws Exception {
        String body = """
                {
                  "email": "blank.emp@utez.edu.mx",
                  "phoneNumber": "5566778800",
                  "firstName": "Sin",
                  "lastName": "Número",
                  "birthDate": "1992-04-20",
                  "employeeNumber": ""
                }
                """;
        mvc.perform(post("/institutional-staff")
                .header("X-API-Version", VERSION)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"));
    }

    @Test
    void registerStaff_duplicateEmployeeNumber_returns409() throws Exception {
        // First, register a staff member
        mvc.perform(post("/institutional-staff")
                .header("X-API-Version", VERSION)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_STAFF_BODY))
                .andExpect(status().isCreated());

        // Then try to register another with the same employee number
        String body = """
                {
                  "email": "another.staff@utez.edu.mx",
                  "phoneNumber": "5566778811",
                  "firstName": "Otro",
                  "lastName": "Personal",
                  "birthDate": "1993-02-14",
                  "employeeNumber": "IN-999"
                }
                """;
        mvc.perform(post("/institutional-staff")
                .header("X-API-Version", VERSION)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isConflict());
    }
}
