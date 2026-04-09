package dev.spiffocode.sigesapi.integration.reservations;

import dev.spiffocode.sigesapi.FlushedIntegrationTest;
import dev.spiffocode.sigesapi.IntegrationTestClass;
import dev.spiffocode.sigesapi.reservables.domain.model.Equipment;
import dev.spiffocode.sigesapi.reservables.domain.model.InventoryItem;
import dev.spiffocode.sigesapi.reservables.domain.model.ReservableStatus;
import dev.spiffocode.sigesapi.reservables.domain.repository.ReservableRepository;
import dev.spiffocode.sigesapi.reservations.domain.model.GroupingType;
import dev.spiffocode.sigesapi.reservations.domain.model.Reservation;
import dev.spiffocode.sigesapi.reservations.domain.model.Status;
import dev.spiffocode.sigesapi.reservations.domain.repository.ReservationRepository;
import dev.spiffocode.sigesapi.reservations.infrastructure.scheduler.ReservationScheduler;
import dev.spiffocode.sigesapi.users.domain.model.Student;
import dev.spiffocode.sigesapi.users.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@IntegrationTestClass
public class ReservationAutomationIT extends FlushedIntegrationTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservableRepository reservableRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private ReservationScheduler reservationScheduler;

    @MockitoBean
    private Clock clock;

    private Equipment equipment;
    private Student student;

    @BeforeEach
    void setUp() {
        // Default clock to 10:00 AM on 2026-04-10
        setMockTime("2026-04-10T10:00:00Z");

        reservationRepository.deleteAll();
        reservableRepository.deleteAll();
        userRepository.deleteAll();

        InventoryItem inventoryItem = InventoryItem.builder()
                .inventoryNum("INV-001")
                .build();

        equipment = Equipment.builder()
                .name("Laptop Test")
                .description("Test Description")
                .status(ReservableStatus.AVAILABLE)
                .inventoryItem(inventoryItem)
                .createdBy("system")
                .build();
        equipment = reservableRepository.save(equipment);

        student = Student.builder()
                .email("student@gmail.com")
                .password(encoder.encode("password123"))
                .firstName("Student")
                .lastName("User")
                .birthDate(LocalDate.of(2000, 1, 1))
                .phoneNumber("+525555555555")
                .registrationNumber("20213TN001")
                .createdBy("system")
                .build();
        student = userRepository.save(student);
    }

    private void setMockTime(String instantIso) {
        Instant instant = Instant.parse(instantIso);
        when(clock.instant()).thenReturn(instant);
        when(clock.getZone()).thenReturn(ZoneId.of("UTC"));
    }

    @Test
    void shouldAutoStartApprovedReservationsAtStartTime() {
        // Given an approved reservation starting at 10:00 AM
        Reservation reservation = Reservation.builder()
                .reservable(equipment)
                .petitioner(student)
                .type(GroupingType.SINGLE)
                .companions(1)
                .date(LocalDate.of(2026, 4, 10))
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(11, 0))
                .status(Status.APPROVED)
                .createdBy("system")
                .build();
        reservation = reservationRepository.save(reservation);

        // When the scheduler runs at 10:00 AM
        reservationScheduler.autoStartReservations();

        // Then the reservation should be IN_PROGRESS
        Reservation updated = reservationRepository.findById(reservation.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(Status.IN_PROGRESS);
        assertThat(updated.isAutoStarted()).isTrue();
        assertThat(updated.getStartedAt()).isNotNull();

        // And the reservable should be LOANED
        Equipment updatedEquipment = (Equipment) reservableRepository.findById(equipment.getId()).orElseThrow();
        assertThat(updatedEquipment.getStatus()).isEqualTo(ReservableStatus.LOANED);
    }

    @Test
    void shouldAutoFinishInProgressReservationsAtEndTime() {
        // Given an in-progress reservation ending at 11:00 AM
        Reservation reservation = Reservation.builder()
                .reservable(equipment)
                .petitioner(student)
                .createdBy("system")
                .type(GroupingType.GROUP)
                .companions(1)
                .date(LocalDate.of(2026, 4, 10))
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(11, 0))
                .status(Status.IN_PROGRESS)
                .type(GroupingType.SINGLE)
                .build();
        reservation = reservationRepository.save(reservation);
        equipment.setStatus(ReservableStatus.LOANED);
        reservableRepository.save(equipment);

        // Advance time to 11:00 AM
        setMockTime("2026-04-10T11:00:00Z");

        // When the scheduler runs
        reservationScheduler.autoFinishReservations();

        // Then the reservation should be FINISHED
        Reservation updated = reservationRepository.findById(reservation.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(Status.FINISHED);
        assertThat(updated.isAutoFinished()).isTrue();
        assertThat(updated.getFinishedAt()).isNotNull();

        // And the reservable should be AVAILABLE
        Equipment updatedEquipment = (Equipment) reservableRepository.findById(equipment.getId()).orElseThrow();
        assertThat(updatedEquipment.getStatus()).isEqualTo(ReservableStatus.AVAILABLE);
    }
}
