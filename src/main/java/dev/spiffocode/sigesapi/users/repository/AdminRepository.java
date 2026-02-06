package dev.spiffocode.sigesapi.users.repository;

import dev.spiffocode.sigesapi.users.model.Admin;
import org.springframework.stereotype.Repository;

@Repository
public interface AdminRepository extends CommonUserRepository<Admin> {
}
