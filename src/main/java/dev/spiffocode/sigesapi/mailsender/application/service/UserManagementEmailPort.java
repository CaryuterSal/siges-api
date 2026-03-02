package dev.spiffocode.sigesapi.mailsender.application.service;

public interface UserManagementEmailPort {
    void sendEmployeeNumberChangeEmail(String email);
    void sendStudentRegistrationNumberChangeEmail(String email);
    void sendEmailChangeEmail(String fromEmail, String toEmail);
    void sendWelcomeEmail(String email);
    void sendGoodbyeEmail(String email);
    void sendPasswordChangedEmail(String email);
    void sendRecoveryEmail(String email);
}
