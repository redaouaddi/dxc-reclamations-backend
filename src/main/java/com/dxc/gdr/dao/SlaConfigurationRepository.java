package com.dxc.gdr.dao;

import com.dxc.gdr.model.ReclamationPriority;
import com.dxc.gdr.model.SlaConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SlaConfigurationRepository extends JpaRepository<SlaConfiguration, Long> {
    Optional<SlaConfiguration> findByPriorite(ReclamationPriority priorite);
}