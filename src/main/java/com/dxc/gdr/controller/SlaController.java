package com.dxc.gdr.controller;

import com.dxc.gdr.Dto.response.SlaConfigurationResponse;
import com.dxc.gdr.service.SlaAdminService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sla")
public class SlaController {

    private final SlaAdminService slaAdminService;

    public SlaController(SlaAdminService slaAdminService) {
        this.slaAdminService = slaAdminService;
    }

    @GetMapping
    public ResponseEntity<List<SlaConfigurationResponse>> getAll() {
        return ResponseEntity.ok(slaAdminService.getAll());
    }
}