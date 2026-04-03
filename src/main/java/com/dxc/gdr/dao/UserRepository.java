package com.dxc.gdr.dao;

import com.dxc.gdr.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Boolean existsByEmail(String email);

    Optional<User> findByIdAndDeletedFalse(Long id);

    java.util.List<User> findByEquipeIsNullAndDeletedFalse();
    
    @org.springframework.data.jpa.repository.Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName AND u.equipe IS NULL AND u.deleted = false")
    java.util.List<User> findByRoleAndEquipeIsNull(@org.springframework.data.repository.query.Param("roleName") String roleName);
}

