package com.dxc.gdr.dao;

import com.dxc.gdr.model.Reclamation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReclamationRepository extends JpaRepository<Reclamation, Long> {

    Optional<Reclamation> findByNumeroReclamation(String numeroReclamation);

    List<Reclamation> findByClientIdOrderByDateCreationDesc(Long clientId);

    boolean existsByNumeroReclamation(String numeroReclamation);

    List<Reclamation> findByStatutInOrderByDateCreationDesc(java.util.Collection<com.dxc.gdr.model.ReclamationStatus> statuts);

    @org.springframework.data.jpa.repository.Query(value = "SELECT * FROM reclamations WHERE equipe_assignee_id = :teamId ORDER BY date_creation DESC", nativeQuery = true)
    List<Reclamation> findAllByTeamId(@org.springframework.data.repository.query.Param("teamId") Long teamId);
}