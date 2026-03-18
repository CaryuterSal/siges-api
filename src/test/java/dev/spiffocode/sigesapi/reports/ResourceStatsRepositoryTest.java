package dev.spiffocode.sigesapi.reports;

import dev.spiffocode.sigesapi.DataTestClass;
import dev.spiffocode.sigesapi.WithMockCustomUser;
import dev.spiffocode.sigesapi.reports.domain.model.ResourceStats;
import dev.spiffocode.sigesapi.reports.domain.repository.ResourceStatsRepository;
import dev.spiffocode.sigesapi.reservables.domain.model.Building;
import dev.spiffocode.sigesapi.reservables.domain.model.ReservableStatus;
import dev.spiffocode.sigesapi.reservables.domain.model.Space;
import dev.spiffocode.sigesapi.reservables.domain.model.SpaceType;
import dev.spiffocode.sigesapi.reservables.domain.repository.BuildingRepository;
import dev.spiffocode.sigesapi.reservables.domain.repository.SpaceRepository;
import dev.spiffocode.sigesapi.reservables.domain.repository.SpaceTypeRepository;
import dev.spiffocode.sigesapi.reservations.domain.model.GroupingType;
import dev.spiffocode.sigesapi.reservations.domain.model.Reservation;
import dev.spiffocode.sigesapi.reservations.domain.model.Status;
import dev.spiffocode.sigesapi.reservations.domain.repository.ReservationRepository;
import dev.spiffocode.sigesapi.users.domain.model.Student;
import dev.spiffocode.sigesapi.users.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@WithMockCustomUser
@DataTestClass
class ResourceStatsRepositoryTest {

        @Autowired
        ResourceStatsRepository resourceStatsRepository;
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

        private Space testSpace;
        private Student testStudent;

        @BeforeEach
        void setUp() {
                Building building = Building.builder().name("Building A").build();
                buildingRepository.save(building);

                SpaceType type = SpaceType.builder().name("Lab").description("Description").build();
                spaceTypeRepository.save(type);

                testSpace = Space.builder()
                                .name("Lab 101")
                                .description("Test Lab")
                                .capacity(10)
                                .studentsAvailable(true)
                                .building(building)
                                .type(type)
                                .bookInAdvance(Duration.ofHours(1))
                                .status(ReservableStatus.AVAILABLE)
                                .build();
                spaceRepository.save(testSpace);

                testStudent = Student.builder()
                                .email("student@test.com")
                                .firstName("John")
                                .lastName("Doe")
                                .password("password")
                                .email("jdoe@gmail.com")
                                .phoneNumber("7772865783")
                                .birthDate(LocalDate.now().minusYears(18))
                                .registrationNumber("12345")
                                .build();
                userRepository.save(testStudent);
        }

        @Test
        void shouldCalculateResourceStats() {
                // Create 2 approved reservations
                Reservation res1 = Reservation.builder()
                                .petitioner(testStudent)
                                .reservable(testSpace)
                                .status(Status.APPROVED)
                                .date(LocalDate.now().minusDays(5))
                                .startTime(LocalTime.of(10, 0))
                                .endTime(LocalTime.of(11, 0))
                                .type(GroupingType.SINGLE)
                                .createdBy("admin")
                                .build();

                Reservation res2 = Reservation.builder()
                                .petitioner(testStudent)
                                .reservable(testSpace)
                                .status(Status.APPROVED)
                                .date(LocalDate.now())
                                .startTime(LocalTime.of(14, 0))
                                .endTime(LocalTime.of(15, 0))
                                .type(GroupingType.SINGLE)
                                .createdBy("admin")
                                .build();

                reservationRepository.saveAll(List.of(res1, res2));

                List<ResourceStats> stats = resourceStatsRepository.findAll();

                assertThat(stats).isNotEmpty();
                Optional<ResourceStats> spaceStats = stats.stream()
                                .filter(s -> s.getReservableId().equals(testSpace.getId()))
                                .findFirst();

                assertThat(spaceStats).isPresent();
                assertThat(spaceStats.get().getResourceName()).isEqualTo("Lab 101");
                assertThat(spaceStats.get().getTotalReservations()).isEqualTo(2);
                assertThat(spaceStats.get().getOccupancyRate()).isEqualTo(BigDecimal.valueOf(100.0));
        }
}
