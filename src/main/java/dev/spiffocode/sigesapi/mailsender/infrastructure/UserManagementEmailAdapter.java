package dev.spiffocode.sigesapi.mailsender.infrastructure;

import dev.spiffocode.sigesapi.mailsender.application.service.UserManagementEmailPort;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserManagementEmailAdapter implements UserManagementEmailPort {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String from;

    @Async("asyncExecutor")
    @Override
    public void sendAdminWelcomeEmail(String email, String name, String generatedPassword) {
        sendWelcome(email, name, generatedPassword, "Administrador");
    }

    @Async("asyncExecutor")
    @Override
    public void sendStudentWelcomeEmail(String email, String name, String generatedPassword) {
        sendWelcome(email, name, generatedPassword, "Estudiante");
    }

    @Async("asyncExecutor")
    @Override
    public void sendInstitutionalStaffWelcomeEmail(String email, String name, String generatedPassword) {
        sendWelcome(email, name, generatedPassword, "Personal Institucional");
    }

    @Async("asyncExecutor")
    @Override
    public void sendRecoveryEmail(String email, String fullName, String token, String recoveryUrl) {
        Context ctx = new Context();
        ctx.setVariable("name", fullName);
        ctx.setVariable("token", token);
        ctx.setVariable("recoveryUrl", "https://tu-api.com/api/v1/password-recovery/redirect?token=" + token);
        sendHtml(email, "Recuperación de contraseña - SIGES", "email/recovery", ctx);
    }

    @Async("asyncExecutor")
    @Override
    public void sendEmailChangeEmail(String fromEmail, String toEmail, String name) {
        Context ctx = new Context();
        ctx.setVariable("name", name);
        ctx.setVariable("label", "Correo");
        ctx.setVariable("oldEmail", fromEmail);
        ctx.setVariable("newEmail", toEmail);
        sendHtml(toEmail, "Tu correo ha sido actualizado - SIGES", "email/email-change", ctx);
    }

    @Async("asyncExecutor")
    @Override
    public void sendPasswordChangedEmail(String email, String name) {
        Context ctx = new Context();
        ctx.setVariable("name", name);
        sendHtml(email, "Tu contraseña ha sido actualizada - SIGES", "email/password-changed", ctx);
    }

    @Async("asyncExecutor")
    @Override
    public void sendGoodbyeEmail(String email, String name) {
        Context ctx = new Context();
        ctx.setVariable("name", name);
        sendHtml(email, "Tu cuenta ha sido desactivada - SIGES", "email/goodbye", ctx);
    }

    @Async("asyncExecutor")
    @Override
    public void sendAccountRestoredEmail(String email, String name) {
        Context ctx = new Context();
        ctx.setVariable("name", name);
        sendHtml(email, "Tu cuenta ha sido restaurada - SIGES", "email/account-restored", ctx);
    }

    @Async("asyncExecutor")
    @Override
    public void sendEmployeeNumberChangeEmail(String email, String name, String oldNumber, String newNumber) {
        Context ctx = new Context();
        ctx.setVariable("name", name);
        ctx.setVariable("label", "Número de empleado");
        ctx.setVariable("oldNumber", oldNumber);
        ctx.setVariable("newNumber", newNumber);
        sendHtml(email, "Tu número de empleado ha sido actualizado - SIGES", "email/identifier-change", ctx);
    }

    @Async("asyncExecutor")
    @Override
    public void sendStudentRegistrationNumberChangeEmail(String email, String name, String oldNumber, String newNumber) {
        Context ctx = new Context();
        ctx.setVariable("name", name);
        ctx.setVariable("label", "Matrícula");
        ctx.setVariable("oldNumber", oldNumber);
        ctx.setVariable("newNumber", newNumber);
        sendHtml(email, "Tu matrícula ha sido actualizada - SIGES", "email/identifier-change", ctx);
    }

    private void sendWelcome(String email, String name, String password, String role) {
        Context ctx = new Context();
        ctx.setVariable("name", name);
        ctx.setVariable("email", email);
        ctx.setVariable("password", password);
        ctx.setVariable("role", role);
        sendHtml(email, "Bienvenido a SIGES", "email/welcome", ctx);
    }

    @Retryable(MessagingException.class)
    private void sendHtml(String to, String subject, String template, Context ctx) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(templateEngine.process(template, ctx), true);
            mailSender.send(message);
            log.info("Correo '{}' enviado a {}", subject, to);
        } catch (MessagingException e) {
            log.error("Error al enviar correo '{}' a {}: {}", subject, to, e.getMessage());
        }
    }
}