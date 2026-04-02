package com.dxc.gdr.controller;

import com.dxc.gdr.Dto.request.FaceLoginRequest;
import com.dxc.gdr.Dto.request.FaceRegisterRequest;
import com.dxc.gdr.dao.UserRepository;
import com.dxc.gdr.model.User;
import com.dxc.gdr.security.jwt.JwtUtils;
import com.dxc.gdr.security.services.UserDetailsImpl;
import com.dxc.gdr.security.services.UserDetailsServiceImpl;
import com.dxc.gdr.service.FaceService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/face")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class FaceController {

    private final FaceService faceService;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository;
    private final UserDetailsServiceImpl userDetailsServiceImpl;

    public FaceController(FaceService faceService,
                          JwtUtils jwtUtils,
                          UserRepository userRepository,
                          UserDetailsServiceImpl userDetailsServiceImpl) {
        this.faceService = faceService;
        this.jwtUtils = jwtUtils;
        this.userRepository = userRepository;
        this.userDetailsServiceImpl = userDetailsServiceImpl;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerFace(@RequestBody FaceRegisterRequest request) {
        System.out.println("===== FACE REGISTER =====");
        System.out.println("EMAIL REGISTER : " + request.getEmail());
        System.out.println("IMAGE REGISTER NULL ? " + (request.getImage() == null));
        System.out.println("IMAGE REGISTER LENGTH : " + (request.getImage() != null ? request.getImage().length() : 0));

        faceService.saveFace(request.getEmail(), request.getImage());

        return ResponseEntity.ok(Map.of(
                "message", "Visage enregistré avec succès"
        ));
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginWithFace(@RequestBody FaceLoginRequest request) {
        System.out.println("===== FACE LOGIN =====");
        System.out.println("EMAIL LOGIN : " + request.getEmail());
        System.out.println("IMAGE LOGIN NULL ? " + (request.getImage() == null));
        System.out.println("IMAGE LOGIN LENGTH : " + (request.getImage() != null ? request.getImage().length() : 0));

        boolean match = faceService.verifyFace(request.getEmail(), request.getImage());

        System.out.println("MATCH RESULT : " + match);

        if (!match) {
            return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "Visage non reconnu"
            ));
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        System.out.println("USER FOUND : " + user.getEmail());

        UserDetailsImpl userDetails = UserDetailsImpl.build(user);

        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );

        String jwt = jwtUtils.generateJwtToken(authentication);

        System.out.println("JWT GENERATED SUCCESSFULLY");

        return ResponseEntity.ok(Map.of(
                "token", jwt,
                "id", user.getId(),
                "email", user.getEmail(),
                "roles", userDetails.getAuthorities().stream()
                        .map(item -> item.getAuthority())
                        .toList(),
                "type", "Bearer"
        ));
    }
}