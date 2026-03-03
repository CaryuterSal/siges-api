package dev.spiffocode.sigesapi.common.infrastructure.persistence;

import dev.spiffocode.sigesapi.users.domain.model.Admin;
import dev.spiffocode.sigesapi.users.domain.model.InstitutionalStaff;
import dev.spiffocode.sigesapi.users.domain.model.Student;
import dev.spiffocode.sigesapi.users.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@Profile("dev")
@RequiredArgsConstructor
@Slf4j
public class DevDataSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.dev.test.admin.email:admin@example.com}")
    private String testAdminEmail;

    @Value("${app.dev.test.student.email:student@example.com}")
    private String testStudentEmail;

    @Value("${app.dev.test.staff.email:staff@example.com}")
    private String testStaffEmail;

    @Override
    public void run(ApplicationArguments args) {
        log.info("TEST ADMIN EMAIL IS {}", testAdminEmail);
        log.info("TEST STUDENT EMAIL IS {}", testStudentEmail);
        log.info("TEST STAFF EMAIL IS {}", testStaffEmail);
        seedAdmin();
        seedStudent();
        seedInstitutionalStaff();
    }

    private void seedAdmin() {
        String email = testAdminEmail;
        if (userRepository.existsByEmail(email)) return;

        Admin admin = Admin.builder()
                .email(email)
                .phoneNumber("+52 555 000 1878")
                .firstName("Admin")
                .lastName("Dev")
                .createdBy("anonymousUser")
                .birthDate(LocalDate.of(1990, 1, 15))
                .password(passwordEncoder.encode("password123"))
                .build();

        userRepository.save(admin);
        log.info("✅ Dev Admin seeded: {}", email);
    }

    private void seedStudent() {
        String email = testStudentEmail;
        if (userRepository.existsByEmail(email)) return;

        Student student = Student.builder()
                .email(email)
                .phoneNumber("+52 555 000 0002")
                .firstName("Student")
                .lastName("Dev")
                .createdBy("anonymousUser")
                .birthDate(LocalDate.of(2000, 6, 20))
                .password(passwordEncoder.encode("password123"))
                .registrationNumber("REG-2024-001")
                .build();

        userRepository.save(student);
        log.info("✅ Dev Student seeded: {}", email);
    }

    private void seedInstitutionalStaff() {
        String email = testAdminEmail;
        if (userRepository.existsByEmail(email)) return;

        InstitutionalStaff staff = InstitutionalStaff.builder()
                .email(email)
                .phoneNumber("+52 555 000 0003")
                .firstName("Staff")
                .lastName("Dev")
                .createdBy("anonymousUser")
                .birthDate(LocalDate.of(1985, 3, 10))
                .password(passwordEncoder.encode("password123"))
                .employeeNumber("EMP-2024-001")
                .build();

        userRepository.save(staff);
        log.info("✅ Dev InstitutionalStaff seeded: {}", email);
    }
}

