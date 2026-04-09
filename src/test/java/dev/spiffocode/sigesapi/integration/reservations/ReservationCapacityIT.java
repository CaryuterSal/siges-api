package dev.spiffocode.sigesapi.integration.reservations;

import dev.spiffocode.sigesapi.FlushedIntegrationTest;
import dev.spiffocode.sigesapi.auth.infrastructure.service.impl.BlacklistedJwtAuthService;
import dev.spiffocode.sigesapi.auth.presentation.dto.LoginRequest;
import dev.spiffocode.sigesapi.reservables.domain.model.*;
import dev.spiffocode.sigesapi.reservables.domain.repository.ReservableRepository;
import dev.spiffocode.sigesapi.reservables.domain.repository.SpaceTypeRepository;
import dev.spiffocode.sigesapi.reservations.domain.model.GroupingType;
import dev.spiffocode.sigesapi.reservations.presentation.CreateReservationRequest;
import dev.spiffocode.sigesapi.users.domain.model.Student;
import dev.spiffocode.sigesapi.users.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ReservationCapacityIT extends FlushedIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper mapper;

    @Autowired
    private ReservableRepository reservableRepository;

    @Autowired
    private SpaceTypeRepository spaceTypeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder encoder;
    @Autowired
    private BlacklistedJwtAuthService blacklistedJwtAuthService;

    private Space space;
    private Student student;

    @BeforeEach
    void setUp() {
        SpaceType type = SpaceType.builder()
                .name("Cubicle")
                .description("1 person cubicle")
                .build();
        spaceTypeRepository.save(type);

        space = Space.builder()
                .name("Cubicle 1")
                .status(ReservableStatus.AVAILABLE)
                .studentsAvailable(true)
                .type(type)
                .capacity(1)
                .bookInAdvance(Duration.ZERO)
                .createdBy("system")
                .build();
        reservableRepository.save(space);



        AvailabilitySlot as = AvailabilitySlot.builder()
                .reservable(space)
                .build();
        space.getAvailability().add(as);

        for(DayOfWeek dow: DayOfWeek.values()) {

            Availability availability = Availability.builder()
                    .dayOfWeek(dow)
                    .group(as)
                    .dateFrom(LocalDate.now())
                    .startTime(LocalTime.of(9, 0))
                    .endTime(LocalTime.of(15, 0))
                    .createdAt(LocalDateTime.now())
                    .build();

            as.getMembers().add(availability);
        }
        reservableRepository.save(space);

        student = Student.builder()
                .email("student_capacity@test.com")
                .password(encoder.encode("password"))
                .phoneNumber("7773829382")
                .firstName("Test")
                .lastName("Student")
                .birthDate(LocalDate.now().minusYears(19))
                .registrationNumber("REG-CAP-" + System.currentTimeMillis())
                .createdBy("system")
                .build();
        userRepository.save(student);
    }

    @Test
    void shouldAllowSingleReservationWithNullCompanions() throws Exception {
        CreateReservationRequest req = new CreateReservationRequest(
                space.getId(), LocalDate.now().plusDays(1), LocalTime.of(10, 0), LocalTime.of(11, 0),
                GroupingType.SINGLE, null, "Single");

        mvc.perform(post("/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req))
                .header("Authorization", "Bearer " + getStudentToken()))
                .andExpect(status().isCreated());
    }

    @Test
    void shouldFailGroupReservationIfTotalExceedsCapacity() throws Exception {
        // Capacity is 1. Petitioner + 1 companion = 2. Should fail.
        CreateReservationRequest req = new CreateReservationRequest(
                space.getId(), LocalDate.now().plusDays(1), LocalTime.of(12, 0), LocalTime.of(13, 0),
                GroupingType.GROUP, 1, "Group of 2");

        mvc.perform(post("/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req))
                .header("Authorization", "Bearer " + getStudentToken()))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    void shouldAllowGroupReservationIfTotalWithinCapacity() throws Exception {
        // Increase capacity to 2
        space.setCapacity(2);
        reservableRepository.save(space);

        // Petitioner + 1 companion = 2. Should pass.
        CreateReservationRequest req = new CreateReservationRequest(
                space.getId(), LocalDate.now().plusDays(1), LocalTime.of(14, 0), LocalTime.of(15, 0),
                GroupingType.GROUP, 1, "Group of 2");

        mvc.perform(post("/reservations")
                .contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsString(req))
                .header("Authorization", "Bearer " + getStudentToken()))
                .andExpect(status().isCreated());
    }

    private String getStudentToken() throws Exception {

        return blacklistedJwtAuthService.login(new LoginRequest("student_capacity@test.com", "password"), "127.0.0.1")
                .accessToken();
    }
}
