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
import dev.spiffocode.sigesapi.reservables.domain.repository.SpaceRepository;
import dev.spiffocode.sigesapi.reservables.domain.repository.SpaceTypeRepository;
import dev.spiffocode.sigesapi.reservations.domain.model.GroupingType;
import dev.spiffocode.sigesapi.reservations.domain.model.Reservation;
import dev.spiffocode.sigesapi.reservations.domain.model.Status;
import dev.spiffocode.sigesapi.reservations.domain.repository.ReservationRepository;
import dev.spiffocode.sigesapi.reservations.presentation.*;
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

import java.time.*;
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
        private Student testStudent2;
        private Admin testAdmin;
        private Equipment testEquipment;
        private Space testSpace;
        private Building testBuilding;
        @Autowired
        private SpaceTypeRepository spaceTypeRepository;
        @Autowired
        private SpaceRepository spaceRepository;

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

                testStudent2 = Student.builder()
                                .email("student2@siges.com")
                                .password(encoder.encode("password123"))
                                .firstName("Student 2")
                                .lastName("User")
                                .birthDate(LocalDate.of(2000, 1, 1))
                                .phoneNumber("+525555551556")
                                .registrationNumber("STU002")
                                .createdBy("system")
                                .build();
                userRepository.save(testStudent);
                userRepository.save(testStudent2);

                adminToken = authService.login(new LoginRequest("admin@siges.com", "password123"), "127.0.0.1")
                                .accessToken();
                studentToken = authService.login(new LoginRequest("student@siges.com", "password123"), "127.0.0.1")
                                .accessToken();

                testBuilding = Building.builder().name("Main Building").build();
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

                InventoryItem inventoryItem = new InventoryItem("INV-1000");
                testEquipment = Equipment.builder()
                                .inventoryItem(inventoryItem)
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
                testSpace.getAvailability().add(as);

                Availability availability = Availability.builder()
                                .dayOfWeek(DayOfWeek.MONDAY)
                                .group(as)
                                .dateFrom(LocalDate.now())
                                .startTime(LocalTime.of(9, 0))
                                .endTime(LocalTime.of(15, 0))
                                .createdAt(LocalDateTime.now())
                                .build();

                as.getMembers().add(availability);
                testEquipment = equipmentRepository.save(testEquipment);
                testSpace = spaceRepository.save(testSpace);
                SecurityContextHolder.createEmptyContext();
        }

        private CreateReservationRequest createValidReservationRequest() {
                return createValidReservationRequest(testEquipment);
        }

        private CreateReservationRequest createValidReservationRequest(Reservable reservable) {
                return CreateReservationRequest.builder()
                                .reservableId(reservable.getId())
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
        void createReservation_capacity_exceeded_returns422() throws Exception {
                CreateReservationRequest req = createValidReservationRequest(testSpace);
                req = req.withCompanions(101);

                mvc.perform(post(API)
                                .header("X-API-Version", VERSION)
                                .header("Authorization", "Bearer " + studentToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(req)))
                                .andExpect(status().isUnprocessableContent());
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
                                .petitioner(testStudent2)
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

                ApproveReservationRequest request = new ApproveReservationRequest(null);

                mvc.perform(patch(API + "/" + r.getId() + "/approve")
                                .header("X-API-Version", VERSION)
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(request)))
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

        @WithMockCustomUser
        @Test
        void createReservation_overlapping_returns409() throws Exception {
                Reservation r = Reservation.builder()
                                .date(LocalDate.now().plusDays(1))
                                .startTime(LocalTime.of(8, 0))
                                .endTime(LocalTime.of(12, 0))
                                .status(Status.APPROVED)
                                .type(GroupingType.SINGLE)
                                .reservable(testEquipment)
                                .petitioner(testStudent)
                                .build();
                reservationRepository.save(r);

                SecurityContextHolder.clearContext();

                CreateReservationRequest req = CreateReservationRequest.builder()
                                .reservableId(testEquipment.getId())
                                .date(LocalDate.now().plusDays(1))
                                .startTime(LocalTime.of(10, 0))
                                .endTime(LocalTime.of(14, 0))
                                .type(GroupingType.SINGLE)
                                .build();

                mvc.perform(post(API)
                                .header("X-API-Version", VERSION)
                                .header("Authorization", "Bearer " + studentToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(req)))
                                .andExpect(status().isConflict());
        }

        @Test
        void createReservation_noAnticipation_returns422() throws Exception {
                // Equipment with 2 days anticipation required
                testSpace.setBookInAdvance(Duration.ofDays(2));
                dev.spiffocode.sigesapi.reservables.domain.model.Space space = spaceRepository.save(testSpace);

                CreateReservationRequest req = CreateReservationRequest.builder()
                                .reservableId(space.getId())
                                .date(LocalDate.now().plusDays(1)) // only 1 day in advance
                                .startTime(LocalTime.of(10, 0))
                                .endTime(LocalTime.of(12, 0))
                                .type(GroupingType.SINGLE)
                                .build();

                mvc.perform(post(API)
                                .header("X-API-Version", VERSION)
                                .header("Authorization", "Bearer " + studentToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(req)))
                                .andExpect(status().isUnprocessableContent());
        }

        @WithMockCustomUser
        @Test
        void rescheduleReservation_returns200() throws Exception {
                LocalDate nextMonday = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY));
                Reservation r = Reservation.builder()
                                .date(nextMonday)
                                .startTime(LocalTime.of(8, 0))
                                .endTime(LocalTime.of(10, 0))
                                .status(Status.APPROVED)
                                .type(GroupingType.SINGLE)
                                .reservable(testEquipment)
                                .petitioner(testStudent)
                                .build();
                r = reservationRepository.save(r);
                SecurityContextHolder.clearContext();

                RescheduleReservationRequest req = RescheduleReservationRequest.builder()
                                .date(nextMonday)
                                .startTime(LocalTime.of(10, 0))
                                .endTime(LocalTime.of(12, 0))
                                .build();

                mvc.perform(patch(API + "/" + r.getId())
                                .header("X-API-Version", VERSION)
                                .header("Authorization", "Bearer " + studentToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(req)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value(Status.PENDING.name()));
        }

        @WithMockCustomUser
        @Test
        void rescheduleReservation_overlapping_returns409() throws Exception {
                LocalDate nextMonday = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY));
                Reservation r1 = Reservation.builder()
                                .date(nextMonday)
                                .startTime(LocalTime.of(10, 0))
                                .endTime(LocalTime.of(12, 0))
                                .status(Status.APPROVED)
                                .type(GroupingType.SINGLE)
                                .reservable(testEquipment)
                                .petitioner(testStudent2)
                                .build();
                reservationRepository.save(r1);

                Reservation r2 = Reservation.builder()
                                .date(nextMonday)
                                .startTime(LocalTime.of(8, 0))
                                .endTime(LocalTime.of(10, 0))
                                .status(Status.APPROVED)
                                .type(GroupingType.SINGLE)
                                .reservable(testEquipment)
                                .petitioner(testStudent)
                                .build();
                r2 = reservationRepository.save(r2);
                SecurityContextHolder.clearContext();

                RescheduleReservationRequest req = RescheduleReservationRequest.builder()
                                .date(nextMonday)
                                .startTime(LocalTime.of(11, 0)) // overlaps with r1
                                .endTime(LocalTime.of(13, 0))
                                .build();

                mvc.perform(patch(API + "/" + r2.getId())
                                .header("X-API-Version", VERSION)
                                .header("Authorization", "Bearer " + studentToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(req)))
                                .andExpect(status().isConflict());
        }

        @WithMockCustomUser
        @Test
        void cancelReservation_petitioner_returns200() throws Exception {
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

                CancelReservationRequest req = new CancelReservationRequest("No longer needed");

                mvc.perform(patch(API + "/" + r.getId() + "/cancel")
                                .header("X-API-Version", VERSION)
                                .header("Authorization", "Bearer " + studentToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(req)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value(Status.CANCELLED.name()));
        }

        @WithMockCustomUser
        @Test
        void startReservation_admin_returns200() throws Exception {
                Reservation r = Reservation.builder()
                                .date(LocalDate.now().plusDays(1))
                                .startTime(LocalTime.of(8, 0))
                                .endTime(LocalTime.of(10, 0))
                                .status(Status.APPROVED)
                                .type(GroupingType.SINGLE)
                                .reservable(testEquipment)
                                .petitioner(testStudent)
                                .build();
                r = reservationRepository.save(r);
                SecurityContextHolder.clearContext();

                mvc.perform(patch(API + "/" + r.getId() + "/start")
                                .header("X-API-Version", VERSION)
                                .header("Authorization", "Bearer " + adminToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value(Status.IN_PROGRESS.name()));
        }

        @WithMockCustomUser
        @Test
        void finishReservation_admin_returns200() throws Exception {
                Reservation r = Reservation.builder()
                                .date(LocalDate.now().plusDays(1))
                                .startTime(LocalTime.of(8, 0))
                                .endTime(LocalTime.of(10, 0))
                                .status(Status.IN_PROGRESS)
                                .type(GroupingType.SINGLE)
                                .reservable(testEquipment)
                                .petitioner(testStudent)
                                .build();
                r = reservationRepository.save(r);
                SecurityContextHolder.clearContext();

                mvc.perform(patch(API + "/" + r.getId() + "/finish")
                                .header("X-API-Version", VERSION)
                                .header("Authorization", "Bearer " + adminToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value(Status.FINISHED.name()));
        }

        @WithMockCustomUser
        @Test
        void addNote_returns201() throws Exception {
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

                PublishNoteRequest req = PublishNoteRequest.builder()
                                .reservationId(r.getId())
                                .comment("This is a new test note")
                                .build();

                mvc.perform(post(API + "/" + r.getId() + "/notes")
                                .header("X-API-Version", VERSION)
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(req)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.notes", hasSize(1)))
                                .andExpect(jsonPath("$.notes[0].comment").value("This is a new test note"));
        }

        @WithMockCustomUser
        @Test
        void editNote_returns200() throws Exception {
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

                // Add note first
                PublishNoteRequest req1 = PublishNoteRequest.builder()
                                .reservationId(r.getId())
                                .comment("Original note")
                                .build();

                String responseString = mvc.perform(post(API + "/" + r.getId() + "/notes")
                                .header("X-API-Version", VERSION)
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(req1)))
                                .andReturn().getResponse().getContentAsString();

                dev.spiffocode.sigesapi.reservations.presentation.ReservationResponse createdRes = mapper
                                .readValue(responseString,
                                                dev.spiffocode.sigesapi.reservations.presentation.ReservationResponse.class);
                Long noteId = createdRes.notes().get(0).id();

                EditNoteRequest req2 = EditNoteRequest.builder()
                                .comment("Edited note")
                                .build();

                mvc.perform(patch(API + "/" + r.getId() + "/notes/" + noteId)
                                .header("X-API-Version", VERSION)
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(mapper.writeValueAsString(req2)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.comment").value("Edited note"));
        }

        @WithMockCustomUser
        @Test
        void getReservations_withSearchQuery_returnsFilteredResults() throws Exception {
                // Create reservations with different resource names, building names, and
                // petitioner names
                Reservation r1 = Reservation.builder()
                                .date(LocalDate.now().plusDays(1))
                                .startTime(LocalTime.of(8, 0))
                                .endTime(LocalTime.of(10, 0))
                                .status(Status.PENDING)
                                .type(GroupingType.SINGLE)
                                .reservable(testEquipment) // Name: MacBook Pro, Building: Main Building
                                .petitioner(testStudent) // Name: Student User
                                .build();
                reservationRepository.save(r1);

                Reservation r2 = Reservation.builder()
                                .date(LocalDate.now().plusDays(1))
                                .startTime(LocalTime.of(11, 0))
                                .endTime(LocalTime.of(13, 0))
                                .status(Status.PENDING)
                                .type(GroupingType.SINGLE)
                                .reservable(testSpace) // Name: Auditorio A, Building: Main Building
                                .petitioner(testStudent2) // Name: Student 2 User
                                .build();
                reservationRepository.save(r2);

                SecurityContextHolder.clearContext();

                // Search by resource name (partial)
                mvc.perform(get(API)
                                .param("q", "MacBook")
                                .header("X-API-Version", VERSION)
                                .header("Authorization", "Bearer " + adminToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content", hasSize(1)))
                                .andExpect(jsonPath("$.content[0].reservable.name").value("MacBook Pro"));

                // Search by building name (partial)
                mvc.perform(get(API)
                                .param("q", "Main")
                                .header("X-API-Version", VERSION)
                                .header("Authorization", "Bearer " + adminToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content", hasSize(2)));

                // Search by petitioner first name
                mvc.perform(get(API)
                                .param("q", "Student 2")
                                .header("X-API-Version", VERSION)
                                .header("Authorization", "Bearer " + adminToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content", hasSize(1)))
                                .andExpect(jsonPath("$.content[0].petitioner.firstName").value("Student 2"));

                // Search by petitioner last name
                mvc.perform(get(API)
                                .param("q", "User")
                                .header("X-API-Version", VERSION)
                                .header("Authorization", "Bearer " + adminToken))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content", hasSize(2)));
        }
}
