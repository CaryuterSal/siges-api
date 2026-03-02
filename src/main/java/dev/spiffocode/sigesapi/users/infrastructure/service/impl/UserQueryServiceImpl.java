package dev.spiffocode.sigesapi.users.infrastructure.service.impl;

import dev.spiffocode.sigesapi.users.application.service.UserFilter;
import dev.spiffocode.sigesapi.users.application.service.UserQueryService;
import dev.spiffocode.sigesapi.users.domain.repository.UserRepository;
import dev.spiffocode.sigesapi.users.presentation.dto.UserResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserQueryServiceImpl implements UserQueryService {

    private final UserRepository userRepository;

    @Override
    public Page<@NonNull UserResponse> findAllUsers(Pageable pageable, UserFilter filter) {
        return null;
    }

    @Override
    public UserResponse findUserByIdentifier(String identifier) {
        return null;
    }

    @Override
    public UserResponse findUserById(Long id) {
        return null;
    }
}
