package dev.spiffocode.sigesapi.integration.reservables;

import dev.spiffocode.sigesapi.FlushedIntegrationTest;
import dev.spiffocode.sigesapi.IntegrationTestClass;
import dev.spiffocode.sigesapi.auth.application.service.BearerAuthService;
import dev.spiffocode.sigesapi.auth.presentation.dto.LoginRequest;
import dev.spiffocode.sigesapi.reservables.domain.model.EquipmentType;
import dev.spiffocode.sigesapi.reservables.domain.repository.EquipmentTypeRepository;
import dev.spiffocode.sigesapi.reservables.presentation.dto.EquipmentTypeRegisterDto;
import dev.spiffocode.sigesapi.reservables.presentation.dto.EquipmentTypeUpdateDto;
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
public class EquipmentTypeIT extends FlushedIntegrationTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper mapper;
    @Autowired
    UserRepository userRepository;
    @Autowired
    EquipmentTypeRepository equipmentTypeRepository;
    @Autowired
    PasswordEncoder encoder;
    @Autowired
    BearerAuthService authService;

    private static final String API = "/equipment-types";
    private static final String VERSION = "1.0.0";

    private String adminToken;
    private String studentToken;

    @BeforeEach
    void setup() {
        equipmentTypeRepository.deleteAll();
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

        Student student = Student.builder()
                .email("student@siges.com")
                .password(encoder.encode("password123"))
                .firstName("Student")
                .lastName("User")
                .birthDate(LocalDate.of(2000, 1, 1))
                .phoneNumber("+525555555556")
                .registrationNumber("STU001")
                .createdBy("system")
                .build();
        userRepository.save(student);

        adminToken = authService.login(new LoginRequest("admin@siges.com", "password123"), "127.0.0.1").accessToken();
        studentToken = authService.login(new LoginRequest("student@siges.com", "password123"), "127.0.0.1")
                .accessToken();
    }

    @Test
    void getAllEquipmentTypes_unauthenticated_returns403() throws Exception {
        mvc.perform(get(API).header("X-API-Version", VERSION))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllEquipmentTypes_authenticated_returns200() throws Exception {
        EquipmentType et = EquipmentType.builder()
                .name("Projector")
                .description("Multimedia projector")
                .build();
        equipmentTypeRepository.save(et);

        mvc.perform(get(API)
                .header("X-API-Version", VERSION)
                .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Projector"));
    }

    @Test
    void getEquipmentType_authenticated_returns200() throws Exception {
        EquipmentType et = EquipmentType.builder()
                .name("Laptop")
                .description("Portable computer")
                .build();
        et = equipmentTypeRepository.save(et);

        mvc.perform(get(API + "/" + et.getId())
                .header("X-API-Version", VERSION)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Laptop"));
    }

    @Test
    void registerEquipmentType_asAdmin_returns201() throws Exception {
        EquipmentTypeRegisterDto dto = EquipmentTypeRegisterDto.builder()
                .name("Speaker")
                .description("Audio output device")
                .build();

        mvc.perform(post(API)
                .header("X-API-Version", VERSION)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Speaker"))
                .andExpect(header().exists("Location"));
    }

    @Test
    void registerEquipmentType_asStudent_returns403() throws Exception {
        EquipmentTypeRegisterDto dto = EquipmentTypeRegisterDto.builder()
                .name("Camera")
                .description("Digital camera")
                .build();

        mvc.perform(post(API)
                .header("X-API-Version", VERSION)
                .header("Authorization", "Bearer " + studentToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateEquipmentType_asAdmin_returns200() throws Exception {
        EquipmentType et = EquipmentType.builder()
                .name("Monitor")
                .description("Computer screen")
                .build();
        et = equipmentTypeRepository.save(et);

        EquipmentTypeUpdateDto dto = new EquipmentTypeUpdateDto("4K Monitor", "Updated description");

        mvc.perform(put(API + "/" + et.getId())
                .header("X-API-Version", VERSION)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("4K Monitor"));
    }

    @Test
    void deactivateEquipmentType_asAdmin_returns204() throws Exception {
        EquipmentType et = EquipmentType.builder()
                .name("Cable")
                .description("Connection cable")
                .build();
        et = equipmentTypeRepository.save(et);

        mvc.perform(patch(API + "/" + et.getId() + "/deactivate")
                .header("X-API-Version", VERSION)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void activateEquipmentType_asAdmin_returns204() throws Exception {
        EquipmentType et = EquipmentType.builder()
                .name("Keyboard")
                .description("Input device")
                .build();
        et = equipmentTypeRepository.save(et);
        equipmentTypeRepository.softDeleteById(et.getId());

        mvc.perform(patch(API + "/" + et.getId() + "/activate")
                .header("X-API-Version", VERSION)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }
}
