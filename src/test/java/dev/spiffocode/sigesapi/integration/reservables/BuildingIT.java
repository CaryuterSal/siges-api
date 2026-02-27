package dev.spiffocode.sigesapi.integration.reservables;

import dev.spiffocode.sigesapi.IntegrationTestClass;
import dev.spiffocode.sigesapi.auth.application.service.BearerAuthService;
import dev.spiffocode.sigesapi.auth.presentation.dto.LoginRequest;
import dev.spiffocode.sigesapi.reservables.domain.model.Building;
import dev.spiffocode.sigesapi.reservables.domain.repository.BuildingRepository;
import dev.spiffocode.sigesapi.reservables.presentation.dto.BuildingRegisterDto;
import dev.spiffocode.sigesapi.reservables.presentation.dto.BuildingUpdateDto;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@IntegrationTestClass
public class BuildingIT {

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper mapper;
    @Autowired
    UserRepository userRepository;
    @Autowired
    BuildingRepository buildingRepository;
    @Autowired
    PasswordEncoder encoder;
    @Autowired
    BearerAuthService authService;

    private static final String API = "/buildings";
    private static final String VERSION = "1.0.0";

    private String adminToken;
    private String studentToken;

    @BeforeEach
    void setup() {
        buildingRepository.deleteAll();
        userRepository.deleteAll();

        Admin admin = Admin.builder()
                .email("admin@siges.com")
                .password(encoder.encode("password123"))
                .firstName("Admin")
                .lastName("User")
                .birthDate(LocalDate.of(1980, 1, 1))
                .phoneNumber("+525555555555")
                .createdBy("admin@siges.com")
                .build();

        userRepository.save(admin);

        Student student = Student.builder()
                .email("student@siges.com")
                .password(encoder.encode("password123"))
                .firstName("Student")
                .lastName("User")
                .birthDate(LocalDate.of(2000, 1, 1))
                .phoneNumber("+525555555556")
                .registrationNumber("STU001")
                .createdBy("admin@siges.com")
                .build();
        userRepository.save(student);

        adminToken = authService.login(new LoginRequest("admin@siges.com", "password123"), "127.0.0.1").accessToken();
        studentToken = authService.login(new LoginRequest("student@siges.com", "password123"), "127.0.0.1")
                .accessToken();
    }

    @Test
    void getAllBuildings_unauthenticated_returns403() throws Exception {
        mvc.perform(get(API).header("X-API-Version", VERSION))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllBuildings_authenticated_returns200() throws Exception {
        Building b = Building.builder().name("CEDIM").build();
        buildingRepository.save(b);

        mvc.perform(get(API)
                .header("X-API-Version", VERSION)
                .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("CEDIM"));
    }

    @Test
    void getBuilding_authenticated_returns200() throws Exception {
        Building b = Building.builder().name("CECADEC").build();
        b = buildingRepository.save(b);

        mvc.perform(get(API + "/" + b.getId())
                .header("X-API-Version", VERSION)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("CECADEC"));
    }

    @Test
    void registerBuilding_asAdmin_returns201() throws Exception {
        BuildingRegisterDto dto = new BuildingRegisterDto("Docencia 1");

        mvc.perform(post(API)
                .header("X-API-Version", VERSION)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Docencia 1"))
                .andExpect(header().exists("Location"));
    }

    @Test
    void registerBuilding_asStudent_returns403() throws Exception {
        BuildingRegisterDto dto = new BuildingRegisterDto("Docencia 2");

        mvc.perform(post(API)
                .header("X-API-Version", VERSION)
                .header("Authorization", "Bearer " + studentToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateBuilding_asAdmin_returns200() throws Exception {
        Building b = Building.builder().name("Rectoria").build();
        b = buildingRepository.save(b);

        BuildingUpdateDto dto = new BuildingUpdateDto("Rectoria Updated");

        mvc.perform(put(API + "/" + b.getId())
                .header("X-API-Version", VERSION)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Rectoria Updated"));
    }

    @Test
    void deactivateBuilding_asAdmin_returns204() throws Exception {
        Building b = Building.builder().name("CCyC").build();
        b = buildingRepository.save(b);

        mvc.perform(patch(API + "/" + b.getId() + "/deactivate")
                .header("X-API-Version", VERSION)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }
}
