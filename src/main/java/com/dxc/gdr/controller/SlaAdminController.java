package com.dxc.gdr.controller;

import com.dxc.gdr.Dto.request.SlaConfigurationRequest;
import com.dxc.gdr.Dto.response.SlaConfigurationResponse;
import com.dxc.gdr.service.SlaAdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/sla")
public class SlaAdminController {

    private final SlaAdminService slaAdminService;

    public SlaAdminController(SlaAdminService slaAdminService) {
        this.slaAdminService = slaAdminService;
    }

    @GetMapping
    public ResponseEntity<List<SlaConfigurationResponse>> getAll() {
        return ResponseEntity.ok(slaAdminService.getAll());
    }

    @PostMapping
    public ResponseEntity<SlaConfigurationResponse> saveOrUpdate(@RequestBody SlaConfigurationRequest request) {
        return ResponseEntity.ok(slaAdminService.saveOrUpdate(request));
    }
}