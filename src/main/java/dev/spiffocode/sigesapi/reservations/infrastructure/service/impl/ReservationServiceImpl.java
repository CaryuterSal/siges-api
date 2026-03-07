package dev.spiffocode.sigesapi.reservations.infrastructure.service.impl;

import dev.spiffocode.sigesapi.auth.infrastructure.SecurityContextHelper;
import dev.spiffocode.sigesapi.mailsender.application.service.ReservationsEmailPort;
import dev.spiffocode.sigesapi.notifications.application.service.NotificationsPort;
import dev.spiffocode.sigesapi.notifications.application.service.SendNotificationCommand;
import dev.spiffocode.sigesapi.notifications.domain.model.Type;
import dev.spiffocode.sigesapi.reservables.domain.exception.ReservableNotFoundException;
import dev.spiffocode.sigesapi.reservables.domain.model.Reservable;
import dev.spiffocode.sigesapi.reservables.domain.model.ReservableStatus;
import dev.spiffocode.sigesapi.reservables.domain.model.Space;
import dev.spiffocode.sigesapi.reservables.domain.repository.ReservableRepository;
import dev.spiffocode.sigesapi.reservations.application.mapper.NoteMapper;
import dev.spiffocode.sigesapi.reservations.application.mapper.ReservationMapper;
import dev.spiffocode.sigesapi.reservations.application.service.ReservationService;
import dev.spiffocode.sigesapi.reservations.domain.exception.*;
import dev.spiffocode.sigesapi.reservations.domain.model.GroupingType;
import dev.spiffocode.sigesapi.reservations.domain.model.Note;
import dev.spiffocode.sigesapi.reservations.domain.model.Reservation;
import dev.spiffocode.sigesapi.reservations.domain.model.Status;
import dev.spiffocode.sigesapi.reservations.domain.repository.NoteRepository;
import dev.spiffocode.sigesapi.reservations.domain.repository.ReservationRepository;
import dev.spiffocode.sigesapi.reservations.domain.specifications.ReservationSpecifications;
import dev.spiffocode.sigesapi.reservations.presentation.*;
import dev.spiffocode.sigesapi.users.domain.exception.UserNotFoundException;
import dev.spiffocode.sigesapi.users.domain.model.User;
import dev.spiffocode.sigesapi.users.domain.repository.UserRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private final ReservableRepository reservableRepository;
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final NoteRepository noteRepository;

    private final SecurityContextHelper securityContextHelper;
    private final NotificationsPort notificationsPort;
    private final ReservationsEmailPort emailPort;

    private final ReservationMapper reservationMapper;
    private final Clock clock;
    private final NoteMapper noteMapper;

    @Override
    public ReservationResponse createReservation(CreateReservationRequest request) {
        Long userId = securityContextHelper.getCurrentUserId();

        Reservable reservable = findReservableOrThrow(request.reservableId());
        User petitioner = findUserOrThrow(userId);

        validateAdvanceTime(reservable, request.date(), request.startTime(), request.endTime());
        validateStudentRestrictions(petitioner, reservable, request.companions());
        validateNoOverlap(request.reservableId(), request.date(), request.startTime(), request.endTime(), null);

        Reservation reservation = Reservation.builder()
                .petitioner(petitioner)
                .reservable(reservable)
                .date(request.date())
                .startTime(request.startTime())
                .endTime(request.endTime())
                .companions(request.type() == GroupingType.GROUP ? request.companions() : null)
                .build();

        Reservation saved = reservationRepository.save(reservation);
        notificationsPort.sendNotificationToAdmins(SendNotificationCommand.builder()
                .type(Type.RESERVATION_CREATED).build());
        emailPort.sendReservationCreatedEmail(petitioner.getEmail(), reservation.getId());
        return reservationMapper.toDto(saved, List.of());
    }

    @Override
    @Transactional
    public ReservationResponse rescheduleReservation(Long id, RescheduleReservationRequest request) {
        Long userId = securityContextHelper.getCurrentUserId();
        Reservation reservation = findReservationOrThrow(id);

        if (reservation.getStatus() != Status.PENDING && reservation.getStatus() != Status.APPROVED)
            throw new InvalidReservationStatusException(reservation.getStatus(), Status.PENDING);

        validateAdvanceTime(reservation.getReservable(), request.date(), request.startTime(), request.endTime());
        validateNoOverlap(reservation.getReservable().getId(), request.date(),
                request.startTime(), request.endTime(), id);

        reservation.reschedule(request.date(), request.startTime(), request.endTime(), clock);

        notificationsPort.sendNotificationToAdmins(SendNotificationCommand.builder()
                .type(Type.RESERVATION_RESCHEDULE).build());

        return toResponse(reservationRepository.save(reservation));
    }

    // ─────────────────────────────────────────────
    // STATUS CHANGES
    // ─────────────────────────────────────────────

    @Transactional
    @Override
    public ReservationResponse approveReservation(Long id) {
        Reservation reservation = findReservationOrThrow(id);
        reservation.approve(clock);

        emailPort.sendReservationResolutionEmail(reservation.getPetitioner().getEmail(), Status.APPROVED, id);
        notificationsPort.sendNotification(reservation.getPetitioner().getId(),
                SendNotificationCommand.builder().type(Type.RESERVATION_APPROVED).build());

        return toResponse(reservationRepository.save(reservation));
    }

    @Transactional
    @Override
    public ReservationResponse rejectReservation(Long id, RejectReservationRequest request) {
        Reservation reservation = findReservationOrThrow(id);
        reservation.reject(request.reason(), clock);

        emailPort.sendReservationResolutionEmail(reservation.getPetitioner().getEmail(), Status.REJECTED,id);
        notificationsPort.sendNotification(reservation.getPetitioner().getId(),
                SendNotificationCommand.builder().type(Type.RESERVATION_REJECTED).build());

        return toResponse(reservationRepository.save(reservation));
    }

    @Transactional
    @Override
    public ReservationResponse cancelReservation(Long id, CancelReservationRequest request) {
        Long userId = securityContextHelper.getCurrentUserId();
        boolean isAdmin = securityContextHelper.isAdmin();
        Reservation reservation = findReservationOrThrow(id);

        boolean isPetitioner = reservation.getPetitioner().getId().equals(userId);
        if (!isAdmin && !isPetitioner){
            throw new AccessDeniedException("Only the petitioner or an admin can cancel a reservation");
            }

        reservation.cancel(request.reason(), clock);

        emailPort.sendReservationCancelledEmail(reservation.getPetitioner().getEmail(), id);
        notificationsPort.sendNotification(reservation.getPetitioner().getId(),
                SendNotificationCommand.builder().type(Type.RESERVATION_CANCELLED).build());

        return toResponse(reservationRepository.save(reservation));
    }

    @Transactional
    @Override
    public ReservationResponse finishReservation(Long id) {
        Reservation reservation = findReservationOrThrow(id);
        reservation.finish(clock);
        return toResponse(reservationRepository.save(reservation));
    }

    // ─────────────────────────────────────────────
    // NOTES
    // ─────────────────────────────────────────────

    @Transactional
    @Override
    public ReservationResponse addNote(Long reservationId, PublishNoteRequest request) {
        Reservation reservation = findReservationOrThrow(reservationId);
        reservation.addNote(request.comment());
        return toResponse(reservationRepository.save(reservation));
    }

    @Transactional
    @Override
    public NoteItem editNote(Long reservationId, Long noteId, EditNoteRequest request) {
        Note note = noteRepository.findByIdAndReservationId(noteId, reservationId)
                .orElseThrow(() -> new NoteNotFoundException(noteId));

        String userId = securityContextHelper.getCurrentUserEmail();
        boolean isAdmin = securityContextHelper.isAdmin();

        if (!isAdmin && !note.getCreatedBy().equals(userId))
            throw new AccessDeniedException("You can only edit your own notes");

        note.setComment(request.comment());
        return noteMapper.toDto(noteRepository.save(note), findUserOrThrow(securityContextHelper.getCurrentUserId()));
    }

    @Transactional(readOnly = true)
    @Override
    public ReservationResponse getReservation(Long id) {
        return toResponse(findReservationOrThrow(id));
    }

    @Transactional(readOnly = true)
    @Override
    public Page<@NonNull ReservationResponse> getReservations(ReservationFilterRequest filter, Pageable pageable) {
        return reservationRepository.findAll(ReservationSpecifications.specificationFromFilter(filter), pageable)
                .map(this::toResponse);
    }

    private Reservation findReservationOrThrow(Long id) {
        return  reservationRepository.findById(id)
                .orElseThrow(() -> new ReservationNotFoundException(id));
    }

    private Reservable findReservableOrThrow(Long id) {
        return reservableRepository.findById(id)
                .orElseThrow(() -> new ReservableNotFoundException(id));
    }

    private User findUserOrThrow(Long id) {
        return  userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    private User findUserOrThrow(String email) {
        return  userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(email));
    }

    private ReservationResponse toResponse(Reservation reservation) {
        List<NoteItem> notes = reservation.getNotes().stream()
                .map(note -> noteMapper.toDto(note, findUserOrThrow(note.getCreatedBy())))
                .toList();

        return reservationMapper.toDto(reservation, notes);
    }


    private void validateNoOverlap(Long reservableId, LocalDate date,
                                   LocalTime start, LocalTime end, Long excludeId) {
        boolean overlap = excludeId != null
                ? reservationRepository.existsOverlapExcluding(reservableId, date, start, end,
                List.of(Status.PENDING, Status.APPROVED), excludeId)
                : reservationRepository.existsOverlap(reservableId, date, start, end,
                List.of(Status.PENDING, Status.APPROVED));

        if (overlap) throw new ReservationOverlapException(date, start, end);
    }

    private void validateStudentRestrictions(User petitioner, Reservable reservable, Integer companions) {
        boolean isStudent = securityContextHelper.isAdmin();

        if (isStudent && !reservable.isStudentsAvailable())
            throw new ReservableNotAvailableForStudentsException(reservable.getId());
    }

    private void validateAdvanceTime(Reservable reservable, LocalDate date, LocalTime startTime, LocalTime endTime) {
        if (reservable.getStatus() != ReservableStatus.AVAILABLE)
            throw new ReservableNotAvailableException(reservable.getId());

        if (reservable instanceof Space space) {
            Duration bookInAdvance = space.getBookInAdvance();
            LocalDateTime requestedDateTime = LocalDateTime.of(date, startTime);
            LocalDateTime minimumAllowedDateTime = LocalDateTime.now(clock).plus(bookInAdvance);

            if (requestedDateTime.isBefore(minimumAllowedDateTime))
                throw new ReservationTooSoonException(reservable.getId(), bookInAdvance);
        }

        DayOfWeek requestedDay = date.getDayOfWeek();
        boolean withinSchedule = reservable.getAvailability().stream()
                .flatMap(slot -> slot.getMembers().stream())
                .filter(av -> av.getDayOfWeek() == requestedDay)
                .filter(av -> !date.isBefore(av.getDateFrom()))
                .filter(av -> av.getDateTo() == null || !date.isAfter(av.getDateTo()))
                .anyMatch(av ->
                        !startTime.isBefore(av.getStartTime()) &&
                                !endTime.isAfter(av.getEndTime())
                );

        if (!withinSchedule)
            throw new ReservableNotAvailableAtRequestedTimeException(reservable.getId(), date, startTime, endTime);

        boolean hasException = reservable.getAvailabilityExceptions().stream()
                .anyMatch(ex ->
                        !date.isBefore(ex.getDateFrom()) &&
                                !date.isAfter(ex.getDateTo()) &&
                                !startTime.isBefore(ex.getStartTime()) &&
                                !endTime.isAfter(ex.getEndTime())
                );

        if (hasException)
            throw new ReservableHasAvailabilityExceptionException(reservable.getId(), date, startTime, endTime);
    }
}
