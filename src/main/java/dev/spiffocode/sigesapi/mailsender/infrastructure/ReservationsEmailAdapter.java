package dev.spiffocode.sigesapi.mailsender.infrastructure;

import dev.spiffocode.sigesapi.mailsender.application.service.ReservationsEmailPort;
import dev.spiffocode.sigesapi.mailsender.application.service.SpecificEmailPort;
import dev.spiffocode.sigesapi.reservations.domain.model.Status;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationsEmailAdapter implements ReservationsEmailPort {

    private final SpecificEmailPort emailPort;

    @Async("asyncExecutor")
    @Override
    public void sendReservationCreatedEmail(String email, long reservationId) {
        Context ctx = new Context();
        ctx.setVariable("reservationId", reservationId);
        emailPort.sendHtml(email, "Reserva creada exitosamente - SIGES", "email/reservation-created", ctx);
    }

    @Async("asyncExecutor")
    @Override
    public void sendNewReservationRequestEmail(String email, String petitionerName, long reservationId) {
        Context ctx = new Context();
        ctx.setVariable("reservationId", reservationId);
        ctx.setVariable("petitionerName", petitionerName);
        emailPort.sendHtml(email, "Nueva solicitud de reservación - SIGES", "email/new-reservation-request", ctx);
    }

    @Async("asyncExecutor")
    @Override
    public void sendReservationResolutionEmail(String email, Status status, long reservationId) {
        Context ctx = new Context();
        ctx.setVariable("reservationId", reservationId);
        ctx.setVariable("status", status.name());

        String subject = status == Status.APPROVED
                ? "Reserva aprobada - SIGES"
                : "Reserva rechazada - SIGES";

        emailPort.sendHtml(email, subject, "email/reservation-resolution", ctx);
    }

    @Async("asyncExecutor")
    @Override
    public void sendReservationCancelledEmail(String email, long reservationId) {
        Context ctx = new Context();
        ctx.setVariable("reservationId", reservationId);
        emailPort.sendHtml(email, "Reserva cancelada - SIGES", "email/reservation-cancelled", ctx);
    }

    @Async("asyncExecutor")
    @Override
    public void sendReservationRescheduledEmail(String email, long reservationId) {
        Context ctx = new Context();
        ctx.setVariable("reservationId", reservationId);
        emailPort.sendHtml(email, "Reserva reprogramada - SIGES", "email/reservation-rescheduled", ctx);
    }
}
