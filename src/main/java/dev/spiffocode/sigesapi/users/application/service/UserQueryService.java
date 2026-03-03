package dev.spiffocode.sigesapi.users.application.service;

import dev.spiffocode.sigesapi.users.presentation.dto.UserResponse;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserQueryService {
    Page<@NonNull UserResponse> findAllUsers(Pageable pageable, UserFilter filter);
    UserResponse findUserByIdentifier(String identifier);
    UserResponse findUserById(Long id);
}
