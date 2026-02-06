package dev.spiffocode.sigesapi.users.repository;

import dev.spiffocode.sigesapi.users.model.User;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends CommonUserRepository<User> {
}
