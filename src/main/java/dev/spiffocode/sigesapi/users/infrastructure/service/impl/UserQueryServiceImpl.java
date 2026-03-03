package dev.spiffocode.sigesapi.users.infrastructure.service.impl;

import dev.spiffocode.sigesapi.common.infrastructure.persistence.WithDeletedRecords;
import dev.spiffocode.sigesapi.users.application.mapper.UserMapper;
import dev.spiffocode.sigesapi.users.application.service.UserFilter;
import dev.spiffocode.sigesapi.users.application.service.UserQueryService;
import dev.spiffocode.sigesapi.users.domain.exception.UserNotFoundException;
import dev.spiffocode.sigesapi.users.domain.model.User;
import dev.spiffocode.sigesapi.users.domain.repository.UserRepository;
import dev.spiffocode.sigesapi.users.presentation.dto.UserResponse;
import jakarta.persistence.EntityManager;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.hibernate.Session;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.stereotype.Service;

import static dev.spiffocode.sigesapi.users.domain.specification.UserSpecifications.*;

@Service
@RequiredArgsConstructor
public class UserQueryServiceImpl implements UserQueryService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    private final EntityManager em;

    @WithDeletedRecords
    @Override
    public Page<@NonNull UserResponse> findAllUsers(Pageable pageable, UserFilter filter) {
        Session session = em.unwrap(Session.class);
        return userRepository.findAll(resolveSpecification(filter), pageable)
                .map(userMapper::toResponse);
    }

    private Specification<@NonNull User> resolveSpecification(UserFilter filter) {
        return Specification
                .where(searchQuery(filter.searchQuery()))
                .and(userTypeIn(filter.userTypes()))
                .and(byShowMode(filter.showMode()));
    }

    @PostAuthorize("hasRole('ADMIN') or @securityContextHelper.isCurrentUser(returnObject.id)")
    @WithDeletedRecords
    @Override
    public UserResponse findUserByIdentifier(String identifier) {
        return userRepository.findByIdentifier(identifier)
                .map(userMapper::toResponse)
                .orElseThrow(() -> new UserNotFoundException(identifier));
    }

    @PostAuthorize("hasRole('ADMIN') or @securityContextHelper.isCurrentUser(returnObject.id)")
    @WithDeletedRecords
    @Override
    public UserResponse findUserById(Long id) {
        return userRepository.findById(id)
                .map(userMapper::toResponse)
                .orElseThrow(() -> new UserNotFoundException(id));
    }
}
