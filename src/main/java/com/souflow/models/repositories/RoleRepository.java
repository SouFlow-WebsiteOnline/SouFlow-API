package com.souflow.models.repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.souflow.models.entities.Role;
import com.souflow.models.enums.RoleCode;

@Repository
public interface RoleRepository extends JpaRepository<Role, RoleCode> {
	Optional<Role> findByCode(RoleCode roleCode);
}
