package dev.spiffocode.sigesapi.integration.reservables;

import dev.spiffocode.sigesapi.FlushedIntegrationTest;
import dev.spiffocode.sigesapi.IntegrationTestClass;
import dev.spiffocode.sigesapi.auth.application.service.BearerAuthService;
import dev.spiffocode.sigesapi.auth.presentation.dto.LoginRequest;
import dev.spiffocode.sigesapi.reservables.domain.model.SpaceType;
import dev.spiffocode.sigesapi.reservables.domain.repository.SpaceTypeRepository;
import dev.spiffocode.sigesapi.reservables.presentation.dto.SpaceTypeRegisterDto;
import dev.spiffocode.sigesapi.reservables.presentation.dto.SpaceTypeUpdateDto;
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
public class SpaceTypeIT extends FlushedIntegrationTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper mapper;
    @Autowired
    UserRepository userRepository;
    @Autowired
    SpaceTypeRepository spaceTypeRepository;
    @Autowired
    PasswordEncoder encoder;
    @Autowired
    BearerAuthService authService;

    private static final String API = "/spacetypes";
    private static final String VERSION = "1.0.0";

    private String adminToken;
    private String studentToken;

    @BeforeEach
    void setup() {
        spaceTypeRepository.deleteAll();
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
    void getAllSpaceTypes_unauthenticated_returns403() throws Exception {
        mvc.perform(get(API).header("X-API-Version", VERSION))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllSpaceTypes_authenticated_returns200() throws Exception {
        SpaceType st = SpaceType.builder()
                .name("Aula")
                .description("Salón de clases")
                .build();
        spaceTypeRepository.save(st);

        mvc.perform(get(API)
                .header("X-API-Version", VERSION)
                .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Aula"));
    }

    @Test
    void getSpaceType_authenticated_returns200() throws Exception {
        SpaceType st = SpaceType.builder()
                .name("Laboratorio")
                .description("Laboratorio de cómputo")
                .build();
        st = spaceTypeRepository.save(st);

        mvc.perform(get(API + "/" + st.getId())
                .header("X-API-Version", VERSION)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Laboratorio"));
    }

    @Test
    void registerSpaceType_asAdmin_returns201() throws Exception {
        SpaceTypeRegisterDto dto = SpaceTypeRegisterDto.builder()
                .name("Auditorio")
                .description("Espacio para eventos")
                .build();

        mvc.perform(post(API)
                .header("X-API-Version", VERSION)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Auditorio"))
                .andExpect(header().exists("Location"));
    }

    @Test
    void registerSpaceType_asStudent_returns403() throws Exception {
        SpaceTypeRegisterDto dto = SpaceTypeRegisterDto.builder()
                .name("Oficina")
                .description("Espacio administrativo")
                .build();

        mvc.perform(post(API)
                .header("X-API-Version", VERSION)
                .header("Authorization", "Bearer " + studentToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateSpaceType_asAdmin_returns200() throws Exception {
        SpaceType st = SpaceType.builder()
                .name("Cancha")
                .description("Espacio deportivo")
                .build();
        st = spaceTypeRepository.save(st);

        SpaceTypeUpdateDto dto = new SpaceTypeUpdateDto("Cancha de Futbol", "Espacio deportivo actualizado");

        mvc.perform(put(API + "/" + st.getId())
                .header("X-API-Version", VERSION)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Cancha de Futbol"));
    }

    @Test
    void deactivateSpaceType_asAdmin_returns204() throws Exception {
        SpaceType st = SpaceType.builder()
                .name("Baños")
                .description("Servicios sanitarios")
                .build();
        st = spaceTypeRepository.save(st);

        mvc.perform(patch(API + "/" + st.getId() + "/deactivate")
                .header("X-API-Version", VERSION)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void activateSpaceType_asAdmin_returns204() throws Exception {
        SpaceType st = SpaceType.builder()
                .name("Cafetería")
                .description("Venta de alimentos")
                .build();
        st = spaceTypeRepository.save(st);
        spaceTypeRepository.softDeleteById(st.getId());

        mvc.perform(patch(API + "/" + st.getId() + "/activate")
                .header("X-API-Version", VERSION)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }
}
