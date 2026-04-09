package dev.spiffocode.sigesapi.integration.reservations;

import dev.spiffocode.sigesapi.FlushedIntegrationTest;
import dev.spiffocode.sigesapi.auth.infrastructure.service.impl.BlacklistedJwtAuthService;
import dev.spiffocode.sigesapi.auth.presentation.dto.LoginRequest;
import dev.spiffocode.sigesapi.reservables.domain.model.*;
import dev.spiffocode.sigesapi.reservables.domain.repository.InventoryItemRepository;
import dev.spiffocode.sigesapi.reservables.domain.repository.ReservableRepository;
import dev.spiffocode.sigesapi.reservations.domain.model.GroupingType;
import dev.spiffocode.sigesapi.reservations.domain.model.Reservation;
import dev.spiffocode.sigesapi.reservations.domain.model.Status;
import dev.spiffocode.sigesapi.reservations.domain.repository.ReservationRepository;
import dev.spiffocode.sigesapi.reservations.presentation.ApproveReservationRequest;
import dev.spiffocode.sigesapi.reservations.presentation.CreateReservationRequest;
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
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ReservationConflictIT extends FlushedIntegrationTest {

        @Autowired
        private MockMvc mvc;

        @Autowired
        private ObjectMapper mapper;

        @Autowired
        private ReservationRepository reservationRepository;

        @Autowired
        private ReservableRepository reservableRepository;

        @Autowired
        private InventoryItemRepository inventoryItemRepository;

        @Autowired
        private UserRepository userRepository;

        @Autowired
        private PasswordEncoder encoder;

        private Equipment equipment;
        private Student student;
        private Admin admin;

        @Autowired
        private BlacklistedJwtAuthService blacklistedJwtAuthService;

    @BeforeEach
        void setUp() {
                InventoryItem item = InventoryItem.builder()
                                .inventoryNum("INV-" + System.currentTimeMillis())
                                .build();

                equipment = Equipment.builder()
                                .name("Laptop")
                                .status(ReservableStatus.AVAILABLE)
                                .studentsAvailable(true)
                                .inventoryItem(item)
                                .createdBy("system")
                                .build();
                reservableRepository.save(equipment);


        AvailabilitySlot as = AvailabilitySlot.builder()
                .reservable(equipment)
                .build();
        equipment.getAvailability().add(as);

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
        reservableRepository.save(equipment);

                student = Student.builder()
                                .email("student_conflict@test.com")
                                .password(encoder.encode("password"))
                                .phoneNumber("7772839283")
                                .firstName("Test")
                                .lastName("Student")
                                .birthDate(LocalDate.of(2002, 2, 2))
                                .registrationNumber("REG-" + System.currentTimeMillis())
                                .createdBy("system")
                                .build();
                userRepository.save(student);

        admin = Admin.builder()
                .email("admin@test.com")
                .password(encoder.encode("password"))
                .phoneNumber("7772839483")
                .firstName("Test")
                .lastName("Admin")
                .birthDate(LocalDate.of(2002, 2, 2))
                .createdBy("system")
                .build();
        userRepository.save(admin);
        }

        @Test
        void shouldAllowCreatingOverlappingPendingReservations() throws Exception {
                CreateReservationRequest req1 = new CreateReservationRequest(
                                equipment.getId(), LocalDate.now().plusDays(1), LocalTime.of(10, 0),
                                LocalTime.of(12, 0), GroupingType.GROUP, 3, "Reason 1");

                CreateReservationRequest req2 = new CreateReservationRequest(
                                equipment.getId(), LocalDate.now().plusDays(1), LocalTime.of(11, 0),
                                LocalTime.of(13, 0), GroupingType.GROUP, 3, "Reason 2");

                String token = getStudentToken();

                // First one
                mvc.perform(post("/reservations")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(req1))
                                .header("Authorization", "Bearer " + token))
                                .andExpect(status().isCreated());

                // Second one (overlaps with first, but first is PENDING)
                mvc.perform(post("/reservations")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(req2))
                                .header("Authorization", "Bearer " + token))
                                .andExpect(status().isCreated());

                assertThat(reservationRepository.count()).isEqualTo(2);
        }

        @Test
        void shouldAutoRejectOverlappingPendingReservationsOnApproval() throws Exception {
                LocalDate date = LocalDate.now().plusDays(1);
                LocalTime start1 = LocalTime.of(10, 0);
                LocalTime end1 = LocalTime.of(12, 0);
                LocalTime start2 = LocalTime.of(11, 0);
                LocalTime end2 = LocalTime.of(13, 0);

                Long res1Id = createReservationDirectly(date, start1, end1, Status.PENDING);
                Long res2Id = createReservationDirectly(date, start2, end2, Status.PENDING);

                // Approve res1
                ApproveReservationRequest approveReq = new ApproveReservationRequest("Approved");
                mvc.perform(patch("/reservations/{id}/approve", res1Id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(approveReq))
                                .header("Authorization", "Bearer " + getAdminToken()))
                                .andExpect(status().isOk());

                // Verify res1 is APPROVED
                Reservation res1 = reservationRepository.findById(res1Id).orElseThrow();
                assertThat(res1.getStatus()).isEqualTo(Status.APPROVED);

                // Verify res2 is REJECTED
                Reservation res2 = reservationRepository.findById(res2Id).orElseThrow();
                assertThat(res2.getStatus()).isEqualTo(Status.REJECTED);
                assertThat(res2.getRejectionReason()).contains("rechazado automáticamente");
        }

        @Test
        void shouldPreventCreatingReservationIfOverlapsWithApproved() throws Exception {
                LocalDate date = LocalDate.now().plusDays(1);
                LocalTime start1 = LocalTime.of(10, 0);
                LocalTime end1 = LocalTime.of(12, 0);

                createReservationDirectly(date, start1, end1, Status.APPROVED);

                // Attempt to create overlapping reservation
                CreateReservationRequest overlapReq = new CreateReservationRequest(
                                equipment.getId(), date, LocalTime.of(11, 0), LocalTime.of(13, 0), GroupingType.SINGLE,
                                0, "Overlap");

                mvc.perform(post("/reservations")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(overlapReq))
                                .header("Authorization", "Bearer " + getStudentToken()))
                                .andExpect(status().isConflict());
        }

        private Long createReservationDirectly(LocalDate date, LocalTime start, LocalTime end, Status status) {
                Reservation res = Reservation.builder()
                                .reservable(equipment)
                                .petitioner(student)
                                .type(GroupingType.GROUP)
                                .companions(2)
                                .date(date)
                                .startTime(start)
                                .endTime(end)
                                .status(status)
                                .createdBy("system")
                                .build();
                return reservationRepository.save(res).getId();
        }

        private String getStudentToken() throws Exception {
                return login("student_conflict@test.com", "password");
        }

        private String getAdminToken() throws Exception {
                return login("admin@test.com", "password");
        }

        private String login(String email, String password) throws Exception {
            return blacklistedJwtAuthService.login(new LoginRequest(email, password), "127.0.0.1")
                    .accessToken();
        }
}
