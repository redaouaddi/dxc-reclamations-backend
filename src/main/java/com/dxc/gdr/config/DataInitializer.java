package com.dxc.gdr.config;

import com.dxc.gdr.model.ERole;
import com.dxc.gdr.model.Role;
import com.dxc.gdr.model.User;
import com.dxc.gdr.dao.RoleRepository;
import com.dxc.gdr.dao.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private PasswordEncoder encoder;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {

        // 1. INITIALISATION DES RÔLES
        // On parcourt l'Enum ERole et on crée chaque rôle en base s'il n'existe pas
        for (ERole name : ERole.values()) {
            if (roleRepository.findByName(name).isEmpty()) {
                roleRepository.save(new Role(name));
                System.out.println("Rôle créé : " + name);
            }
        }

        // 2. CRÉATION DE L'ADMIN PAR DÉFAUT
        // On vérifie si un utilisateur "admin" existe déjà
        if (userRepository.findByUsername("admin").isEmpty()) {

            // Note : Pour l'instant le mot de passe est en clair "admin123"
            // Dès qu'on ajoutera Spring Security à l'étape suivante, on le hachera avec BCrypt
            User admin = new User("admin", "admin@dxc.com", encoder.encode("admin123"));

            // On récupère l'objet Role correspondant à ROLE_ADMIN
            Role adminRole = roleRepository.findByName(ERole.ROLE_ADMIN)
                    .orElseThrow(() -> new RuntimeException("Erreur: Le rôle ADMIN n'est pas trouvé."));

            Set<Role> roles = new HashSet<>();
            roles.add(adminRole);
            admin.setRoles(roles);

            userRepository.save(admin);
            System.out.println("Utilisateur Admin par défaut créé (admin / admin123)");
        }
    }
}