package com.dxc.gdr.security;

import com.dxc.gdr.common.exception.BadRequestException;
import com.dxc.gdr.dao.RoleRepository;
import com.dxc.gdr.model.ERole;
import com.dxc.gdr.model.Role;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class RoleResolver {

    private final RoleRepository roleRepository;

    public RoleResolver(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public Set<Role> resolveRoles(Set<String> roleNames) {
        Set<Role> roles = new HashSet<>();

        for (String r : roleNames) {
            ERole eRole = toERole(r);

            Role roleEntity = roleRepository.findByName(eRole)
                    .orElseThrow(() -> new BadRequestException("Rôle introuvable : " + r));

            roles.add(roleEntity);
        }

        return roles;
    }

    private ERole toERole(String roleName) {
        String normalized = roleName.trim().toUpperCase();

        if (normalized.startsWith("ROLE_")) {
            try {
                return ERole.valueOf(normalized);
            } catch (IllegalArgumentException ex) {
                throw new BadRequestException("Rôle invalide : " + roleName);
            }
        }

        try {
            return ERole.valueOf("ROLE_" + normalized);
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Rôle invalide : " + roleName);
        }
    }
}