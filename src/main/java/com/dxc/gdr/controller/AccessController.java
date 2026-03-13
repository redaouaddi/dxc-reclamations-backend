package com.dxc.gdr.controller;

import com.dxc.gdr.Dto.AccessDto;
import com.dxc.gdr.service.AccessService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/accesses")
@PreAuthorize("hasRole('ADMIN')")
public class AccessController {

    private final AccessService accessService;

    public AccessController(AccessService accessService) {
        this.accessService = accessService;
    }

    @GetMapping
    public List<AccessDto> getAll() {
        return accessService.getAll();
    }

    @PostMapping
    public AccessDto create(@RequestBody AccessDto dto) {
        return accessService.create(dto);
    }

    @PutMapping("/{id}")
    public AccessDto update(@PathVariable Integer id, @RequestBody AccessDto dto) {
        return accessService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id) {
        accessService.softDelete(id);
    }
}
