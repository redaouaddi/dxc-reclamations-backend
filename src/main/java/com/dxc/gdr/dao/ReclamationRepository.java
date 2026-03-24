package com.dxc.gdr.dao;

import com.dxc.gdr.model.Reclamation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReclamationRepository extends JpaRepository<Reclamation, Long> {

    Optional<Reclamation> findByNumeroReclamation(String numeroReclamation);

    List<Reclamation> findByClientIdOrderByDateCreationDesc(Long clientId);

    boolean existsByNumeroReclamation(String numeroReclamation);
}