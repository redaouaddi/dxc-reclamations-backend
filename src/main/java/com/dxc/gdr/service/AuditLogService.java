package com.dxc.gdr.service;

import com.dxc.gdr.Dto.response.AuditLogResponse;
import com.dxc.gdr.dao.AuditLogRepository;
import com.dxc.gdr.model.AuditLog;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional(readOnly = true)
    public Page<AuditLogResponse> findAll(
            String role,
            String user,
            String action,
            String startDate,
            String endDate,
            String search,
            boolean excludeConsultations,
            Pageable pageable
    ) {
        Pageable sorted = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "timestamp")
        );

        Specification<AuditLog> spec = buildSpecification(role, user, action, startDate, endDate, search, excludeConsultations);
        return auditLogRepository.findAll(spec, sorted).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<AuditLogResponse> findAllList(
            String role,
            String user,
            String action,
            String startDate,
            String endDate,
            String search,
            boolean excludeConsultations
    ) {
        Specification<AuditLog> spec = buildSpecification(role, user, action, startDate, endDate, search, excludeConsultations);
        Sort sort = Sort.by(Sort.Direction.DESC, "timestamp");
        return auditLogRepository.findAll(spec, sort).stream()
                .map(this::toResponse)
                .collect(java.util.stream.Collectors.toList());
    }

    @Transactional
    public AuditLog record(
            String actorEmail,
            String actorName,
            String role,
            String action,
            String entityType,
            String entityId,
            String details,
            String ipAddress
    ) {
        AuditLog log = new AuditLog();
        log.setActorEmail(actorEmail != null ? actorEmail : "system@gdr.local");
        log.setActorName(actorName != null && !actorName.isBlank() ? actorName : log.getActorEmail());
        log.setRole(role != null ? role : "SYSTEM");
        log.setAction(action != null ? action : "ACTION");
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setDetails(details);
        log.setIpAddress(ipAddress);
        log.setTimestamp(LocalDateTime.now());
        return auditLogRepository.save(log);
    }

    private Specification<AuditLog> buildSpecification(
            String role,
            String user,
            String action,
            String startDate,
            String endDate,
            String search,
            boolean excludeConsultations
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (role != null && !role.isBlank()) {
                predicates.add(cb.equal(cb.upper(root.get("role")), role.trim().toUpperCase()));
            }

            if (user != null && !user.isBlank()) {
                String pattern = "%" + user.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("actorEmail")), pattern),
                        cb.like(cb.lower(root.get("actorName")), pattern)
                ));
            }

            if (action != null && !action.isBlank()) {
                String pattern = "%" + action.trim().toLowerCase() + "%";
                predicates.add(cb.like(cb.lower(root.get("action")), pattern));
            }

            if (startDate != null && !startDate.isBlank()) {
                LocalDateTime start = parseDateTime(startDate, false);
                if (start != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("timestamp"), start));
                }
            }

            if (endDate != null && !endDate.isBlank()) {
                LocalDateTime end = parseDateTime(endDate, true);
                if (end != null) {
                    predicates.add(cb.lessThanOrEqualTo(root.get("timestamp"), end));
                }
            }

            if (search != null && !search.isBlank()) {
                String pattern = "%" + search.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("actorEmail")), pattern),
                        cb.like(cb.lower(root.get("actorName")), pattern),
                        cb.like(cb.lower(root.get("action")), pattern),
                        cb.like(cb.lower(cb.coalesce(root.get("details"), "")), pattern),
                        cb.like(cb.lower(cb.coalesce(root.get("entityType"), "")), pattern),
                        cb.like(cb.lower(cb.coalesce(root.get("entityId"), "")), pattern)
                ));
            }

            if (excludeConsultations) {
                jakarta.persistence.criteria.Expression<String> actionExpr =
                        cb.upper(cb.coalesce(root.get("action"), ""));
                predicates.add(cb.and(
                        cb.notLike(actionExpr, "CONSULTATION_%"),
                        cb.notLike(actionExpr, "ECHEC_CONSULTATION_%")
                ));
            }

            if (predicates.isEmpty()) {
                return cb.conjunction();
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private LocalDateTime parseDateTime(String dateStr, boolean isEnd) {
        if (dateStr == null || dateStr.isBlank()) {
            return null;
        }
        try {
            return java.time.OffsetDateTime.parse(dateStr).toLocalDateTime();
        } catch (Exception e) {
            try {
                return java.time.ZonedDateTime.parse(dateStr).toLocalDateTime();
            } catch (Exception e1) {
                try {
                    return LocalDateTime.parse(dateStr);
                } catch (Exception e2) {
                    try {
                        java.time.LocalDate date = java.time.LocalDate.parse(dateStr);
                        return isEnd ? date.atTime(23, 59, 59, 999999999) : date.atStartOfDay();
                    } catch (Exception e3) {
                        return null;
                    }
                }
            }
        }
    }

    private AuditLogResponse toResponse(AuditLog log) {
        AuditLogResponse dto = new AuditLogResponse();
        dto.setId(log.getId());
        dto.setActorEmail(log.getActorEmail());
        dto.setActorName(log.getActorName());
        dto.setRole(log.getRole());
        dto.setAction(log.getAction());
        dto.setEntityType(log.getEntityType());
        dto.setEntityId(log.getEntityId());
        dto.setDetails(log.getDetails());
        dto.setIpAddress(log.getIpAddress());
        dto.setTimestamp(log.getTimestamp());
        return dto;
    }
}
