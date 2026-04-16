package com.dxc.gdr.config;

import com.dxc.gdr.dao.AccessRepository;
import com.dxc.gdr.dao.EquipeRepository;
import com.dxc.gdr.dao.ReclamationRepository;
import com.dxc.gdr.dao.UserRepository;
import com.dxc.gdr.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
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
    @Autowired private EquipeRepository equipeRepository;
    @Autowired private ReclamationRepository reclamationRepository;
    @Autowired private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {

        // FIX : Supprimer la contrainte de check sur l'enum si elle bloque les nouvelles permissions (PostgreSQL)
        try {
            jdbcTemplate.execute("ALTER TABLE access_permissions DROP CONSTRAINT IF EXISTS access_permissions_permission_check");
        } catch (Exception e) {
            System.out.println("ℹ️ Info : Pas de contrainte à supprimer ou erreur SQL mineure : " + e.getMessage());
        }

        // 1. MIGRATION : Supprimer le préfixe ROLE_ des noms de rôles existants si nécessaire
        jdbcTemplate.execute("UPDATE accesses SET name = SUBSTRING(name, 6) WHERE name LIKE 'ROLE_%'");
        System.out.println("🔄 Migration : Préfixes 'ROLE_' supprimés des noms de rôles existants.");

        // ─────────────────────────────────────────────────────────────
        // 2. ACCÈS (RÔLES) — libellés propres sans préfixe ROLE_
        // ─────────────────────────────────────────────────────────────

        // CLIENT
        createAccessIfAbsent("CLIENT", Set.of(
                LIRE_RECLAMATIONS,
                CREER_RECLAMATIONS
        ));

        // AGENT
        createAccessIfAbsent("AGENT", Set.of(
                LIRE_RECLAMATIONS,
                MODIFIER_RECLAMATIONS
        ));

        // SERVICE_MANAGER
        createAccessIfAbsent("SERVICE_MANAGER", Set.of(
                LIRE_RECLAMATIONS,
                ASSIGNER_RECLAMATIONS,
                VOIR_NOUVELLES_RECLAMATIONS
        ));

        // CHEF_EQUIPE
        createAccessIfAbsent("CHEF_EQUIPE", Set.of(
                LIRE_RECLAMATIONS,
                GERER_EQUIPE
        ));

        // ADMIN : accès total
        createAccessIfAbsent("ADMIN", Set.of(EPermission.values()));

        // ─────────────────────────────────────────────────────────────
        // 3. UTILISATEUR ADMIN PAR DÉFAUT
        // ─────────────────────────────────────────────────────────────
        if (userRepository.findByEmail("admin@dxc.com").isEmpty()) {
            User admin = new User(
                    "Admin",
                    "DXC",
                    "admin@dxc.com",
                    encoder.encode("admin123"),
                    Gender.MASCULIN
            );
            Access adminRole = accessRepository.findByName("ADMIN")
                    .orElseThrow(() -> new RuntimeException("ADMIN introuvable."));
            Set<Access> roles = new HashSet<>();
            roles.add(adminRole);
            admin.setRoles(roles);
            userRepository.save(admin);
            System.out.println("✅ Admin créé : admin@dxc.com / admin123");
        }

        // 4. TEST DATA FOR AGENT STORY
        Equipe team = equipeRepository.findByNom("Team Support")
                .orElseGet(() -> {
                    Equipe t = new Equipe();
                    t.setNom("Team Support");
                    return equipeRepository.save(t);
                });

        User agent = userRepository.findByEmail("agent@dxc.com")
                .orElseGet(() -> {
                    User u = new User(
                            "Agent",
                            "DXC",
                            "agent@dxc.com",
                            encoder.encode("agent123"),
                            Gender.FEMININ
                    );
                    Access agentRole = accessRepository.findByName("AGENT")
                            .orElseThrow(() -> new RuntimeException("AGENT introuvable."));
                    u.getRoles().add(agentRole);
                    return u;
                });
        
        // Ensure team is set
        agent.setEquipe(team);
        userRepository.save(agent);
        System.out.println("✅ Agent vérifié/créé : agent@dxc.com (Team: Team Support)");

        // Create a Reclamation for this team if it doesn't exist
        if (reclamationRepository.findByNumeroReclamation("REC-TEST-001").isEmpty()) {
            User admin = userRepository.findByEmail("admin@dxc.com")
                    .orElseThrow(() -> new RuntimeException("Admin de test manquant"));

            Reclamation rec = new Reclamation();
            rec.setNumeroReclamation("REC-TEST-001");
            rec.setTitre("Problème VPN");
            rec.setDescription("Le VPN ne se connecte plus depuis ce matin.");
            rec.setStatut(ReclamationStatus.EN_ATTENTE);
            rec.setPriorite(ReclamationPriority.ELEVEE);
            rec.setCategorie(ReclamationCategory.MAINTENANCE);
            rec.setEquipeAssignee(team);
            rec.setClient(admin); // Fix: assign a client
            rec.setDateCreation(java.time.LocalDateTime.now());
            rec.setDateMiseAJour(java.time.LocalDateTime.now());
            reclamationRepository.save(rec);
            System.out.println("✅ Réclamation de test REC-TEST-001 créée et assignée à Team Support.");
        }
    }

    private void createAccessIfAbsent(String name, Set<EPermission> permissions) {
        accessRepository.findByName(name).ifPresentOrElse(
                access -> {
                    if (access.isDeleted()) {
                        access.setDeleted(false);
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
