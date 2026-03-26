package dev.spiffocode.sigesapi.integration.reservables;

import dev.spiffocode.sigesapi.FlushedIntegrationTest;
import dev.spiffocode.sigesapi.IntegrationTestClass;
import dev.spiffocode.sigesapi.auth.application.service.BearerAuthService;
import dev.spiffocode.sigesapi.auth.presentation.dto.LoginRequest;
import dev.spiffocode.sigesapi.reservables.domain.model.*;
import dev.spiffocode.sigesapi.reservables.domain.repository.BuildingRepository;
import dev.spiffocode.sigesapi.reservables.domain.repository.EquipmentRepository;
import dev.spiffocode.sigesapi.reservables.domain.repository.EquipmentTypeRepository;
import dev.spiffocode.sigesapi.reservables.domain.repository.SpaceRepository;
import dev.spiffocode.sigesapi.reservables.domain.repository.SpaceTypeRepository;
import dev.spiffocode.sigesapi.users.domain.model.Admin;
import dev.spiffocode.sigesapi.users.domain.model.Student;
import dev.spiffocode.sigesapi.users.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@IntegrationTestClass
public class ReservableIT extends FlushedIntegrationTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper mapper;
    @Autowired
    UserRepository userRepository;
    @Autowired
    SpaceRepository spaceRepository;
    @Autowired
    EquipmentRepository equipmentRepository;
    @Autowired
    BuildingRepository buildingRepository;
    @Autowired
    SpaceTypeRepository spaceTypeRepository;
    @Autowired
    EquipmentTypeRepository equipmentTypeRepository;
    @Autowired
    PasswordEncoder encoder;
    @Autowired
    BearerAuthService authService;

    private static final String API = "/reservables";
    private static final String VERSION = "1.0.0";

    private String adminToken;
    private String studentToken;
    private Building testBuilding;
    private SpaceType testSpaceType;
    private EquipmentType testEquipmentType;

    @BeforeEach
    void setup() {
        spaceRepository.deleteAll();
        equipmentRepository.deleteAll();
        spaceTypeRepository.deleteAll();
        equipmentTypeRepository.deleteAll();
        buildingRepository.deleteAll();
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

        adminToken = authService.login(new LoginRequest("admin@siges.com", "password123"), "127.0.0.1")
                .accessToken();
        studentToken = authService.login(new LoginRequest("student@siges.com", "password123"), "127.0.0.1")
                .accessToken();

        testBuilding = Building.builder().name("Main Building").build();
        testBuilding = buildingRepository.save(testBuilding);

        testSpaceType = SpaceType.builder()
                .name("Aula")
                .description("Salón de clases")
                .build();
        testSpaceType = spaceTypeRepository.save(testSpaceType);

        testEquipmentType = EquipmentType.builder()
                .name("Proyector")
                .description("Proyector de video")
                .build();
        testEquipmentType = equipmentTypeRepository.save(testEquipmentType);
    }

    @Test
    void searchReservables_unauthenticated_returns403() throws Exception {
        mvc.perform(get(API).header("X-API-Version", VERSION))
                .andExpect(status().isForbidden());
    }

    @Test
    void searchReservables_authenticated_returnsBothTypes() throws Exception {
        Space s = Space.builder()
                .name("Aula 101")
                .description("Aula básica")
                .studentsAvailable(true)
                .building(testBuilding)
                .type(testSpaceType)
                .capacity(30)
                .bookInAdvance(Duration.ZERO)
                .status(ReservableStatus.AVAILABLE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .createdBy("system")
                .build();
        spaceRepository.save(s);

        Equipment e = Equipment.builder()
                .name("Epson X")
                .description("Proyector HD")
                .studentsAvailable(true)
                .building(testBuilding)
                .type(testEquipmentType)
                .inventoryItem(new InventoryItem("INV-123"))
                .status(ReservableStatus.AVAILABLE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .createdBy("system")
                .build();
        equipmentRepository.save(e);

        mvc.perform(get(API)
                .header("X-API-Version", VERSION)
                .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2));
    }

    @Test
    void getReservable_authenticated_space_returns200() throws Exception {
        Space s = Space.builder()
                .name("Laboratorio A")
                .description("Laboratorio de cómputo")
                .studentsAvailable(false)
                .building(testBuilding)
                .type(testSpaceType)
                .capacity(20)
                .bookInAdvance(Duration.ZERO)
                .status(ReservableStatus.AVAILABLE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .createdBy("system")
                .build();
        s = spaceRepository.save(s);

        mvc.perform(get(API + "/" + s.getId())
                .header("X-API-Version", VERSION)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Laboratorio A"))
                .andExpect(jsonPath("$.resourceType").value("SPACE"));
    }

    @Test
    void getReservable_authenticated_equipment_returns200() throws Exception {
        Equipment e = Equipment.builder()
                .name("Cable HDMI")
                .description("2 metros")
                .studentsAvailable(true)
                .building(testBuilding)
                .type(testEquipmentType)
                .inventoryItem(new InventoryItem("CAB-001"))
                .status(ReservableStatus.AVAILABLE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .createdBy("system")
                .build();
        e = equipmentRepository.save(e);

        mvc.perform(get(API + "/" + e.getId())
                .header("X-API-Version", VERSION)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Cable HDMI"))
                .andExpect(jsonPath("$.resourceType").value("EQUIPMENT"));
    }

    @Test
    void getReservable_notFound_returns404() throws Exception {
        mvc.perform(get(API + "/999999")
                .header("X-API-Version", VERSION)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }
}
