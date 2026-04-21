package com.dxc.gdr.security;

import com.dxc.gdr.common.exception.NotFoundException;
import com.dxc.gdr.dao.AccessRepository;
import com.dxc.gdr.model.Access;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class AccessResolver {

    private final AccessRepository accessRepository;

    public AccessResolver(AccessRepository accessRepository) {
        this.accessRepository = accessRepository;
    }

    public Set<Access> resolveAccesses(Collection<String> roleNames) {
        if (roleNames == null || roleNames.isEmpty()) {
            throw new NotFoundException("Aucun rôle fourni.");
        }

        return roleNames.stream()
                .map(role -> role == null ? null : role.trim().toUpperCase())
                .map(roleName -> accessRepository.findByNameAndDeletedFalse(roleName)
                        .orElseThrow(() -> new NotFoundException("Access introuvable : " + roleName)))
                .collect(Collectors.toSet());
    }
}