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
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public UserDto createUser(@Valid @RequestBody CreateUserRequest request) {
        return userService.createUser(request);
    }

    @PutMapping("/{id}/roles")
    public UserDto updateRoles(@PathVariable Long id, @Valid @RequestBody UserDto dto) {
        return userService.updateRoles(id, dto);
    }

    @GetMapping
    public List<UserDto> getAll() {
        return userService.getAll();
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        userService.softDelete(id);
    }
}