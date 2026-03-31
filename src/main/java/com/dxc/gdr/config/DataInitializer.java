package com.dxc.gdr.config;

import com.dxc.gdr.dao.AccessRepository;
import com.dxc.gdr.dao.UserRepository;
import com.dxc.gdr.model.Access;
import com.dxc.gdr.model.EPermission;
import com.dxc.gdr.model.Gender;
import com.dxc.gdr.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

import static com.dxc.gdr.model.EPermission.*;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired private PasswordEncoder encoder;
    @Autowired private AccessRepository accessRepository;
    @Autowired private UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {

        // ─────────────────────────────────────────────────────────────
        // 1. ACCÈS (RÔLES) — noms stockés avec ROLE_ (convention Spring)
        // ─────────────────────────────────────────────────────────────

        // CLIENT : peut lire et créer ses propres réclamations
        createAccessIfAbsent("ROLE_CLIENT", Set.of(
                LIRE_RECLAMATIONS,
                CREER_RECLAMATIONS
        ));

// AGENT : traite les réclamations assignées
        createAccessIfAbsent("ROLE_AGENT", Set.of(
                LIRE_RECLAMATIONS,
                CREER_RECLAMATIONS,
                MODIFIER_RECLAMATIONS,
                ASSIGNER_RECLAMATIONS
        ));

// SERVICE_MANAGER : supervise les agents et réclamations
        createAccessIfAbsent("ROLE_SERVICE_MANAGER", Set.of(
                LIRE_RECLAMATIONS,
                CREER_RECLAMATIONS,
                MODIFIER_RECLAMATIONS,
                SUPPRIMER_RECLAMATIONS,
                ASSIGNER_RECLAMATIONS,
                CONSULTER_RAPPORTS
        ));

// MANAGER : vue globale + gestion utilisateurs + rapports
        createAccessIfAbsent("ROLE_MANAGER", Set.of(
                LIRE_RECLAMATIONS,
                LIRE_UTILISATEURS,
                MODIFIER_UTILISATEURS,
                CONSULTER_RAPPORTS
        ));

        // ADMIN : accès total
        createAccessIfAbsent("ROLE_ADMIN", Set.of(EPermission.values()));

        // ─────────────────────────────────────────────────────────────
        // 2. UTILISATEUR ADMIN PAR DÉFAUT
        // ─────────────────────────────────────────────────────────────
        if (userRepository.findByEmail("admin@dxc.com").isEmpty()) {
            User admin = new User(
                    "Admin",
                    "DXC",
                    "admin@dxc.com",
                    encoder.encode("admin123"),
                    Gender.MASCULIN
            );
            Access adminRole = accessRepository.findByName("ROLE_ADMIN")
                    .orElseThrow(() -> new RuntimeException("ROLE_ADMIN introuvable."));
            Set<Access> roles = new HashSet<>();
            roles.add(adminRole);
            admin.setRoles(roles);
            userRepository.save(admin);
            System.out.println("✅ Admin créé : admin@dxc.com / admin123");
        }
    }

    private void createAccessIfAbsent(String name, Set<EPermission> permissions) {
        accessRepository.findByName(name).ifPresentOrElse(
                access -> {
                    if (access.isDeleted()) {
                        access.setDeleted(false);
                        System.out.println("♻️ Access réactivé : " + name);
                    }
                    access.setPermissions(new HashSet<>(permissions));
                    accessRepository.save(access);
                },
                () -> {
                    Access access = new Access(name);
                    access.setPermissions(new HashSet<>(permissions));
                    accessRepository.save(access);
                    System.out.println("✅ Access créé : " + name);
                }
        );
    }
}