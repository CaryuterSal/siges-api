package dev.spiffocode.sigesapi.reservations.infrastructure.service.impl;

import dev.spiffocode.sigesapi.auth.infrastructure.SecurityContextHelper;
import dev.spiffocode.sigesapi.mailsender.application.service.ReservationsEmailPort;
import dev.spiffocode.sigesapi.notifications.application.service.NotificationsPort;
import dev.spiffocode.sigesapi.notifications.application.service.SendNotificationCommand;
import dev.spiffocode.sigesapi.notifications.domain.model.Type;
import dev.spiffocode.sigesapi.reservables.domain.exception.ReservableNotFoundException;
import dev.spiffocode.sigesapi.reservables.domain.model.Reservable;
import dev.spiffocode.sigesapi.reservables.domain.repository.ReservableRepository;
import dev.spiffocode.sigesapi.reservations.application.service.ReservationService;
import dev.spiffocode.sigesapi.reservations.domain.exception.ReservationOverlapException;
import dev.spiffocode.sigesapi.reservations.domain.model.Reservation;
import dev.spiffocode.sigesapi.reservations.domain.model.Status;
import dev.spiffocode.sigesapi.reservations.domain.repository.ReservationRepository;
import dev.spiffocode.sigesapi.reservations.presentation.ChangeReservationStatusRequest;
import dev.spiffocode.sigesapi.reservations.presentation.CreateReservationRequest;
import dev.spiffocode.sigesapi.reservations.presentation.RescheduleReservationRequest;
import dev.spiffocode.sigesapi.reservations.presentation.ReservationResponse;
import dev.spiffocode.sigesapi.users.domain.exception.UserNotFoundException;
import dev.spiffocode.sigesapi.users.domain.model.User;
import dev.spiffocode.sigesapi.users.domain.repository.ApplicantRepository;
import dev.spiffocode.sigesapi.users.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private final ReservableRepository reservableRepository;
    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final ApplicantRepository applicantRepository;

    private final SecurityContextHelper securityContextHelper;
    private final NotificationsPort notificationsPort;
    private final ReservationsEmailPort emailPort;

    @Override
    public ReservationResponse createReservation(CreateReservationRequest request) {
        Long userId = securityContextHelper.getCurrentUserId();

        Reservable reservable = reservableRepository.findById(request.reservableId())
                .orElseThrow(() -> new ReservableNotFoundException(request.reservableId()));
        User petitioner = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        validateAdvanceTime(reservable, request.date(), request.startTime());
        validateStudentRestrictions(petitioner, reservable, request.attendees());

        if (reservationRepository.existsOverlap(
                request.reservableId(), request.date(), request.startTime(), request.endTime(),
                List.of(Status.PENDING, Status.APPROVED)))
            throw new ReservationOverlapException(request.date(), request.startTime(), request.endTime());

        Reservation reservation = Reservation.builder()
                .petitioner(petitioner)
                .reservable(reservable)
                .date(request.date())
                .startTime(request.startTime())
                .endTime(request.endTime())
                .build();

        Reservation saved = reservationRepository.save(reservation);
        notificationsPort.sendNotificationToAdmins(SendNotificationCommand.builder().type(Type.RESERVATION_CREATED).build());
        emailPort.sendReservationCreatedEmail(petitioner.getEmail(), reservation.getId());
        return saved;
    }

    @Override
    public ReservationResponse createReservation(CreateReservationRequest request) {
        return null;
    }

    @Override
    public ReservationResponse rescheduleReservation(Long id, RescheduleReservationRequest request) {
        return null;
    }

    @Override
    public void changeReservationStatus(Long id, ChangeReservationStatusRequest request) {

    }
}
