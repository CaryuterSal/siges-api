package dev.spiffocode.sigesapi.reports;

import dev.spiffocode.sigesapi.DataTestClass;
import dev.spiffocode.sigesapi.WithMockCustomUser;
import dev.spiffocode.sigesapi.reports.domain.model.DashboardStats;
import dev.spiffocode.sigesapi.reports.domain.repository.DashboardStatsRepository;
import dev.spiffocode.sigesapi.reservables.domain.model.*;
import dev.spiffocode.sigesapi.reservables.domain.repository.BuildingRepository;
import dev.spiffocode.sigesapi.reservables.domain.repository.SpaceRepository;
import dev.spiffocode.sigesapi.reservables.domain.repository.SpaceTypeRepository;
import dev.spiffocode.sigesapi.reservations.domain.model.Reservation;
import dev.spiffocode.sigesapi.reservations.domain.model.Status;
import dev.spiffocode.sigesapi.reservations.domain.repository.ReservationRepository;
import dev.spiffocode.sigesapi.users.domain.model.Student;
import dev.spiffocode.sigesapi.users.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@WithMockCustomUser
@DataTestClass
class DashboardStatsRepositoryTest {

    @Autowired
    DashboardStatsRepository dashboardStatsRepository;
    @Autowired
    ReservationRepository reservationRepository;
    @Autowired
    SpaceRepository spaceRepository;
    @Autowired
    BuildingRepository buildingRepository;
    @Autowired
    SpaceTypeRepository spaceTypeRepository;
    @Autowired
    UserRepository userRepository;

    private Student testStudent;
    private Space testSpace;

    @BeforeEach
    void setUp() {
        Building building = Building.builder().name("Building D").build();
        buildingRepository.save(building);

        SpaceType type = SpaceType.builder().name("General").description("Desc").build();
        spaceTypeRepository.save(type);

        testSpace = Space.builder()
                .name("General Room")
                .description("Desc")
                .capacity(50)
                .studentsAvailable(true)
                .building(building)
                .type(type)
                .bookInAdvance(Duration.ofHours(1))
                .status(ReservableStatus.AVAILABLE)
                .build();
        spaceRepository.save(testSpace);

        testStudent = Student.builder()
                .email("dash@test.com")
                .firstName("Dash")
                .lastName("Board")
                .password("password")
                .email("student@test.com")
                .phoneNumber("7772865783")
                .birthDate(LocalDate.now().minusYears(18))
                .registrationNumber("12345")
                .build();
        userRepository.save(testStudent);
    }

    @Test
    void shouldCalculateDashboardStats() {
        // 1 Pending today
        Reservation res1 = Reservation.builder()
                .petitioner(testStudent)
                .reservable(testSpace)
                .status(Status.PENDING)
                .date(LocalDate.now())
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(10, 0))
                .type(dev.spiffocode.sigesapi.reservations.domain.model.GroupingType.SINGLE)
                .createdBy("admin")
                .build();

        // 1 Approved today
        Reservation res2 = Reservation.builder()
                .petitioner(testStudent)
                .reservable(testSpace)
                .status(Status.APPROVED)
                .date(LocalDate.now())
                .startTime(LocalTime.of(11, 0))
                .endTime(LocalTime.of(12, 0))
                .type(dev.spiffocode.sigesapi.reservations.domain.model.GroupingType.SINGLE)
                .createdBy("admin")
                .build();

        reservationRepository.saveAll(List.of(res1, res2));

        Optional<DashboardStats> stats = dashboardStatsRepository.getStats();

        assertThat(stats).isPresent();
        assertThat(stats.get().getPendingRequests()).isGreaterThanOrEqualTo(1);
        assertThat(stats.get().getTodayReservations()).isGreaterThanOrEqualTo(2);
    }
}
