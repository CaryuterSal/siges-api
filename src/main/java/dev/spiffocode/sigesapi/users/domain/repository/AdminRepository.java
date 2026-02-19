package dev.spiffocode.sigesapi.users.domain.repository;

import dev.spiffocode.sigesapi.users.domain.model.Admin;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminRepository extends CommonUserRepository<Admin> {
}
