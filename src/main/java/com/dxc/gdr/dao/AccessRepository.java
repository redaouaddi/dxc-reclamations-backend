package com.dxc.gdr.dao;

import com.dxc.gdr.model.Access;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccessRepository extends JpaRepository<Access, Integer> {
    Optional<Access> findByName(String name);
    Optional<Access> findByNameAndDeletedFalse(String name);
}
