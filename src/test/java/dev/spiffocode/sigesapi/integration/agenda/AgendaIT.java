package dev.spiffocode.sigesapi.integration.agenda;

import dev.spiffocode.sigesapi.FlushedIntegrationTest;
import dev.spiffocode.sigesapi.IntegrationTestClass;
import dev.spiffocode.sigesapi.auth.application.service.BearerAuthService;
import dev.spiffocode.sigesapi.auth.presentation.dto.LoginRequest;
import dev.spiffocode.sigesapi.reservables.domain.model.*;
import dev.spiffocode.sigesapi.reservables.domain.repository.BuildingRepository;
import dev.spiffocode.sigesapi.reservables.domain.repository.SpaceRepository;
import dev.spiffocode.sigesapi.reservables.domain.repository.SpaceTypeRepository;
import dev.spiffocode.sigesapi.reservations.domain.model.GroupingType;
import dev.spiffocode.sigesapi.reservations.domain.model.Reservation;
import dev.spiffocode.sigesapi.reservations.domain.model.Status;
import dev.spiffocode.sigesapi.reservations.domain.repository.ReservationRepository;
import dev.spiffocode.sigesapi.users.domain.model.Admin;
import dev.spiffocode.sigesapi.users.domain.model.Student;
import dev.spiffocode.sigesapi.users.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@IntegrationTestClass
public class AgendaIT extends FlushedIntegrationTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    UserRepository userRepository;
    @Autowired
    SpaceRepository spaceRepository;
    @Autowired
    BuildingRepository buildingRepository;
    @Autowired
    SpaceTypeRepository spaceTypeRepository;
    @Autowired
    ReservationRepository reservationRepository;
    @Autowired
    PasswordEncoder encoder;
    @Autowired
    BearerAuthService authService;

    private static final String API = "/reservables/%d/calendar";
    private static final String VERSION = "1.0.0";

    private String adminToken;
    private Space testSpace;

    private LocalDate nextMonday;
    private LocalDate nextTuesday;
    private LocalDate nextWednesday;
    private LocalDate nextSaturday;

    @BeforeEach
    void setup() {
        reservationRepository.deleteAll();
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

        Building testBuilding = Building.builder().name("Main Building").build();
        testBuilding = buildingRepository.save(testBuilding);

        SpaceType testSpaceType = SpaceType.builder()
                .name("Aula")
                .description("Salón de clases")
                .build();
        testSpaceType = spaceTypeRepository.save(testSpaceType);

        testSpace = Space.builder()
                .name("Auditorio A")
                .description("Espacio grande")
                .studentsAvailable(true)
                .building(testBuilding)
                .type(testSpaceType)
                .capacity(100)
                .bookInAdvance(Duration.ZERO)
                .status(ReservableStatus.AVAILABLE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .createdBy("system")
                .build();

        // Availability: Monday-Friday 08:00 - 18:00
        AvailabilitySlot slot = AvailabilitySlot.builder().build();
        for (DayOfWeek day : List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY,
                DayOfWeek.FRIDAY)) {
            slot.addMember(Availability.builder()
                    .group(slot)
                    .dayOfWeek(day)
                    .startTime(LocalTime.of(8, 0))
                    .endTime(LocalTime.of(18, 0))
                    .build());
        }
        testSpace.addAvailabilitySlot(slot);

        nextMonday = advanceTo(DayOfWeek.MONDAY);
        nextTuesday = nextMonday.plusDays(1);
        nextWednesday = nextMonday.plusDays(2);
        nextSaturday = nextMonday.plusDays(5);

        // Exception: Wednesday 12:00 - 14:00
        AvailabilityException ex = AvailabilityException.builder()
                .dateFrom(nextWednesday)
                .dateTo(nextWednesday)
                .startTime(LocalTime.of(12, 0))
                .endTime(LocalTime.of(14, 0))
                .build();
        testSpace.addAvailabilityException(ex);

        testSpace = spaceRepository.save(testSpace);

        // Reservations
        // 1. Monday 10:00 - 12:00 (APPROVED)
        Reservation r1 = Reservation.builder()
                .reservable(testSpace)
                .petitioner(student)
                .status(Status.APPROVED)
                .date(nextMonday)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(12, 0))
                .type(GroupingType.GROUP)
                .companions(20)
                .build();

        // 2. Monday 14:00 - 15:00 (PENDING)
        Reservation r2 = Reservation.builder()
                .reservable(testSpace)
                .petitioner(student)
                .status(Status.PENDING)
                .date(nextMonday)
                .startTime(LocalTime.of(14, 0))
                .endTime(LocalTime.of(15, 0))
                .type(GroupingType.GROUP)
                .companions(20)
                .build();

        // 3. Tuesday 10:00 - 12:00 (REJECTED) -> shouldn't block availability
        Reservation r3 = Reservation.builder()
                .reservable(testSpace)
                .petitioner(student)
                .status(Status.REJECTED)
                .date(nextTuesday)
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(12, 0))
                .type(GroupingType.GROUP)
                .companions(10)
                .build();

        reservationRepository.saveAll(List.of(r1, r2, r3));
    }

    private LocalDate advanceTo(DayOfWeek targetDay) {
        LocalDate current = LocalDate.now();
        while (current.getDayOfWeek() != targetDay) {
            current = current.plusDays(1);
        }
        // ensure it's in the future
        if (!current.isAfter(LocalDate.now())) {
            current = current.plusWeeks(1);
        }
        return current;
    }

    @Test
    void getCalendar_forMonday_showsOccupiedAndAvailableBlocks() throws Exception {
        mvc.perform(get(String.format(API, testSpace.getId()))
                .param("from", nextMonday.toString())
                .param("to", nextMonday.toString())
                .header("X-API-Version", VERSION)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].date").value(nextMonday.toString()))
                // 3 Available blocks: 08:00-10:00, 12:00-14:00, 15:00-18:00
                .andExpect(jsonPath("$[0].availableBlocks", hasSize(3)))
                .andExpect(jsonPath("$[0].availableBlocks[0].start").value("08:00:00"))
                .andExpect(jsonPath("$[0].availableBlocks[0].end").value("10:00:00"))
                .andExpect(jsonPath("$[0].availableBlocks[1].start").value("12:00:00"))
                .andExpect(jsonPath("$[0].availableBlocks[1].end").value("14:00:00"))
                .andExpect(jsonPath("$[0].availableBlocks[2].start").value("15:00:00"))
                .andExpect(jsonPath("$[0].availableBlocks[2].end").value("18:00:00"))
                // 2 Occupied blocks: 10:00-12:00, 14:00-15:00
                .andExpect(jsonPath("$[0].occupiedBlocks", hasSize(2)))
                .andExpect(jsonPath("$[0].occupiedBlocks[0].start").value("10:00:00"))
                .andExpect(jsonPath("$[0].occupiedBlocks[0].end").value("12:00:00"))
                .andExpect(jsonPath("$[0].occupiedBlocks[0].status").value("APPROVED"))
                .andExpect(jsonPath("$[0].occupiedBlocks[1].start").value("14:00:00"))
                .andExpect(jsonPath("$[0].occupiedBlocks[1].end").value("15:00:00"))
                .andExpect(jsonPath("$[0].occupiedBlocks[1].status").value("PENDING"));
    }

    @Test
    void getCalendar_forTuesday_ignoresRejectedReservations() throws Exception {
        mvc.perform(get(String.format(API, testSpace.getId()))
                .param("from", nextTuesday.toString())
                .param("to", nextTuesday.toString())
                .header("X-API-Version", VERSION)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                // REJECTED reservation does not occupy anything
                .andExpect(jsonPath("$[0].occupiedBlocks", empty()))
                .andExpect(jsonPath("$[0].availableBlocks", hasSize(1)))
                .andExpect(jsonPath("$[0].availableBlocks[0].start").value("08:00:00"))
                .andExpect(jsonPath("$[0].availableBlocks[0].end").value("18:00:00"));
    }

    @Test
    void getCalendar_forWednesday_showsExceptionSlicing() throws Exception {
        mvc.perform(get(String.format(API, testSpace.getId()))
                .param("from", nextWednesday.toString())
                .param("to", nextWednesday.toString())
                .header("X-API-Version", VERSION)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].occupiedBlocks", empty()))
                // Exception from 12:00 to 14:00 slices availability into 08:00-12:00 and
                // 14:00-18:00
                .andExpect(jsonPath("$[0].availableBlocks", hasSize(2)))
                .andExpect(jsonPath("$[0].availableBlocks[0].start").value("08:00:00"))
                .andExpect(jsonPath("$[0].availableBlocks[0].end").value("12:00:00"))
                .andExpect(jsonPath("$[0].availableBlocks[1].start").value("14:00:00"))
                .andExpect(jsonPath("$[0].availableBlocks[1].end").value("18:00:00"));
    }

    @Test
    void getCalendar_forSaturday_showsNoAvailability() throws Exception {
        mvc.perform(get(String.format(API, testSpace.getId()))
                .param("from", nextSaturday.toString())
                .param("to", nextSaturday.toString())
                .header("X-API-Version", VERSION)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].availableBlocks", empty()))
                .andExpect(jsonPath("$[0].occupiedBlocks", empty()));
    }

    @Test
    void getCalendar_invalidDates_returns400() throws Exception {
        mvc.perform(get(String.format(API, testSpace.getId()))
                .param("from", nextTuesday.toString()) // from is after to
                .param("to", nextMonday.toString())
                .header("X-API-Version", VERSION)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getCalendar_nonExistentReservable_returns404() throws Exception {
        mvc.perform(get(String.format(API, 999999L))
                .param("from", nextMonday.toString())
                .param("to", nextMonday.toString())
                .header("X-API-Version", VERSION)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

    @Test
    void getCalendar_noDatesProvided_returnsDefaultMonth() throws Exception {
        mvc.perform(get(String.format(API, testSpace.getId()))
                .header("X-API-Version", VERSION)
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                // Verify that roughly 30 days are returned (1 month plus/minus days by month
                // length)
                .andExpect(jsonPath("$.length()", greaterThanOrEqualTo(28)));
    }
}
