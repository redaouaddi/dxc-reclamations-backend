package com.dxc.gdr.service;

import com.dxc.gdr.Dto.UserDto;
import com.dxc.gdr.Dto.request.CreateUserRequest;
import com.dxc.gdr.common.exception.BadRequestException;
import com.dxc.gdr.common.exception.NotFoundException;
import com.dxc.gdr.dao.UserRepository;
import com.dxc.gdr.mapper.UserMapper;
import com.dxc.gdr.model.User;
import com.dxc.gdr.security.AccessResolver;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccessResolver accessResolver;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AccessResolver accessResolver,
                       UserMapper userMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.accessResolver = accessResolver;
        this.userMapper = userMapper;
    }

    public UserDto createUser(CreateUserRequest request) {
        String email = request.getEmail().toLowerCase().trim();

        if (!(email.endsWith("@dxc.com") || email.endsWith("@gmail.com") || email.endsWith("@test.com"))) {
            throw new BadRequestException("Email invalide");
        }

        // Check for existing user (including deleted ones)
        User existingUser = userRepository.findByEmail(email).orElse(null);

        if (existingUser != null) {
            if (existingUser.isDeleted()) {
                // Reactivate and update
                existingUser.setDeleted(false);
                existingUser.setFirstName(request.getFirstName());
                existingUser.setLastName(request.getLastName());
                existingUser.setGender(com.dxc.gdr.model.Gender.valueOf(request.getGender().toUpperCase()));
                existingUser.setPassword(passwordEncoder.encode(request.getPassword()));
                existingUser.setRoles(accessResolver.resolveAccesses(request.getRoles()));
                
                User saved = userRepository.save(existingUser);
                System.out.println("♻️ Utilisateur réactivé : " + email);
                return userMapper.toDto(saved);
            } else {
                throw new BadRequestException("Email déjà utilisé.");
            }
        }

        User user = new User(
                request.getFirstName(),
                request.getLastName(),
                email,
                passwordEncoder.encode(request.getPassword()),
                com.dxc.gdr.model.Gender.valueOf(request.getGender().toUpperCase())
        );

        user.setRoles(accessResolver.resolveAccesses(request.getRoles()));

        User saved = userRepository.save(user);
        return userMapper.toDto(saved);
    }

    public UserDto updateRoles(Long id, UserDto dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Utilisateur introuvable."));

        user.setRoles(accessResolver.resolveAccesses(dto.getRoles()));
        User saved = userRepository.save(user);

        return userMapper.toDto(saved);
    }

    public List<UserDto> getAll() {
        return userRepository.findAll().stream()
                .map(userMapper::toDto)
                .toList();
    }

    public void softDelete(Long id) {
        User user = userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Utilisateur introuvable."));

        user.setDeleted(true);
        userRepository.save(user);
    }
}
