package dev.spiffocode.sigesapi.integration.reports;

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
import dev.spiffocode.sigesapi.reservations.domain.model.Reservation;
import dev.spiffocode.sigesapi.reservations.domain.model.Status;
import dev.spiffocode.sigesapi.reservations.domain.repository.ReservationRepository;
import dev.spiffocode.sigesapi.users.domain.model.Admin;
import dev.spiffocode.sigesapi.users.domain.model.Student;
import dev.spiffocode.sigesapi.users.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@IntegrationTestClass
public class ReportsIT {

    @Autowired
    MockMvc mvc;
    @Autowired
    UserRepository userRepository;
    @Autowired
    BuildingRepository buildingRepository;
    @Autowired
    SpaceRepository spaceRepository;
    @Autowired
    SpaceTypeRepository spaceTypeRepository;
    @Autowired
    ReservationRepository reservationRepository;
    @Autowired
    PasswordEncoder encoder;
    @Autowired
    BearerAuthService authService;

    private String adminToken;
    private String studentToken;

    @BeforeEach
    void setup() {
        String pass = encoder.encode("password");

        Admin admin = Admin.builder()
                .email("admin-reports@test.com")
                .firstName("Admin")
                .lastName("Reports")
                .password(pass)
                .phoneNumber("7772865781")
                .birthDate(LocalDate.now().minusYears(20))
                .createdBy("system")
                .build();
        userRepository.save(admin);
        adminToken = authService.login(new LoginRequest("admin-reports@test.com", "password"), "127.0.0.1").accessToken();

        Student student = Student.builder()
                .email("student-reports@test.com")
                .firstName("Student")
                .lastName("Reports")
                .password(pass)
                .phoneNumber("7772865783")
                .birthDate(LocalDate.now().minusYears(18))
                .registrationNumber("S123-REPORTS")
                .createdBy("system")
                .build();
        userRepository.save(student);
        studentToken = authService.login(new LoginRequest("student-reports@test.com", "password"), "127.0.0.1").accessToken();

        Building building = Building.builder().name("Building Reports").build();
        buildingRepository.save(building);

        SpaceType type = SpaceType.builder().name("ReportRoom").description("Desc").build();
        spaceTypeRepository.save(type);

        Space space = Space.builder()
                .name("Report Space")
                .description("test description")
                .capacity(10)
                .building(building)
                .type(type)
                .bookInAdvance(Duration.ofHours(1))
                .status(ReservableStatus.AVAILABLE)
                .studentsAvailable(true)
                .createdBy("system")
                .build();
        spaceRepository.save(space);

        Reservation res = Reservation.builder()
                .petitioner(student)
                .reservable(space)
                .status(Status.APPROVED)
                .date(LocalDate.now())
                .startTime(LocalTime.of(10, 0))
                .endTime(LocalTime.of(11, 0))
                .type(dev.spiffocode.sigesapi.reservations.domain.model.GroupingType.SINGLE)
                .createdBy("admin")
                .build();
        reservationRepository.save(res);
    }

    @Test
    void getDashboardStats_asAdmin_returns200() throws Exception {
        mvc.perform(get("/reports/dashboard")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pendingRequests").exists())
                .andExpect(jsonPath("$.todayReservations").isNumber());
    }

    @Test
    void getDashboardStats_asStudent_returns403() throws Exception {
        mvc.perform(get("/reports/dashboard")
                .header("Authorization", "Bearer " + studentToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void getResourceStats_asAdmin_returns200() throws Exception {
        mvc.perform(get("/reports/resources")
                .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].resourceName").exists());
    }
}
