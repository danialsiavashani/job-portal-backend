package com.secure.jobs.repositories;

import com.secure.jobs.models.user.auth.AppRole;
import com.secure.jobs.models.user.auth.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByRoleName(AppRole roleName);
}
