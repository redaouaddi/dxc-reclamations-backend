package com.dxc.gdr.dao;

import com.dxc.gdr.model.Reclamation;
import com.dxc.gdr.model.ReclamationStatus;
import com.dxc.gdr.model.SlaStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReclamationRepository extends JpaRepository<Reclamation, Long> {

    Optional<Reclamation> findByNumeroReclamation(String numeroReclamation);

    long countByStatut(ReclamationStatus statut);

    @Query("SELECT r FROM Reclamation r WHERE r.statut = :statut")
    Page<Reclamation> findByStatut(@Param("statut") ReclamationStatus statut, Pageable pageable);

    long countBySlaStatus(SlaStatus slaStatus);

    org.springframework.data.domain.Page<Reclamation> findByClientIdOrderByDateCreationDesc(Long clientId, org.springframework.data.domain.Pageable pageable);

    boolean existsByNumeroReclamation(String numeroReclamation);

    org.springframework.data.domain.Page<Reclamation> findByStatutInOrderByDateCreationDesc(java.util.Collection<com.dxc.gdr.model.ReclamationStatus> statuts, org.springframework.data.domain.Pageable pageable);

    @org.springframework.data.jpa.repository.Query(value = "SELECT * FROM reclamations WHERE equipe_assignee_id = :teamId",
            countQuery = "SELECT count(*) FROM reclamations WHERE equipe_assignee_id = :teamId",
            nativeQuery = true)
    org.springframework.data.domain.Page<Reclamation> findAllByTeamId(@org.springframework.data.repository.query.Param("teamId") Long teamId, org.springframework.data.domain.Pageable pageable);

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

    java.util.List<Reclamation> findByEquipeAssigneeId(Long teamId);

    org.springframework.data.domain.Page<Reclamation> findByEquipeAssigneeIdAndStatutIn(Long teamId, java.util.Collection<ReclamationStatus> statuts, org.springframework.data.domain.Pageable pageable);

    // ===== FILTERED QUERIES =====

    @Query("SELECT COUNT(r) FROM Reclamation r WHERE " +
           "(:year IS NULL OR YEAR(r.dateCreation) = :year) AND " +
           "(:month IS NULL OR MONTH(r.dateCreation) = :month) AND " +
           "(:priorite IS NULL OR r.priorite = :priorite) AND " +
           "(:teamId IS NULL OR r.equipeAssignee.id = :teamId)")
    long countFiltered(@Param("year") Integer year, @Param("month") Integer month, @Param("priorite") com.dxc.gdr.model.ReclamationPriority priorite, @Param("teamId") Long teamId);

    @Query("SELECT COUNT(r) FROM Reclamation r WHERE r.statut = :statut AND " +
           "(:year IS NULL OR YEAR(r.dateCreation) = :year) AND " +
           "(:month IS NULL OR MONTH(r.dateCreation) = :month) AND " +
           "(:priorite IS NULL OR r.priorite = :priorite) AND " +
           "(:teamId IS NULL OR r.equipeAssignee.id = :teamId)")
    long countByStatutFiltered(@Param("statut") ReclamationStatus statut, @Param("year") Integer year, @Param("month") Integer month, @Param("priorite") com.dxc.gdr.model.ReclamationPriority priorite, @Param("teamId") Long teamId);

    @Query("SELECT COUNT(r) FROM Reclamation r WHERE r.slaStatus = :slaStatus AND " +
           "(:year IS NULL OR YEAR(r.dateCreation) = :year) AND " +
           "(:month IS NULL OR MONTH(r.dateCreation) = :month) AND " +
           "(:priorite IS NULL OR r.priorite = :priorite) AND " +
           "(:teamId IS NULL OR r.equipeAssignee.id = :teamId)")
    long countBySlaStatusFiltered(@Param("slaStatus") SlaStatus slaStatus, @Param("year") Integer year, @Param("month") Integer month, @Param("priorite") com.dxc.gdr.model.ReclamationPriority priorite, @Param("teamId") Long teamId);

    @Query("SELECT r.statut, COUNT(r) FROM Reclamation r WHERE " +
           "(:year IS NULL OR YEAR(r.dateCreation) = :year) AND " +
           "(:month IS NULL OR MONTH(r.dateCreation) = :month) AND " +
           "(:priorite IS NULL OR r.priorite = :priorite) AND " +
           "(:teamId IS NULL OR r.equipeAssignee.id = :teamId) " +
           "GROUP BY r.statut")
    List<Object[]> countByStatusGroupFiltered(@Param("year") Integer year, @Param("month") Integer month, @Param("priorite") com.dxc.gdr.model.ReclamationPriority priorite, @Param("teamId") Long teamId);

    @Query("SELECT r.priorite, COUNT(r) FROM Reclamation r WHERE " +
           "(:year IS NULL OR YEAR(r.dateCreation) = :year) AND " +
           "(:month IS NULL OR MONTH(r.dateCreation) = :month) AND " +
           "(:priorite IS NULL OR r.priorite = :priorite) AND " +
           "(:teamId IS NULL OR r.equipeAssignee.id = :teamId) " +
           "GROUP BY r.priorite")
    List<Object[]> countByPrioriteGroupFiltered(@Param("year") Integer year, @Param("month") Integer month, @Param("priorite") com.dxc.gdr.model.ReclamationPriority priorite, @Param("teamId") Long teamId);

    @Query("SELECT r.categorie, COUNT(r) FROM Reclamation r WHERE " +
           "(:year IS NULL OR YEAR(r.dateCreation) = :year) AND " +
           "(:month IS NULL OR MONTH(r.dateCreation) = :month) AND " +
           "(:priorite IS NULL OR r.priorite = :priorite) AND " +
           "(:teamId IS NULL OR r.equipeAssignee.id = :teamId) " +
           "GROUP BY r.categorie")
    List<Object[]> countByCategorieGroupFiltered(@Param("year") Integer year, @Param("month") Integer month, @Param("priorite") com.dxc.gdr.model.ReclamationPriority priorite, @Param("teamId") Long teamId);

    @Query("SELECT MONTH(r.dateCreation), COUNT(r) FROM Reclamation r WHERE " +
           "(:year IS NULL OR YEAR(r.dateCreation) = :year) AND " +
           "(:month IS NULL OR MONTH(r.dateCreation) = :month) AND " +
           "(:priorite IS NULL OR r.priorite = :priorite) AND " +
           "(:teamId IS NULL OR r.equipeAssignee.id = :teamId) " +
           "GROUP BY MONTH(r.dateCreation) ORDER BY MONTH(r.dateCreation)")
    List<Object[]> countByMonthGroupFiltered(@Param("year") Integer year, @Param("month") Integer month, @Param("priorite") com.dxc.gdr.model.ReclamationPriority priorite, @Param("teamId") Long teamId);
}