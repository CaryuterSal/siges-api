package dev.spiffocode.sigesapi.users;

import dev.spiffocode.sigesapi.UnitTestClass;
import dev.spiffocode.sigesapi.users.domain.model.User;
import dev.spiffocode.sigesapi.users.domain.repository.UserRepository;
import dev.spiffocode.sigesapi.users.infrastructure.service.impl.UserDetailsServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@UnitTestClass
class UserDetailsServiceImplTest {

    @Mock
    UserRepository repo;

    @InjectMocks
    UserDetailsServiceImpl service;

    @Test
    void loadUser_ok_returnsUser() {

        User user = mock(User.class);

        when(repo.findByIdentifier("mail@test.com"))
                .thenReturn(Optional.of(user));

        UserDetails result =
                service.loadUserByUsername("mail@test.com");

        assertEquals(user, result);
    }

    @Test
    void loadUser_notFound_throws() {

        when(repo.findByIdentifier("x"))
                .thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> service.loadUserByUsername("x"));
    }
}
