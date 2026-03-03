package dev.spiffocode.sigesapi.users.infrastructure.service.impl;

import dev.spiffocode.sigesapi.auth.application.service.CustomUserDetails;
import dev.spiffocode.sigesapi.common.infrastructure.persistence.WithDeletedRecords;
import dev.spiffocode.sigesapi.users.domain.repository.UserRepository;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @WithDeletedRecords
    @Override
    public @NonNull CustomUserDetails loadUserByUsername(@NonNull String identifier) throws UsernameNotFoundException {
        return userRepository.findByIdentifier(identifier).orElseThrow(
                () -> new UsernameNotFoundException("User not found with identifier: " + identifier)
        );
    }
}
