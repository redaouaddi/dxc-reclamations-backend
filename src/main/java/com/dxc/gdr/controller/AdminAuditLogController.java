package com.dxc.gdr.controller;

import com.dxc.gdr.Dto.response.AuditLogResponse;
import com.dxc.gdr.service.AuditLogService;
import com.dxc.gdr.service.AuditExportService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.util.List;

@RestController
@RequestMapping("/api/admin/audit-logs")
@PreAuthorize("hasAuthority('ADMIN') or hasAuthority('CONSULTER_RAPPORTS') or hasRole('ADMIN')")
public class AdminAuditLogController {

    private final AuditLogService auditLogService;
    private final AuditExportService auditExportService;

    public AdminAuditLogController(AuditLogService auditLogService, AuditExportService auditExportService) {
        this.auditLogService = auditLogService;
        this.auditExportService = auditExportService;
    }

    @GetMapping
    public Page<AuditLogResponse> list(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String user,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "false") boolean excludeConsultations,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return auditLogService.findAll(role, user, action, startDate, endDate, search, excludeConsultations, pageable);
    }

    @GetMapping("/export/excel")
    public ResponseEntity<InputStreamResource> exportExcel(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String user,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "false") boolean excludeConsultations
    ) {
        List<AuditLogResponse> logs = auditLogService.findAllList(role, user, action, startDate, endDate, search, excludeConsultations);
        ByteArrayInputStream in = auditExportService.exportToExcel(logs);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=audit-log.xlsx");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .body(new InputStreamResource(in));
    }

    @GetMapping("/export/pdf")
    public ResponseEntity<InputStreamResource> exportPdf(
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String user,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "false") boolean excludeConsultations
    ) {
        List<AuditLogResponse> logs = auditLogService.findAllList(role, user, action, startDate, endDate, search, excludeConsultations);
        ByteArrayInputStream in = auditExportService.exportToPdf(logs);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=audit-log.pdf");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(in));
    }
}
