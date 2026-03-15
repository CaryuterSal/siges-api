package dev.spiffocode.sigesapi.mailsender.infrastructure;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import dev.spiffocode.sigesapi.mailsender.application.service.ReservationsEmailPort;
import dev.spiffocode.sigesapi.reservations.domain.model.Status;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationsEmailAdapter implements ReservationsEmailPort {

    private final SpringTemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String from;

    @Value("${spring.mail.password}")
    private String apiKey;

    @Async("asyncExecutor")
    @Override
    public void sendReservationCreatedEmail(String email, long reservationId) {
        Context ctx = new Context();
        ctx.setVariable("reservationId", reservationId);
        sendHtml(email, "Reserva creada exitosamente - SIGES", "email/reservation-created", ctx);
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

        sendHtml(email, subject, "email/reservation-resolution", ctx);
    }

    @Async("asyncExecutor")
    @Override
    public void sendReservationCancelledEmail(String email, long reservationId) {
        Context ctx = new Context();
        ctx.setVariable("reservationId", reservationId);
        sendHtml(email, "Reserva cancelada - SIGES", "email/reservation-cancelled", ctx);
    }

    @Async("asyncExecutor")
    @Override
    public void sendReservationRescheduledEmail(String email, long reservationId) {
        Context ctx = new Context();
        ctx.setVariable("reservationId", reservationId);
        sendHtml(email, "Reserva reprogramada - SIGES", "email/reservation-rescheduled", ctx);
    }

    @Retryable
    private void sendHtml(String to, String subject, String template, Context ctx) {
        log.debug("Sending email to {}", to);
        try {
            Resend resend = new Resend(apiKey);
            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from(from)
                    .to(to)
                    .subject(subject)
                    .html(templateEngine.process(template, ctx))
                    .build();
            resend.emails().send(params);
            log.info("Correo '{}' enviado a {}", subject, to);
        } catch (ResendException e) {
            log.error("Error al enviar correo '{}' a {}: {}", subject, to, e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
