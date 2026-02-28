package dev.spiffocode.sigesapi.integration.reservables;

import dev.spiffocode.sigesapi.FlushedIntegrationTest;
import dev.spiffocode.sigesapi.IntegrationTestClass;
import dev.spiffocode.sigesapi.auth.application.service.BearerAuthService;
import dev.spiffocode.sigesapi.auth.presentation.dto.LoginRequest;
import dev.spiffocode.sigesapi.reservables.domain.model.Building;
import dev.spiffocode.sigesapi.reservables.domain.model.ReservableStatus;
import dev.spiffocode.sigesapi.reservables.domain.model.Space;
import dev.spiffocode.sigesapi.reservables.domain.model.SpaceType;
import dev.spiffocode.sigesapi.reservables.domain.repository.BuildingRepository;
import dev.spiffocode.sigesapi.reservables.domain.repository.SpaceRepository;
import dev.spiffocode.sigesapi.reservables.domain.repository.SpaceTypeRepository;
import dev.spiffocode.sigesapi.reservables.presentation.dto.AvailabilitySlotRegisterDto;
import dev.spiffocode.sigesapi.reservables.presentation.dto.SpaceRegisterDto;
import dev.spiffocode.sigesapi.reservables.presentation.dto.SpaceUpdateDto;
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

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@IntegrationTestClass
public class SpaceIT extends FlushedIntegrationTest {

        @Autowired
        MockMvc mvc;
        @Autowired
        ObjectMapper mapper;
        @Autowired
        UserRepository userRepository;
        @Autowired
        SpaceRepository spaceRepository;
        @Autowired
        BuildingRepository buildingRepository;
        @Autowired
        SpaceTypeRepository spaceTypeRepository;
        @Autowired
        PasswordEncoder encoder;
        @Autowired
        BearerAuthService authService;

        private static final String API = "/spaces";
        private static final String VERSION = "1.0.0";

        private String adminToken;
        private String studentToken;
        private Building testBuilding;
        private SpaceType testSpaceType;

        @BeforeEach
        void setup() {
                spaceRepository.deleteAll();
                spaceTypeRepository.deleteAll();
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
        }

        private SpaceRegisterDto createValidDto() {
                AvailabilitySlotRegisterDto slot = AvailabilitySlotRegisterDto.builder()
                                .startTime(LocalTime.of(8, 0))
                                .endTime(LocalTime.of(20, 0))
                                .daysOfWeek(Set.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                                                DayOfWeek.THURSDAY,
                                                DayOfWeek.FRIDAY))
                                .build();

                return SpaceRegisterDto.builder()
                                .name("Auditorio A")
                                .description("Espacio para eventos grandes")
                                .studentsAvailable(true)
                                .buildingId(testBuilding.getId())
                                .spaceTypeId(testSpaceType.getId())
                                .capacity(100)
                                .bookInAdvanceDuration(Duration.ZERO)
                                .availability(List.of(slot))
                                .build();
        }

        @Test
        void searchSpaces_unauthenticated_returns403() throws Exception {
                mvc.perform(get(API).header("X-API-Version", VERSION))
                                .andExpect(status().isForbidden());
        }

        @Test
        void searchSpaces_authenticated_returns200() throws Exception {
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

                mvc.perform(get(API)
                                .header("X-API-Version", VERSION)
                                .header("Authorization", "Bearer " + studentToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content").isArray())
                                .andExpect(jsonPath("$.content[0].name").value("Aula 101"));
        }

        @Test
        void getSpace_authenticated_returns200() throws Exception {
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
                                .andExpect(jsonPath("$.name").value("Laboratorio A"));
        }

        @Test
        void registerSpace_asAdmin_returns201() throws Exception {
                SpaceRegisterDto dto = createValidDto();

                mvc.perform(post(API)
                                .header("X-API-Version", VERSION)
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(dto)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.name").value("Auditorio A"))
                                .andExpect(header().exists("Location"));
        }

        @Test
        void registerSpace_asStudent_returns403() throws Exception {
                SpaceRegisterDto dto = createValidDto();

                mvc.perform(post(API)
                                .header("X-API-Version", VERSION)
                                .header("Authorization", "Bearer " + studentToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(dto)))
                                .andExpect(status().isForbidden());
        }

        @Test
        void updateSpace_asAdmin_returns200() throws Exception {
                Space s = Space.builder()
                                .name("Old Space")
                                .description("Needs update")
                                .studentsAvailable(true)
                                .building(testBuilding)
                                .type(testSpaceType)
                                .capacity(10)
                                .bookInAdvance(Duration.ZERO)
                                .status(ReservableStatus.AVAILABLE)
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .createdBy("system")
                                .build();
                s = spaceRepository.save(s);

                SpaceUpdateDto dto = SpaceUpdateDto.builder()
                                .name("New Space")
                                .description("Updated!")
                                .studentsAvailable(false)
                                .buildingId(testBuilding.getId())
                                .spaceTypeId(testSpaceType.getId())
                                .capacity(15)
                                .bookInAdvanceDuration(Duration.ZERO)
                                .build();

                mvc.perform(put(API + "/" + s.getId())
                                .header("X-API-Version", VERSION)
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(dto)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.name").value("New Space"))
                                .andExpect(jsonPath("$.capacity").value(15));
        }

        @Test
        void deactivateSpace_asAdmin_returns204() throws Exception {
                Space s = Space.builder()
                                .name("Delete Me")
                                .description("Bye")
                                .studentsAvailable(true)
                                .building(testBuilding)
                                .type(testSpaceType)
                                .capacity(5)
                                .bookInAdvance(Duration.ZERO)
                                .status(ReservableStatus.AVAILABLE)
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .createdBy("system")
                                .build();
                s = spaceRepository.save(s);

                mvc.perform(patch(API + "/" + s.getId() + "/deactivate")
                                .header("X-API-Version", VERSION)
                                .header("Authorization", "Bearer " + adminToken))
                                .andExpect(status().isNoContent());
        }

        @Test
        void activateSpace_asAdmin_returns204() throws Exception {
                Space s = Space.builder()
                                .name("Activate Me")
                                .description("Hi")
                                .studentsAvailable(true)
                                .building(testBuilding)
                                .type(testSpaceType)
                                .capacity(5)
                                .bookInAdvance(Duration.ZERO)
                                .status(ReservableStatus.AVAILABLE)
                                .createdAt(LocalDateTime.now())
                                .updatedAt(LocalDateTime.now())
                                .createdBy("system")
                                .build();
                s = spaceRepository.save(s);
                spaceRepository.softDeleteById(s.getId());

                mvc.perform(patch(API + "/" + s.getId() + "/activate")
                                .header("X-API-Version", VERSION)
                                .header("Authorization", "Bearer " + adminToken))
                                .andExpect(status().isNoContent());
        }
}
