package com.dxc.gdr.service;

import com.dxc.gdr.Dto.AccessDto;
import com.dxc.gdr.common.exception.BadRequestException;
import com.dxc.gdr.common.exception.NotFoundException;
import com.dxc.gdr.dao.AccessRepository;
import com.dxc.gdr.mapper.AccessMapper;
import com.dxc.gdr.model.Access;
import com.dxc.gdr.model.EPermission;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AccessService {

    private final AccessRepository accessRepository;
    private final AccessMapper accessMapper;

    public AccessService(AccessRepository accessRepository,
                         AccessMapper accessMapper) {
        this.accessRepository = accessRepository;
        this.accessMapper = accessMapper;
    }

    public List<AccessDto> getAll() {
        return accessRepository.findAll().stream()
                .map(accessMapper::toDto)
                .toList();
    }

    public AccessDto create(AccessDto dto) {
        String name = dto.getName();
        if (name == null || name.trim().isEmpty()) {
            throw new BadRequestException("Le nom de l'access est obligatoire.");
        }
        Set<String> permissionNames = dto.getPermissions();
        if (permissionNames == null || permissionNames.isEmpty()) {
            throw new BadRequestException("Un access doit avoir au moins une permission.");
        }

        String normalizedName = normalizeAccessName(name);
        return accessRepository.findByName(normalizedName)
                .map(access -> {
                    if (access.isDeleted()) {
                        access.setDeleted(false);
                        access.setPermissions(resolvePermissions(permissionNames));
                        System.out.println("♻️ Access réactivé : " + normalizedName);
                        return accessMapper.toDto(accessRepository.save(access));
                    }
                    throw new BadRequestException("Cet access existe déjà.");
                })
                .orElseGet(() -> {
                    Access access = new Access(normalizedName);
                    access.setPermissions(resolvePermissions(permissionNames));
                    Access saved = accessRepository.save(access);
                    return accessMapper.toDto(saved);
                });
    }

    public AccessDto update(Integer id, AccessDto dto) {
        Access access = accessRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Access introuvable."));

        if (dto.getName() != null && !dto.getName().trim().isEmpty()) {
            access.setName(normalizeAccessName(dto.getName()));
        }

        if (dto.getPermissions() != null) {
            Set<EPermission> permissions = resolvePermissions(dto.getPermissions());
            if (permissions.isEmpty()) {
                throw new BadRequestException("Un access doit avoir au moins une permission.");
            }
            access.setPermissions(permissions);
        }

        Access saved = accessRepository.save(access);
        return accessMapper.toDto(saved);
    }

    public void softDelete(Integer id) {
        Access access = accessRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Access introuvable."));

        access.setDeleted(true);
        accessRepository.save(access);
    }

    private Set<EPermission> resolvePermissions(Set<String> permissionNames) {
        return permissionNames.stream()
                .map(this::normalizePermissionName)
                .map(name -> {
                    try {
                        return EPermission.valueOf(name);
                    } catch (IllegalArgumentException e) {
                        throw new BadRequestException("Permission introuvable : " + name);
                    }
                })
                .collect(Collectors.toSet());
    }

    private String normalizeAccessName(String accessName) {
        String normalized = accessName.trim().toUpperCase();
        if (!normalized.startsWith("ROLE_")) {
            normalized = "ROLE_" + normalized;
        }
        return normalized;
    }

    private String normalizePermissionName(String permissionName) {
        return permissionName.trim().toUpperCase();
    }
}
