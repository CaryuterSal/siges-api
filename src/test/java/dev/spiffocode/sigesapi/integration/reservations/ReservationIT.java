package dev.spiffocode.sigesapi.integration.reservations;

import dev.spiffocode.sigesapi.FlushedIntegrationTest;
import dev.spiffocode.sigesapi.IntegrationTestClass;
import dev.spiffocode.sigesapi.WithMockCustomUser;
import dev.spiffocode.sigesapi.auth.application.service.BearerAuthService;
import dev.spiffocode.sigesapi.auth.presentation.dto.LoginRequest;
import dev.spiffocode.sigesapi.mailsender.application.service.ReservationsEmailPort;
import dev.spiffocode.sigesapi.notifications.application.service.PushNotificationPort;
import dev.spiffocode.sigesapi.reservables.domain.model.*;
import dev.spiffocode.sigesapi.reservables.domain.repository.BuildingRepository;
import dev.spiffocode.sigesapi.reservables.domain.repository.EquipmentRepository;
import dev.spiffocode.sigesapi.reservations.domain.model.GroupingType;
import dev.spiffocode.sigesapi.reservations.domain.model.Reservation;
import dev.spiffocode.sigesapi.reservations.domain.model.Status;
import dev.spiffocode.sigesapi.reservations.domain.repository.ReservationRepository;
import dev.spiffocode.sigesapi.reservations.presentation.CreateReservationRequest;
import dev.spiffocode.sigesapi.reservations.presentation.RejectReservationRequest;
import dev.spiffocode.sigesapi.users.domain.model.Admin;
import dev.spiffocode.sigesapi.users.domain.model.Student;
import dev.spiffocode.sigesapi.users.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@IntegrationTestClass
public class ReservationIT extends FlushedIntegrationTest {

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
    ReservationRepository reservationRepository;
    @Autowired
    PasswordEncoder encoder;
    @Autowired
    BearerAuthService authService;

    @MockitoBean
    PushNotificationPort pushNotificationPort;
    @MockitoBean
    ReservationsEmailPort reservationsEmailPort;

    private static final String API = "/reservations";
    private static final String VERSION = "1.0.0";

    private String adminToken;
    private String studentToken;
    private Student testStudent;
    private Admin testAdmin;
    private Equipment testEquipment;
    private Building testBuilding;

    @BeforeEach
    void setup() {
        reservationRepository.deleteAll();
        equipmentRepository.deleteAll();
        buildingRepository.deleteAll();
        userRepository.deleteAll();

        testAdmin = Admin.builder()
                .email("admin@siges.com")
                .password(encoder.encode("password123"))
                .firstName("Admin")
                .lastName("User")
                .birthDate(LocalDate.of(1980, 1, 1))
                .phoneNumber("+525555555555")
                .createdBy("system")
                .build();
        userRepository.save(testAdmin);

        testStudent = Student.builder()
                .email("student@siges.com")
                .password(encoder.encode("password123"))
                .firstName("Student")
                .lastName("User")
                .birthDate(LocalDate.of(2000, 1, 1))
                .phoneNumber("+525555555556")
                .registrationNumber("STU001")
                .createdBy("system")
                .build();
        userRepository.save(testStudent);

        adminToken = authService.login(new LoginRequest("admin@siges.com", "password123"), "127.0.0.1").accessToken();
        studentToken = authService.login(new LoginRequest("student@siges.com", "password123"), "127.0.0.1")
                .accessToken();

        testBuilding = Building.builder().name("Main Building").build();
        testBuilding = buildingRepository.save(testBuilding);

        testEquipment = Equipment.builder()
                .inventoryNum("INV-1000")
                .name("MacBook Pro")
                .description("Sample laptop")
                .studentsAvailable(true)
                .building(testBuilding)
                .status(ReservableStatus.AVAILABLE)
                .createdBy("system")
                .build();

        AvailabilitySlot as = AvailabilitySlot.builder()
                .reservable(testEquipment)
                .build();
        testEquipment.getAvailability().add(as);


        Availability availability = Availability.builder()
                .dayOfWeek(DayOfWeek.MONDAY)
                .group(as)
                .dateFrom(LocalDate.now())
                .startTime(LocalTime.of(9,0))
                .endTime(LocalTime.of(15,0))
                .createdAt(LocalDateTime.now())
                .build();

        as.getMembers().add(availability);
        testEquipment = equipmentRepository.save(testEquipment);
        SecurityContextHolder.createEmptyContext();
    }

    private CreateReservationRequest createValidReservationRequest() {
        return CreateReservationRequest.builder()
                .reservableId(testEquipment.getId())
                .date(LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY)))
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(12, 0))
                .type(GroupingType.SINGLE)
                .build();
    }

    @Test
    void createReservation_authenticated_returns201() throws Exception {
        CreateReservationRequest req = createValidReservationRequest();

        mvc.perform(post(API)
                .header("X-API-Version", VERSION)
                .header("Authorization", "Bearer " + studentToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.status").value(Status.PENDING.name()))
                .andExpect(header().exists("Location"));
    }

    @Test
    void createReservation_unauthenticated_returns403() throws Exception {
        CreateReservationRequest req = createValidReservationRequest();

        mvc.perform(post(API)
                .header("X-API-Version", VERSION)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
                .andExpect(status().isForbidden());
    }


    @WithMockCustomUser
    @Test
    void getReservations_withStudent_returnsOnlyTheirReservations() throws Exception {
        // Create 2 reservations: one for student, one by admin
        Reservation r1 = Reservation.builder()
                .date(LocalDate.now().plusDays(1))
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(10, 0))
                .status(Status.PENDING)
                .type(GroupingType.SINGLE)
                .reservable(testEquipment)
                .petitioner(testStudent)
                .build();
        reservationRepository.save(r1);

        Reservation r2 = Reservation.builder()
                .date(LocalDate.now().plusDays(1))
                .startTime(LocalTime.of(11, 0))
                .endTime(LocalTime.of(13, 0))
                .status(Status.PENDING)
                .type(GroupingType.SINGLE)
                .reservable(testEquipment)
                .petitioner(testAdmin)
                .build();
        reservationRepository.save(r2);

        SecurityContextHolder.clearContext();

        mvc.perform(get(API)
                .header("X-API-Version", VERSION)
                .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].petitioner.id").value(testStudent.getId()));

        mvc.perform(get(API)
                .header("X-API-Version", VERSION)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)));
    }

    @WithMockCustomUser
    @Test
    void approveReservation_asAdmin_returns200() throws Exception {
        Reservation r = Reservation.builder()
                .date(LocalDate.now().plusDays(1))
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(10, 0))
                .status(Status.PENDING)
                .type(GroupingType.SINGLE)
                .reservable(testEquipment)
                .petitioner(testStudent)
                .build();
        r = reservationRepository.save(r);
        SecurityContextHolder.clearContext();

        mvc.perform(patch(API + "/" + r.getId() + "/approve")
                .header("X-API-Version", VERSION)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(Status.APPROVED.name()));
    }

    @WithMockCustomUser
    @Test
    void approveReservation_asStudent_returns403() throws Exception {
        Reservation r = Reservation.builder()
                .date(LocalDate.now().plusDays(1))
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(10, 0))
                .status(Status.PENDING)
                .type(GroupingType.SINGLE)
                .reservable(testEquipment)
                .petitioner(testStudent)
                .build();
        r = reservationRepository.save(r);
        SecurityContextHolder.clearContext();

        mvc.perform(patch(API + "/" + r.getId() + "/approve")
                .header("X-API-Version", VERSION)
                .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden());
    }


    @WithMockCustomUser
    @Test
    void rejectReservation_asAdmin_returns200() throws Exception {
        Reservation r = Reservation.builder()
                .date(LocalDate.now().plusDays(1))
                .startTime(LocalTime.of(8, 0))
                .endTime(LocalTime.of(10, 0))
                .status(Status.PENDING)
                .type(GroupingType.SINGLE)
                .reservable(testEquipment)
                .petitioner(testStudent)
                .build();
        r = reservationRepository.save(r);
        SecurityContextHolder.clearContext();

        RejectReservationRequest req = new RejectReservationRequest("Because reasons");

        mvc.perform(patch(API + "/" + r.getId() + "/reject")
                .header("X-API-Version", VERSION)
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(Status.REJECTED.name()));
    }
}
