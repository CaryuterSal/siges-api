package dev.spiffocode.sigesapi.mailsender.application.service;

public interface UserManagementEmailPort {
    void sendEmployeeNumberChangeEmail(String email, String name, String oldEmployeeNumber, String newEmployeeNumber);
    void sendStudentRegistrationNumberChangeEmail(String email, String name, String oldStudentNumber, String newStudentNumber);
    void sendEmailChangeEmail(String fromEmail, String toEmail, String name);
    void sendAdminWelcomeEmail(String email, String name, String generatedPassword);
    void sendStudentWelcomeEmail(String email, String name, String generatedPassword);
    void sendInstitutionalStaffWelcomeEmail(String email, String name, String generatedPassword);
    void sendGoodbyeEmail(String email, String name);
    void sendAccountRestoredEmail(String email, String name);
    void sendPasswordChangedEmail(String email, String name);
    void sendRecoveryEmail(String email, String token);
}
