package com.dxc.gdr.dao;

import com.dxc.gdr.model.ERole;
import com.dxc.gdr.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Integer> {
    Optional<Role> findByName(ERole name);
}
