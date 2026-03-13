package com.dxc.gdr.security;

import com.dxc.gdr.common.exception.BadRequestException;
import com.dxc.gdr.dao.AccessRepository;
import com.dxc.gdr.model.Access;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class AccessResolver {

    private final AccessRepository accessRepository;

    public AccessResolver(AccessRepository accessRepository) {
        this.accessRepository = accessRepository;
    }

    public Set<Access> resolveAccesses(Set<String> accessNames) {
        Set<Access> accesses = new HashSet<>();

        for (String r : accessNames) {
            String normalized = normalizeAccessName(r);

            Access accessEntity = accessRepository.findByNameAndDeletedFalse(normalized)
                    .orElseThrow(() -> new BadRequestException("Access introuvable : " + r));

            accesses.add(accessEntity);
        }

        return accesses;
    }

    private String normalizeAccessName(String accessName) {
        String normalized = accessName.trim().toUpperCase();
        if (!normalized.startsWith("ROLE_")) {
            normalized = "ROLE_" + normalized;
        }
        return normalized;
    }
}
