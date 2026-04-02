package com.dxc.gdr.dao;

import com.dxc.gdr.model.Passkey;
import com.dxc.gdr.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PasskeyRepository extends JpaRepository<Passkey, Long> {
    Optional<Passkey> findByCredentialId(String credentialId);
    List<Passkey> findByUser(User user);
}