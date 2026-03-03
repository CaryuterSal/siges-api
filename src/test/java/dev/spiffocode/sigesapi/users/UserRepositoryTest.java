package dev.spiffocode.sigesapi.users;

import dev.spiffocode.sigesapi.DataTestClass;
import dev.spiffocode.sigesapi.WithMockCustomUser;
import dev.spiffocode.sigesapi.users.domain.model.InstitutionalStaff;
import dev.spiffocode.sigesapi.users.domain.model.Student;
import dev.spiffocode.sigesapi.users.domain.repository.UserRepository;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataTestClass
class UserRepositoryTest {

    @Autowired
    UserRepository repo;

    private Student createStudent() {
        Student s = Student.builder()
                .email("s@mail.com")
                .phoneNumber("2222229899")
                .firstName("A")
                .lastName("B")
                .birthDate(LocalDate.of(2000,1,1))
                .password("x")
                .registrationNumber("REG1")
                .build();

        return repo.save(s);
    }

    private InstitutionalStaff createStaff() {
        InstitutionalStaff st = InstitutionalStaff.builder()
                .email("st@mail.com")
                .phoneNumber("2222222222")
                .firstName("A")
                .lastName("B")
                .birthDate(LocalDate.of(2000,1,1))
                .password("x")
                .employeeNumber("EMP1")
                .build();

        return repo.save(st);
    }


    @WithMockCustomUser(id = 42L, email = "juan@test.com", role = "ROLE_STUDENT")
    @Test
    void findByEmail() {
        createStudent();

        assertTrue(repo.findByIdentifier("s@mail.com").isPresent());
    }

    @WithMockCustomUser(id = 42L, email = "juan@test.com", role = "ROLE_STUDENT")
    @Test
    void findByPhone() {
        createStudent();

        assertTrue(repo.findByIdentifier("2222229899").isPresent());
    }

    @WithMockCustomUser(id = 42L, email = "juan@test.com", role = "ROLE_STUDENT")
    @Test
    void findByRegistrationNumber() {
        createStudent();

        assertTrue(repo.findByIdentifier("REG1").isPresent());
    }

    @WithMockCustomUser(id = 42L, email = "juan@test.com", role = "ROLE_STUDENT")
    @Test
    void findByEmployeeNumber() {
        createStaff();

        assertTrue(repo.findByIdentifier("EMP1").isPresent());
    }

    @Test
    void notFound_returnsEmpty() {
        assertTrue(repo.findByIdentifier("xxx").isEmpty());
    }

    @WithMockCustomUser(id = 42L, email = "juan@test.com", role = "ROLE_STUDENT")
    @Test
    void email_mustBeUnique() {

        Student s1 = createStudent();

        InstitutionalStaff st = InstitutionalStaff.builder()
                .email(s1.getEmail())
                .phoneNumber("2222222222")
                .firstName("A")
                .lastName("B")
                .birthDate(LocalDate.of(2000,1,1))
                .password("x")
                .employeeNumber("EMP1")
                .build();

        assertThrows(DataIntegrityViolationException.class, () -> repo.save(st));
    }

    @WithMockCustomUser(id = 42L, email = "juan@test.com", role = "ROLE_STUDENT")
    @Test
    void phoneNumber_mustBeUnique() {

        Student s1 = createStudent();

        InstitutionalStaff st = InstitutionalStaff.builder()
                .email("other@gmail.com")
                .phoneNumber(s1.getPhoneNumber())
                .firstName("A")
                .lastName("B")
                .birthDate(LocalDate.of(2000,1,1))
                .password("x")
                .employeeNumber("EMP1")
                .build();

        assertThrows(DataIntegrityViolationException.class, () -> repo.save(st));
    }

    @WithMockCustomUser(id = 42L, email = "juan@test.com", role = "ROLE_STUDENT")
    @Test
    void registrationNumber_mustBeUnique() {

        Student s1 = createStudent();

        Student s = Student.builder()
                .email("other@gmail.com")
                .firstName("A")
                .lastName("B")
                .birthDate(LocalDate.of(2000,1,1))
                .password("x")
                .registrationNumber(s1.getRegistrationNumber())
                .build();
        assertThrows(ConstraintViolationException.class, () -> repo.save(s));
    }

    @WithMockCustomUser(id = 42L, email = "juan@test.com", role = "ROLE_STUDENT")
    @Test
    void employeeNumber_mustBeUnique() {

        InstitutionalStaff s1 = createStaff();

        InstitutionalStaff st = InstitutionalStaff.builder()
                .email("other@gmail.com")
                .phoneNumber("22284928193")
                .firstName("A")
                .lastName("B")
                .birthDate(LocalDate.of(2000,1,1))
                .password("x")
                .employeeNumber(s1.getEmployeeNumber())
                .build();

        assertThrows(ConstraintViolationException.class, () -> repo.save(st));
    }

}
