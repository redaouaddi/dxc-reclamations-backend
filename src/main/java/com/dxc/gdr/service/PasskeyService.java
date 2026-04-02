package com.dxc.gdr.service;

import com.dxc.gdr.model.Passkey;
import com.dxc.gdr.model.User;
import com.dxc.gdr.Dto.request.PasskeyRegisterFinishRequest;
import com.dxc.gdr.dao.PasskeyRepository;
import com.dxc.gdr.dao.UserRepository;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;

@Service
public class PasskeyService {

    private final PasskeyRepository passkeyRepository;
    private final UserRepository userRepository;

    public PasskeyService(PasskeyRepository passkeyRepository, UserRepository userRepository) {
        this.passkeyRepository = passkeyRepository;
        this.userRepository = userRepository;
    }

    public String generateChallenge() {
        byte[] challenge = new byte[32];
        new SecureRandom().nextBytes(challenge);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(challenge);
    }

    public void savePasskeyForUser(String email, PasskeyRegisterFinishRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        Passkey passkey = new Passkey();
        passkey.setUser(user);
        passkey.setCredentialId(request.getCredentialId());
        passkey.setPublicKey(request.getPublicKey());
        passkey.setSignCount(request.getSignCount() != null ? request.getSignCount() : 0L);
        passkey.setLabel(request.getLabel());

        passkeyRepository.save(passkey);
    }
}