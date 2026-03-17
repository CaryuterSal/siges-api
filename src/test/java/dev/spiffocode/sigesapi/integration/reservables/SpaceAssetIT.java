package dev.spiffocode.sigesapi.integration.reservables;

import dev.spiffocode.sigesapi.FlushedIntegrationTest;
import dev.spiffocode.sigesapi.IntegrationTestClass;
import dev.spiffocode.sigesapi.auth.application.service.BearerAuthService;
import dev.spiffocode.sigesapi.auth.presentation.dto.LoginRequest;
import dev.spiffocode.sigesapi.reservables.domain.model.Building;
import dev.spiffocode.sigesapi.reservables.domain.model.EquipmentType;
import dev.spiffocode.sigesapi.reservables.domain.model.Space;
import dev.spiffocode.sigesapi.reservables.domain.repository.BuildingRepository;
import dev.spiffocode.sigesapi.reservables.domain.repository.EquipmentTypeRepository;
import dev.spiffocode.sigesapi.reservables.domain.repository.SpaceAssetRepository;
import dev.spiffocode.sigesapi.reservables.domain.repository.SpaceRepository;
import dev.spiffocode.sigesapi.reservables.presentation.dto.SpaceAssetDto;
import dev.spiffocode.sigesapi.reservables.presentation.dto.SpaceAssetRegisterDto;
import dev.spiffocode.sigesapi.reservables.presentation.dto.SpaceAssetUpdateDto;
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
public class SpaceAssetIT extends FlushedIntegrationTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper mapper;
    @Autowired
    UserRepository userRepository;
    @Autowired
    BuildingRepository buildingRepository;
    @Autowired
    SpaceRepository spaceRepository;
    @Autowired
    EquipmentTypeRepository equipmentTypeRepository;
    @Autowired
    SpaceAssetRepository spaceAssetRepository;
    @Autowired
    PasswordEncoder encoder;
    @Autowired
    BearerAuthService authService;

    private static final String API = "/spaces";
    private static final String VERSION = "1.0.0";

    private String adminToken;
    private String studentToken;
    private Space testSpace;
    private EquipmentType testType;

    @BeforeEach
    void setup() {
        spaceAssetRepository.deleteAll();
        spaceRepository.deleteAll();
        buildingRepository.deleteAll();
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

        adminToken = authService.login(new LoginRequest("admin@siges.com", "password123"), "127.0.0.1")
                .accessToken();
        studentToken = authService.login(new LoginRequest("student@siges.com", "password123"), "127.0.0.1")
                .accessToken();

        Building building = Building.builder().name("Main Building").build();
        building = buildingRepository.save(building);

        testSpace = Space.builder().name("Lab 1").building(building).build();
        testSpace = spaceRepository.save(testSpace);

        testType = EquipmentType.builder().name("Electronics").description("Electronic equipment").build();
        testType = equipmentTypeRepository.save(testType);
    }

    private SpaceAssetRegisterDto createValidDto(String invNum) {
        return SpaceAssetRegisterDto.builder()
                .name("OLED TV")
                .description("Smart TV for Lab")
                .inventoryNum(invNum)
                .typeId(testType.getId())
                .build();
    }

    @Test
    void getSpaceAsset_authenticated_returns200() throws Exception {
        SpaceAssetDto asset = mapper.readValue(
                mvc.perform(post(API + "/" + testSpace.getId() + "/assets")
                        .header("X-API-Version", VERSION)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createValidDto("INV-100"))))
                        .andReturn().getResponse().getContentAsString(),
                dev.spiffocode.sigesapi.reservables.presentation.dto.SpaceAssetDto.class);

        mvc.perform(get(API + "/assets/" + asset.id())
                .header("X-API-Version", VERSION)
                .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("OLED TV"));
    }

    @Test
    void getSpaceAsset_notFound_returns404() throws Exception {
        mvc.perform(get(API + "/assets/999")
                .header("X-API-Version", VERSION)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void searchSpaceAssets_returns200() throws Exception {
        mvc.perform(post(API + "/" + testSpace.getId() + "/assets")
                .header("X-API-Version", VERSION)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(createValidDto("INV-101"))));

        mvc.perform(get(API + "/assets")
                .header("X-API-Version", VERSION)
                .header("Authorization", "Bearer " + studentToken)
                .param("searchQuery", "OLED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    void registerSpaceAsset_asAdmin_returns201() throws Exception {
        SpaceAssetRegisterDto dto = createValidDto("INV-201");

        mvc.perform(post(API + "/" + testSpace.getId() + "/assets")
                .header("X-API-Version", VERSION)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.inventoryNum").value("INV-201"))
                .andExpect(header().exists("Location"));
    }

    @Test
    void registerSpaceAsset_asStudent_returns403() throws Exception {
        SpaceAssetRegisterDto dto = createValidDto("INV-202");

        mvc.perform(post(API + "/" + testSpace.getId() + "/assets")
                .header("X-API-Version", VERSION)
                .header("Authorization", "Bearer " + studentToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void registerSpaceAsset_duplicateInventoryNum_returns409() throws Exception {
        SpaceAssetRegisterDto dto = createValidDto("INV-DUPE");
        mvc.perform(post(API + "/" + testSpace.getId() + "/assets")
                .header("X-API-Version", VERSION)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(dto)));

        mvc.perform(post(API + "/" + testSpace.getId() + "/assets")
                .header("X-API-Version", VERSION)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    @Test
    void updateSpaceAsset_returns200() throws Exception {
        SpaceAssetDto asset = mapper.readValue(
                mvc.perform(post(API + "/" + testSpace.getId() + "/assets")
                        .header("X-API-Version", VERSION)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createValidDto("INV-UP-1"))))
                        .andReturn().getResponse().getContentAsString(),
                dev.spiffocode.sigesapi.reservables.presentation.dto.SpaceAssetDto.class);

        SpaceAssetUpdateDto updateDto = SpaceAssetUpdateDto.builder()
                .name("Updated TV")
                .description("Better TV")
                .inventoryNum("INV-UP-2")
                .typeId(testType.getId())
                .build();

        mvc.perform(put(API + "/assets/" + asset.id())
                .header("X-API-Version", VERSION)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated TV"))
                .andExpect(jsonPath("$.inventoryNum").value("INV-UP-2"));
    }

    @Test
    void deactivateAndActivateSpaceAsset_returns204() throws Exception {
        SpaceAssetDto asset = mapper.readValue(
                mvc.perform(post(API + "/" + testSpace.getId() + "/assets")
                        .header("X-API-Version", VERSION)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(createValidDto("INV-ACT"))))
                        .andReturn().getResponse().getContentAsString(),
                dev.spiffocode.sigesapi.reservables.presentation.dto.SpaceAssetDto.class);

        mvc.perform(patch(API + "/assets/" + asset.id() + "/deactivate")
                .header("X-API-Version", VERSION)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        mvc.perform(patch(API + "/assets/" + asset.id() + "/activate")
                .header("X-API-Version", VERSION)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }
}
