package com.dxc.gdr.controller;

import com.dxc.gdr.Dto.request.CreateUserRequest;
import com.dxc.gdr.Dto.request.UpdateUserRolesRequest;
import com.dxc.gdr.Dto.response.UserResponse;
import com.dxc.gdr.common.exception.BadRequestException;
import com.dxc.gdr.common.exception.NotFoundException;
import com.dxc.gdr.dao.UserRepository;
import com.dxc.gdr.model.User;
import com.dxc.gdr.security.RoleResolver;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleResolver roleResolver;

    public AdminUserController(UserRepository userRepository,
                               PasswordEncoder passwordEncoder,
                               RoleResolver roleResolver) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.roleResolver = roleResolver;
    }

    @PostMapping
    public UserResponse createUser(@Valid @RequestBody CreateUserRequest request) {
        String email = request.getEmail().toLowerCase();

        if (Boolean.TRUE.equals(userRepository.existsByEmail(email))) {
            throw new BadRequestException("Email déjà utilisé.");
        }
        if (Boolean.TRUE.equals(userRepository.existsByUsername(request.getUsername()))) {
            throw new BadRequestException("Username déjà utilisé.");
        }

        User user = new User(
                request.getUsername(),
                email,
                passwordEncoder.encode(request.getPassword())
        );

        user.setRoles(roleResolver.resolveRoles(request.getRoles()));

        User saved = userRepository.save(user);
        return toResponse(saved);
    }

    @PutMapping("/{id}/roles")
    public UserResponse updateRoles(@PathVariable Long id, @Valid @RequestBody UpdateUserRolesRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Utilisateur introuvable."));

        user.setRoles(roleResolver.resolveRoles(request.getRoles()));
        User saved = userRepository.save(user);

        return toResponse(saved);
    }

    @GetMapping
    public List<UserResponse> getAll() {
        return userRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    private UserResponse toResponse(User u) {
        Set<String> roles = u.getRoles().stream()
                .map(role -> role.getName().name()) // ex: ROLE_ADMIN
                .collect(Collectors.toSet());

        return new UserResponse(u.getId(), u.getUsername(), u.getEmail(), roles);
    }
}