package dev.spiffocode.sigesapi.users.application.mapper;

import dev.spiffocode.sigesapi.UnitTestClass;
import dev.spiffocode.sigesapi.users.domain.model.Student;
import dev.spiffocode.sigesapi.users.presentation.dto.UserResponse;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTestClass
public class UserMapperTest {

    UserMapper userMapperTest = new UserMapperImpl();

    @Test
    void test_student_maps_to_response(){
        Student student = Student.builder()
                .email("student@utez.edu.mx")
                .password("1234123")
                .firstName("Student")
                .lastName("Test")
                .birthDate(LocalDate.of(2000, 1, 1))
                .phoneNumber("+525512340002")
                .registrationNumber("20201ds002")
                .createdBy("admin@utez.edu.mx")
                .build();
        UserResponse response = userMapperTest.toResponse(student);
        assertThat(response.getRole()).isEqualTo("STUDENT");
    }
}
