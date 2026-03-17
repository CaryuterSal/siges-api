package dev.spiffocode.sigesapi.reservations.infrastructure.service.impl;

import dev.spiffocode.sigesapi.auth.infrastructure.SecurityContextHelper;
import dev.spiffocode.sigesapi.notifications.application.service.NotificationsPort;
import dev.spiffocode.sigesapi.notifications.application.service.SendNotificationCommand;
import dev.spiffocode.sigesapi.notifications.domain.model.Type;
import dev.spiffocode.sigesapi.reservables.domain.exception.ReservableNotFoundException;
import dev.spiffocode.sigesapi.reservables.domain.model.Reservable;
import dev.spiffocode.sigesapi.reservables.domain.model.ReservableStatus;
import dev.spiffocode.sigesapi.reservables.domain.repository.ReservableRepository;
import dev.spiffocode.sigesapi.reservations.application.mapper.NoteMapper;
import dev.spiffocode.sigesapi.reservations.application.mapper.ReservationMapper;
import dev.spiffocode.sigesapi.reservations.application.service.ReservationService;
import dev.spiffocode.sigesapi.reservations.domain.exception.InvalidReservationStatusException;
import dev.spiffocode.sigesapi.reservations.domain.exception.NoteNotFoundException;
import dev.spiffocode.sigesapi.reservations.domain.exception.ReservationNotFoundException;
import dev.spiffocode.sigesapi.reservations.domain.exception.ReservationOverlapException;
import dev.spiffocode.sigesapi.reservations.domain.model.Note;
import dev.spiffocode.sigesapi.reservations.domain.model.Reservation;
import dev.spiffocode.sigesapi.reservations.domain.model.Status;
import dev.spiffocode.sigesapi.reservations.domain.repository.NoteRepository;
import dev.spiffocode.sigesapi.reservations.domain.repository.ReservationRepository;
import dev.spiffocode.sigesapi.reservations.domain.specifications.ReservationSpecifications;
import dev.spiffocode.sigesapi.reservations.presentation.*;
import dev.spiffocode.sigesapi.users.domain.exception.UserNotFoundException;
import dev.spiffocode.sigesapi.users.domain.model.Applicant;
import dev.spiffocode.sigesapi.users.domain.model.Student;
import dev.spiffocode.sigesapi.users.domain.model.User;
import dev.spiffocode.sigesapi.users.domain.repository.ApplicantRepository;
import dev.spiffocode.sigesapi.users.domain.repository.UserRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

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

    private final ReservationMapper reservationMapper;
    private final Clock clock;
    private final NoteMapper noteMapper;
    private final ApplicantRepository applicantRepository;

    @Override
        public ReservationResponse createReservation(CreateReservationRequest request) {
                Long userId = securityContextHelper.getCurrentUserId();

                Reservable reservable = findReservableOrThrow(request.reservableId());
                Applicant petitioner = findApplicantOrThrow(userId);

                boolean petitionerIsStudent = petitioner.getClass().equals(Student.class);

                reservable.assertCanDoReservation(request.date(), request.startTime(), request.endTime(), petitionerIsStudent, clock);
                validateNoOverlap(reservable, request.date(), request.startTime(), request.endTime());

                Reservation reservation = reservationMapper.toEntity(request, petitioner, reservable);

                Reservation saved = reservationRepository.save(reservation);
                notificationsPort.sendNotification(petitioner.getId(), SendNotificationCommand.builder()
                                .type(Type.RESERVATION_CREATED)
                                .entityId(saved.getId())
                                .metadata(Map.of(
                                        "reservationId", saved.getId().toString(),
                                        "reservableId", reservable.getId().toString()))
                                .build());
                notificationsPort.sendNotificationToAdmins(SendNotificationCommand.builder()
                                .type(Type.RESERVATION_CREATED)
                                .entityId(saved.getId())
                                .metadata(Map.of(
                                        "reservationId", saved.getId().toString(),
                                        "reservableId", reservable.getId().toString()))
                                .build());
                return reservationMapper.toDto(saved, List.of());
        }

        @Override
        public ReservationResponse rescheduleReservation(Long id, RescheduleReservationRequest request) {
                Long userId = securityContextHelper.getCurrentUserId();
                Reservation reservation = findReservationOrThrow(id);

                if (reservation.getStatus() != Status.PENDING && reservation.getStatus() != Status.APPROVED)
                        throw new InvalidReservationStatusException(reservation.getStatus(), Status.PENDING);

                reservation.getReservable().assertAvailabilityAllowsReservation(request.date(), request.startTime(), request.endTime());
                validateNoOverlap(reservation.getReservable(), request.date(),
                                request.startTime(), request.endTime(), id);

                reservation.reschedule(request.date(), request.startTime(), request.endTime(), clock);

                notificationsPort.sendNotification(reservation.getPetitioner().getId(), SendNotificationCommand
                                .builder()
                                .type(Type.RESERVATION_RESCHEDULE)
                                .entityId(reservation.getId())
                                .metadata(Map.of("reservationId", reservation.getId().toString(), "reservableId",
                                                reservation.getReservable().getId().toString()))
                                .build());

                notificationsPort.sendNotificationToAdmins(SendNotificationCommand.builder()
                                .type(Type.RESERVATION_RESCHEDULE)
                                .entityId(reservation.getId())
                                .metadata(Map.of("reservationId", reservation.getId().toString(), "reservableId",
                                                reservation.getReservable().getId().toString()))
                                .build());

                return toResponse(reservationRepository.save(reservation));
        }

        // ─────────────────────────────────────────────
        // STATUS CHANGES
        // ─────────────────────────────────────────────

        @Override
        public ReservationResponse approveReservation(Long id) {
                Reservation reservation = findReservationOrThrow(id);
                reservation.approve(clock);

                notificationsPort.sendNotification(reservation.getPetitioner().getId(),
                                SendNotificationCommand.builder()
                                                .type(Type.RESERVATION_APPROVED)
                                                .entityId(id)
                                                .metadata(Map.of("reservationId", id.toString(), "reservableId",
                                                                reservation.getReservable().getId().toString()))
                                                .build());

                return toResponse(reservationRepository.save(reservation));
        }

        @Override
        public ReservationResponse rejectReservation(Long id, RejectReservationRequest request) {
                Reservation reservation = findReservationOrThrow(id);
                reservation.reject(request.reason(), clock);

                notificationsPort.sendNotification(reservation.getPetitioner().getId(),
                                SendNotificationCommand.builder()
                                                .type(Type.RESERVATION_REJECTED)
                                                .entityId(id)
                                                .metadata(Map.of("reservationId", id.toString(), "reservableId",
                                                                reservation.getReservable().getId().toString()))
                                                .build());

                return toResponse(reservationRepository.save(reservation));
        }

        @Override
        public ReservationResponse cancelReservation(Long id, CancelReservationRequest request) {
                Long userId = securityContextHelper.getCurrentUserId();
                boolean isAdmin = securityContextHelper.isAdmin();
                Reservation reservation = findReservationOrThrow(id);

                boolean isPetitioner = reservation.getPetitioner().getId().equals(userId);
                if (!isAdmin && !isPetitioner) {
                        throw new AccessDeniedException("Only the petitioner or an admin can cancel a reservation");
                }

                reservation.cancel(request.reason(), clock);

                notificationsPort.sendNotification(reservation.getPetitioner().getId(),
                                SendNotificationCommand.builder()
                                                .type(Type.RESERVATION_CANCELLED)
                                                .entityId(id)
                                                .metadata(Map.of("reservationId", id.toString(), "reservableId",
                                                                reservation.getReservable().getId().toString()))
                                                .build());

                if (isAdmin) {
                        notificationsPort.sendNotificationToAdmins(SendNotificationCommand.builder()
                                        .type(Type.RESERVATION_CANCELLED)
                                        .entityId(id)
                                        .metadata(Map.of("reservationId", id.toString(), "reservableId",
                                                        reservation.getReservable().getId().toString()))
                                        .build());
                }

                return toResponse(reservationRepository.save(reservation));
        }

        @Override
        public ReservationResponse startReservation(Long id) {
            Reservation reservation = findReservationOrThrow(id);
            reservation.start(clock);
            reservation.getReservable().setStatus(ReservableStatus.LOANED);
            reservableRepository.save(reservation.getReservable());
            return toResponse(reservationRepository.save(reservation));
        }

        @Override
        public ReservationResponse finishReservation(Long id) {
            Reservation reservation = findReservationOrThrow(id);
            reservation.finish(clock);
            reservation.getReservable().setStatus(ReservableStatus.AVAILABLE);
            reservableRepository.save(reservation.getReservable());
            return toResponse(reservationRepository.save(reservation));
        }

        // ─────────────────────────────────────────────
        // NOTES
        // ─────────────────────────────────────────────

        @Override
        public ReservationResponse addNote(Long reservationId, PublishNoteRequest request) {
                Reservation reservation = findReservationOrThrow(reservationId);
                reservation.addNote(request.comment());

                boolean isAdmin = securityContextHelper.isAdmin();

                SendNotificationCommand command = SendNotificationCommand.builder()
                                .type(Type.COMMENT_ON_RESERVATION)
                                .entityId(reservationId)
                                .metadata(Map.of("reservationId", reservationId.toString(), "reservableId",
                                                reservation.getReservable().getId().toString()))
                                .build();

                if (isAdmin) {
                        notificationsPort.sendNotification(reservation.getPetitioner().getId(), command);
                } else {
                        notificationsPort.sendNotificationToAdmins(command);
                }

                return toResponse(reservationRepository.save(reservation));
        }

        @Override
        public NoteItem editNote(Long reservationId, Long noteId, EditNoteRequest request) {
                Note note = noteRepository.findByIdAndReservationId(noteId, reservationId)
                                .orElseThrow(() -> new NoteNotFoundException(noteId));

                String userId = securityContextHelper.getCurrentUserEmail();
                boolean isAdmin = securityContextHelper.isAdmin();

                if (!isAdmin && !note.getCreatedBy().equals(userId))
                        throw new AccessDeniedException("You can only edit your own notes");

                note.setComment(request.comment());
                return noteMapper.toDto(noteRepository.save(note),
                                findUserOrThrow(securityContextHelper.getCurrentUserId()));
        }

        @PostAuthorize("hasRole('ADMIN') or @securityContextHelper.isCurrentUser(returnObject.petitioner.id)")
        @Transactional(readOnly = true)
        @Override
        public ReservationResponse getReservation(Long id) {
                return toResponse(findReservationOrThrow(id));
        }

        @Transactional(readOnly = true)
        @Override
        public Page<@NonNull ReservationResponse> getReservations(ReservationFilterRequest filter, Pageable pageable) {
                return reservationRepository
                                .findAll(ReservationSpecifications.specificationFromFilter(filter, securityContextHelper.getCurrentUserId(), securityContextHelper.isApplicant()), pageable)
                                .map(this::toResponse);
        }

        private Reservation findReservationOrThrow(Long id) {
                return reservationRepository.findById(id)
                                .orElseThrow(() -> new ReservationNotFoundException(id));
        }

        private Reservable findReservableOrThrow(Long id) {
                return reservableRepository.findById(id)
                                .orElseThrow(() -> new ReservableNotFoundException(id));
        }

        private User findUserOrThrow(Long id) {
                return userRepository.findById(id)
                                .orElseThrow(() -> new UserNotFoundException(id));
        }

        private User findUserOrThrow(String email) {
                return userRepository.findByEmail(email)
                                .orElseThrow(() -> new UserNotFoundException(email));
        }

        private Applicant findApplicantOrThrow(Long id) {
            return applicantRepository.findById(id)
                    .orElseThrow(() -> new UserNotFoundException(id));
        }

        private ReservationResponse toResponse(Reservation reservation) {
                List<NoteItem> notes = reservation.getNotes().stream()
                                .map(note -> noteMapper.toDto(note, findUserOrThrow(note.getCreatedBy())))
                                .toList();

                return reservationMapper.toDto(reservation, notes);
        }

        private void validateNoOverlap(Reservable reservable, LocalDate date,
                        LocalTime start, LocalTime end) {
                boolean overlap = reservationRepository.existsOverlap(reservable, date, start, end,
                                                List.of(Status.PENDING, Status.APPROVED));

                if (overlap) throw new ReservationOverlapException(date, start, end);
        }

        private void validateNoOverlap(Reservable reservable, LocalDate date,
                                       LocalTime start, LocalTime end, @NonNull Long excludeId) {
            boolean overlap = reservationRepository.existsOverlapExcluding(reservable, date, start, end,
                    List.of(Status.PENDING, Status.APPROVED), excludeId);

            if (overlap) throw new ReservationOverlapException(date, start, end);
        }
}
