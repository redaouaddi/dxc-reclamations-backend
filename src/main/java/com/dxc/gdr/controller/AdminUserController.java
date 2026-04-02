package com.dxc.gdr.controller;

import com.dxc.gdr.Dto.UserDto;
import com.dxc.gdr.Dto.request.CreateUserRequest;
import com.dxc.gdr.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {


    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CREER_UTILISATEURS') or hasRole('ADMIN')")
    public UserDto createUser(@Valid @RequestBody CreateUserRequest request) {
        return userService.createUser(request);
    }


    @PutMapping("/{id}/roles")
    @PreAuthorize("hasAuthority('MODIFIER_UTILISATEURS') or hasRole('ADMIN')")
    public UserDto updateRoles(@PathVariable Long id, @Valid @RequestBody UserDto dto) {
        return userService.updateRoles(id, dto);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('MODIFIER_UTILISATEURS') or hasRole('ADMIN')")
    public UserDto updateUser(@PathVariable Long id, @Valid @RequestBody com.dxc.gdr.Dto.request.UpdateUserRequest request) {
        return userService.updateUser(id, request);
    }


    @GetMapping
    @PreAuthorize("hasAuthority('LIRE_UTILISATEURS') or hasRole('ADMIN')")
    public List<UserDto> getAll() {
        return userService.getAll();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SUPPRIMER_UTILISATEURS') or hasRole('ADMIN')")
    public void deleteUser(@PathVariable Long id) {
        userService.softDelete(id);
    }
}