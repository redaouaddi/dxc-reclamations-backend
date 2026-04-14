package com.dxc.gdr.dao;

import com.dxc.gdr.model.Reclamation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ReclamationRepository extends JpaRepository<Reclamation, Long> {

    Optional<Reclamation> findByNumeroReclamation(String numeroReclamation);

    List<Reclamation> findByClientIdOrderByDateCreationDesc(Long clientId);

    boolean existsByNumeroReclamation(String numeroReclamation);

    List<Reclamation> findByStatutInOrderByDateCreationDesc(java.util.Collection<com.dxc.gdr.model.ReclamationStatus> statuts);

    @org.springframework.data.jpa.repository.Query(value = "SELECT * FROM reclamations WHERE equipe_assignee_id = :teamId ORDER BY date_creation DESC", nativeQuery = true)
    List<Reclamation> findAllByTeamId(@org.springframework.data.repository.query.Param("teamId") Long teamId);

    @Query("SELECT r.statut, COUNT(r) FROM Reclamation r GROUP BY r.statut")
    List<Object[]> countByStatus();

    @Query("SELECT r.priorite, COUNT(r) FROM Reclamation r GROUP BY r.priorite")
    List<Object[]> countByPriorite();

    @Query(value = """
    SELECT EXTRACT(MONTH FROM r.date_creation) AS month, COUNT(*) AS total
    FROM reclamations r
    GROUP BY EXTRACT(MONTH FROM r.date_creation)
    ORDER BY EXTRACT(MONTH FROM r.date_creation)
    """, nativeQuery = true)
    List<Object[]> countByMonth();

    @Query("SELECT r.categorie, COUNT(r) FROM Reclamation r GROUP BY r.categorie")
    List<Object[]> countByCategorie();
}