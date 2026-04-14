package com.dxc.gdr.dao;

import com.dxc.gdr.model.MessageInterne;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MessageInterneRepository extends JpaRepository<MessageInterne, Long> {

    List<MessageInterne> findByReclamationIdOrderByDateEnvoiAsc(Long reclamationId);
}