package com.dxc.gdr.dao;

import com.dxc.gdr.model.Equipe;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EquipeRepository extends JpaRepository<Equipe, Long> {
    Optional<Equipe> findByChefEquipeId(Long chefId);
    Optional<Equipe> findByNom(String nom);
}
