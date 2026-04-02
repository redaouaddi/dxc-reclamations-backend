package com.dxc.gdr.controller;

import com.dxc.gdr.Dto.request.PasskeyRegisterFinishRequest;
import com.dxc.gdr.service.PasskeyService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/passkey")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class PasskeyController {

    private final PasskeyService passkeyService;

    public PasskeyController(PasskeyService passkeyService) {
        this.passkeyService = passkeyService;
    }

    @PostMapping("/register/options")
    public ResponseEntity<?> registerOptions(@RequestParam String email, HttpSession session) {
        String challenge = passkeyService.generateChallenge();
        session.setAttribute("passkey_register_challenge", challenge);
        session.setAttribute("passkey_register_email", email);

        return ResponseEntity.ok(Map.of(
                "challenge", challenge,
                "rpId", "localhost",
                "rpName", "DXC Reclamation App",
                "userName", email
        ));
    }

    @PostMapping("/register/verify")
    public ResponseEntity<?> registerVerify(@RequestBody PasskeyRegisterFinishRequest request,
                                            HttpSession session) {

        String email = (String) session.getAttribute("passkey_register_email");
        String challenge = (String) session.getAttribute("passkey_register_challenge");

        if (email == null || challenge == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Session expirée"));
        }

        passkeyService.savePasskeyForUser(email, request);

        session.removeAttribute("passkey_register_email");
        session.removeAttribute("passkey_register_challenge");

        return ResponseEntity.ok(Map.of("message", "Face ID activé avec succès"));
    }
}