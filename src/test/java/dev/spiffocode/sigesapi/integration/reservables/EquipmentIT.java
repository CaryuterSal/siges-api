package dev.spiffocode.sigesapi.integration.reservables;

import com.fasterxml.jackson.core.JsonProcessingException;
import dev.spiffocode.sigesapi.FlushedIntegrationTest;
import dev.spiffocode.sigesapi.IntegrationTestClass;
import dev.spiffocode.sigesapi.auth.application.service.BearerAuthService;
import dev.spiffocode.sigesapi.auth.presentation.dto.LoginRequest;
import dev.spiffocode.sigesapi.reservables.domain.model.Building;
import dev.spiffocode.sigesapi.reservables.domain.model.Equipment;
import dev.spiffocode.sigesapi.reservables.domain.model.InventoryItem;
import dev.spiffocode.sigesapi.reservables.domain.model.ReservableStatus;
import dev.spiffocode.sigesapi.reservables.domain.repository.BuildingRepository;
import dev.spiffocode.sigesapi.reservables.domain.repository.EquipmentRepository;
import dev.spiffocode.sigesapi.reservables.presentation.dto.AvailabilityExceptionRegisterDto;
import dev.spiffocode.sigesapi.reservables.presentation.dto.AvailabilitySlotRegisterDto;
import dev.spiffocode.sigesapi.reservables.presentation.dto.EquipmentRegisterDto;
import dev.spiffocode.sigesapi.reservables.presentation.dto.EquipmentUpdateDto;
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
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@IntegrationTestClass
public class EquipmentIT extends FlushedIntegrationTest {

        @Autowired
        MockMvc mvc;
        @Autowired
        ObjectMapper mapper;
        @Autowired
        UserRepository userRepository;
        @Autowired
        BuildingRepository buildingRepository;
        @Autowired
        EquipmentRepository equipmentRepository;
        @Autowired
        PasswordEncoder encoder;
        @Autowired
        BearerAuthService authService;

        private static final String API = "/equipments";
        private static final String VERSION = "1.0.0";

        private String adminToken;
        private String studentToken;
        private Building testBuilding;

        @Test
        void serialize_equipment_update_dto() throws JsonProcessingException {
                EquipmentUpdateDto dto = EquipmentUpdateDto.builder()
                                .inventoryNum("INV-5000")
                                .name("New Laptop")
                                .description("Updated!")
                                .studentsAvailable(false)
                                .buildingId(1L)
                                .build();
                String json = mapper.writeValueAsString(dto);

                EquipmentUpdateDto result = mapper.readValue(json, EquipmentUpdateDto.class);
        }

        @Test
        void serialize_equipment_register_dto() throws JsonProcessingException {
                EquipmentRegisterDto dto = EquipmentRegisterDto.builder()
                                .inventoryNum("INV-5000")
                                .name("New Laptop")
                                .description("Updated!")
                                .studentsAvailable(false)
                                .buildingId(1L)
                                .build();
                String json = mapper.writeValueAsString(dto);

                EquipmentRegisterDto result = mapper.readValue(json, EquipmentRegisterDto.class);
        }

        @BeforeEach
        void setup() {
                equipmentRepository.deleteAll();
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
        }

        private EquipmentRegisterDto createValidDto() {
                AvailabilitySlotRegisterDto slot = AvailabilitySlotRegisterDto.builder()
                                .startTime(LocalTime.of(8, 0))
                                .endTime(LocalTime.of(20, 0))
                                .daysOfWeek(Set.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY,
                                                DayOfWeek.THURSDAY,
                                                DayOfWeek.FRIDAY))
                                .build();

            AvailabilityExceptionRegisterDto exception = AvailabilityExceptionRegisterDto.builder()
                    .startTime(LocalTime.of(8,0))
                    .endTime(LocalTime.of(20,0))
                    .dateFrom(LocalDate.of(2026,12,1))
                    .dateTo(LocalDate.of(2026,12,7))
                    .reason("Navidad")
                    .build();

                return EquipmentRegisterDto.builder()
                                .inventoryNum("INV-1002")
                                .name("Proyector Epson")
                                .description("Proyector para clases")
                                .studentsAvailable(true)
                                .buildingId(testBuilding.getId())
                                .availability(List.of(slot))
                                .exceptions(List.of(exception))
                                .build();
        }

        @Test
        void searchEquipments_unauthenticated_returns403() throws Exception {
                mvc.perform(get(API).header("X-API-Version", VERSION))
                                .andExpect(status().isForbidden());
        }

        @Test
        void searchEquipments_authenticated_returns200() throws Exception {
            InventoryItem inventoryItem = new InventoryItem("INV-TEST-01");
            Equipment e = Equipment.builder()
                    .inventoryItem(inventoryItem)
                    .name("MacBook Pro")
                    .description("Sample laptop")
                    .studentsAvailable(true)
                    .building(testBuilding)
                    .status(ReservableStatus.AVAILABLE)
                    .createdBy("system")
                    .build();
            equipmentRepository.save(e);

            var a = mvc.perform(get(API)
                    .header("X-API-Version", VERSION)
                    .header("Authorization", "Bearer " + studentToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray()).andReturn();
            System.out.println(a);
        }

        @Test
        void getEquipment_authenticated_returns200() throws Exception {

            InventoryItem inventoryItem = new InventoryItem("INV-1234");
            Equipment e = Equipment.builder()
                    .inventoryItem(inventoryItem)
                    .name("Arduino Uno")
                    .description("Microcontroller")
                    .studentsAvailable(false)
                    .building(testBuilding)
                    .status(ReservableStatus.AVAILABLE)
                    .createdBy("system")
                    .build();
            e = equipmentRepository.save(e);

            mvc.perform(get(API + "/" + e.getId())
                    .header("X-API-Version", VERSION)
                    .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.inventoryIdNum").value("INV-1234"));
        }

        @Test
        void registerEquipment_asAdmin_returns201() throws Exception {
                EquipmentRegisterDto dto = createValidDto();

                var a = mvc.perform(post(API)
                                .header("X-API-Version", VERSION)
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(dto)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.inventoryIdNum").value("INV-1002"))
                                .andExpect(jsonPath("$.availabilityExceptions.length()").value(1))
                                .andExpect(jsonPath("$.availabilityExceptions[0].reason").value(dto.getExceptions().getFirst().reason()))
                                .andExpect(jsonPath("$.availabilitySlots.length()").value(1))
                                .andExpect(header().exists("Location"))
                        .andReturn();
            System.out.println(a);
        }

        @Test
        void registerEquipment_asStudent_returns403() throws Exception {
                EquipmentRegisterDto dto = createValidDto();

                mvc.perform(post(API)
                                .header("X-API-Version", VERSION)
                                .header("Authorization", "Bearer " + studentToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(dto)))
                                .andExpect(status().isForbidden());
        }

        @Test
        void updateEquipment_asAdmin_returns200() throws Exception {

            InventoryItem inventoryItem = new InventoryItem("INV-5000");
            Equipment e = Equipment.builder()
                    .inventoryItem(inventoryItem)
                    .name("Old Laptop")
                    .description("Needs update")
                    .studentsAvailable(true)
                    .building(testBuilding)
                    .status(ReservableStatus.AVAILABLE)
                    .createdBy("system")
                    .build();
            e = equipmentRepository.save(e);

            EquipmentUpdateDto dto = EquipmentUpdateDto.builder()
                    .inventoryNum("INV-5000")
                    .name("New Laptop")
                    .description("Updated!")
                    .studentsAvailable(false)
                    .buildingId(testBuilding.getId())
                    .build();

            mvc.perform(put(API + "/" + e.getId())
                    .header("X-API-Version", VERSION)
                    .header("Authorization", "Bearer " + adminToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsString(dto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("New Laptop"))
                    .andExpect(jsonPath("$.availableForStudents").value(false));
        }

        @Test
        void deactivateEquipment_asAdmin_returns204() throws Exception {
            InventoryItem inventoryItem = new InventoryItem("INV-TO-DEL");
                Equipment e = Equipment.builder()
                                .inventoryItem(inventoryItem)
                                .name("Delete Me")
                                .description("Bye")
                                .studentsAvailable(true)
                                .building(testBuilding)
                                .status(ReservableStatus.AVAILABLE)
                                .createdBy("system")
                                .build();
                e = equipmentRepository.save(e);

                mvc.perform(patch(API + "/" + e.getId() + "/deactivate")
                                .header("X-API-Version", VERSION)
                                .header("Authorization", "Bearer " + adminToken))
                                .andExpect(status().isNoContent());
        }

}
