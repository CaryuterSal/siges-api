package dev.spiffocode.sigesapi.users;

import dev.spiffocode.sigesapi.DataTestClass;
import dev.spiffocode.sigesapi.users.model.InstitutionalStaff;
import dev.spiffocode.sigesapi.users.model.Student;
import dev.spiffocode.sigesapi.users.repository.UserRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataTestClass
class UserRepositoryTest {

    @Autowired
    UserRepository repo;

    @Autowired
    TestEntityManager em;


    private Student createStudent() {
        Student s = new Student();
        s.setEmail("s@mail.com");
        s.setPhoneNumber("1111111111");
        s.setFirstName("A");
        s.setLastName("B");
        s.setBirthDate(LocalDate.of(2000,1,1));
        s.setPassword("x");
        s.setRegistrationNumber("REG1");

        return em.persistAndFlush(s);
    }

    private InstitutionalStaff createStaff() {
        InstitutionalStaff st = new InstitutionalStaff();
        st.setEmail("st@mail.com");
        st.setPhoneNumber("2222222222");
        st.setFirstName("A");
        st.setLastName("B");
        st.setBirthDate(LocalDate.of(2000,1,1));
        st.setPassword("x");
        st.setEmployeeNumber("EMP1");

        return em.persistAndFlush(st);
    }


    @Test
    void findByEmail() {
        createStudent();

        assertTrue(repo.findByIdentifier("s@mail.com").isPresent());
    }

    @Test
    void findByPhone() {
        createStudent();

        assertTrue(repo.findByIdentifier("1111111111").isPresent());
    }

    @Test
    void findByRegistrationNumber() {
        createStudent();

        assertTrue(repo.findByIdentifier("REG1").isPresent());
    }

    @Test
    void findByEmployeeNumber() {
        createStaff();

        assertTrue(repo.findByIdentifier("EMP1").isPresent());
    }

    @Test
    void notFound_returnsEmpty() {
        assertTrue(repo.findByIdentifier("xxx").isEmpty());
    }

    @Test
    void email_mustBeUnique() {

        Student s1 = createStudent();

        InstitutionalStaff st = new InstitutionalStaff();
        st.setEmail(s1.getEmail());
        st.setPhoneNumber("2222222222");
        st.setFirstName("A");
        st.setLastName("B");
        st.setBirthDate(LocalDate.of(2000,1,1));
        st.setPassword("x");

        assertThrows(ConstraintViolationException.class, () -> {
            em.persistAndFlush(st);
        });
    }

    @Test
    void phoneNumber_mustBeUnique() {

        Student s1 = createStudent();

        InstitutionalStaff st = new InstitutionalStaff();
        st.setEmail("other@gmail.com");
        st.setPhoneNumber(s1.getPhoneNumber());
        st.setFirstName("A");
        st.setLastName("B");
        st.setBirthDate(LocalDate.of(2000,1,1));
        st.setPassword("x");

        assertThrows(ConstraintViolationException.class, () -> {
            em.persistAndFlush(st);
        });
    }

    @Test
    void registrationNumber_mustBeUnique() {

        Student s1 = createStudent();

        Student s = new Student();
        s.setEmail("other@gmail.com");
        s.setPhoneNumber("1111111122");
        s.setFirstName("A");
        s.setLastName("B");
        s.setBirthDate(LocalDate.of(2000,1,1));
        s.setPassword("x");
        s.setRegistrationNumber(s1.getRegistrationNumber());

        assertThrows(ConstraintViolationException.class, () -> {
            em.persistAndFlush(s);
        });
    }

    @Test
    void employeeNumber_mustBeUnique() {

        InstitutionalStaff s1 = createStaff();

        InstitutionalStaff st = new InstitutionalStaff();
        st.setEmail("other@gmail.com");
        st.setPhoneNumber("1111211111");
        st.setFirstName("A");
        st.setLastName("B");
        st.setBirthDate(LocalDate.of(2000,1,1));
        st.setPassword("x");
        st.setEmployeeNumber(st.getEmployeeNumber());

        assertThrows(ConstraintViolationException.class, () -> {
            em.persistAndFlush(st);
        });
    }

}
