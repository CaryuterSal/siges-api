package dev.spiffocode.sigesapi.mailsender.infrastructure;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import dev.spiffocode.sigesapi.mailsender.application.service.SpecificEmailPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Component
@Slf4j
@RequiredArgsConstructor
public class ResendEmailAdapter implements SpecificEmailPort {


    private final SpringTemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String from;

    @Value("${spring.mail.password}")
    private String apiKey;

    @Retryable
    public void sendHtml(String to, String subject, String template, Context ctx) {
        String htmlContent = templateEngine.process(template, ctx);
        log.info("\n=== OUTGOING EMAIL ===\nTo: {}\nSubject: {}\nTemplate: {}\n=======================", to, subject,
                template);
        try {
            Resend resend = new Resend(apiKey);
            CreateEmailOptions params = CreateEmailOptions.builder()
                    .from(from)
                    .to(to)
                    .subject(subject)
                    .html(htmlContent)
                    .build();
            resend.emails().send(params);
        } catch (ResendException e) {
            log.error("Error al enviar correo '{}' a {}: {}", subject, to, e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
