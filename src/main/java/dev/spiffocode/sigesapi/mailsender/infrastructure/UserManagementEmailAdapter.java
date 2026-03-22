package dev.spiffocode.sigesapi.mailsender.infrastructure;

import dev.spiffocode.sigesapi.mailsender.application.service.SpecificEmailPort;
import dev.spiffocode.sigesapi.mailsender.application.service.UserManagementEmailPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserManagementEmailAdapter implements UserManagementEmailPort {

    SpecificEmailPort emailPort;

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
        ctx.setVariable("recoveryUrl", recoveryUrl);
        emailPort.sendHtml(email, "Recuperación de contraseña - SIGES", "email/recovery", ctx);
    }

    @Async("asyncExecutor")
    @Override
    public void sendEmailChangeEmail(String fromEmail, String toEmail, String name) {
        Context ctx = new Context();
        ctx.setVariable("name", name);
        ctx.setVariable("label", "Correo");
        ctx.setVariable("oldEmail", fromEmail);
        ctx.setVariable("newEmail", toEmail);
        emailPort.sendHtml(toEmail, "Tu correo ha sido actualizado - SIGES", "email/email-change", ctx);
    }

    @Async("asyncExecutor")
    @Override
    public void sendPasswordChangedEmail(String email, String name) {
        log.debug("Sending password changed email");
        Context ctx = new Context();
        ctx.setVariable("name", name);
        emailPort.sendHtml(email, "Tu contraseña ha sido actualizada - SIGES", "email/password-changed", ctx);
    }

    @Async("asyncExecutor")
    @Override
    public void sendGoodbyeEmail(String email, String name) {
        Context ctx = new Context();
        ctx.setVariable("name", name);
        emailPort.sendHtml(email, "Tu cuenta ha sido desactivada - SIGES", "email/goodbye", ctx);
    }

    @Async("asyncExecutor")
    @Override
    public void sendAccountRestoredEmail(String email, String name) {
        Context ctx = new Context();
        ctx.setVariable("name", name);
        emailPort.sendHtml(email, "Tu cuenta ha sido restaurada - SIGES", "email/account-restored", ctx);
    }

    @Async("asyncExecutor")
    @Override
    public void sendEmployeeNumberChangeEmail(String email, String name, String oldNumber, String newNumber) {
        Context ctx = new Context();
        ctx.setVariable("name", name);
        ctx.setVariable("label", "Número de empleado");
        ctx.setVariable("oldNumber", oldNumber);
        ctx.setVariable("newNumber", newNumber);
        emailPort.sendHtml(email, "Tu número de empleado ha sido actualizado - SIGES", "email/identifier-change", ctx);
    }

    @Async("asyncExecutor")
    @Override
    public void sendStudentRegistrationNumberChangeEmail(String email, String name, String oldNumber,
            String newNumber) {
        Context ctx = new Context();
        ctx.setVariable("name", name);
        ctx.setVariable("label", "Matrícula");
        ctx.setVariable("oldNumber", oldNumber);
        ctx.setVariable("newNumber", newNumber);
        emailPort.sendHtml(email, "Tu matrícula ha sido actualizada - SIGES", "email/identifier-change", ctx);
    }

    private void sendWelcome(String email, String name, String password, String role) {
        Context ctx = new Context();
        ctx.setVariable("name", name);
        ctx.setVariable("email", email);
        ctx.setVariable("password", password);
        ctx.setVariable("role", role);
        emailPort.sendHtml(email, "Bienvenido a SIGES", "email/welcome", ctx);
    }
}