package com.dxc.gdr.service;

import com.dxc.gdr.dao.SlaConfigurationRepository;
import com.dxc.gdr.model.ReclamationPriority;
import com.dxc.gdr.model.SlaConfiguration;
import org.springframework.stereotype.Service;

@Service
public class SlaConfigService {

    private final SlaConfigurationRepository slaConfigurationRepository;

    public SlaConfigService(SlaConfigurationRepository slaConfigurationRepository) {
        this.slaConfigurationRepository = slaConfigurationRepository;
    }

    public long getDelaiEnHeures(ReclamationPriority priorite) {
        return slaConfigurationRepository.findByPriorite(priorite)
                .map(config -> config.getDelaiHeures().longValue())
                .orElseGet(() -> getDefaultDelay(priorite));
    }

    private long getDefaultDelay(ReclamationPriority priorite) {
        if (priorite == null) {
            return 24;
        }

        return switch (priorite) {
            case ELEVEE -> 8;
            case MOYENNE -> 24;
            case FAIBLE -> 48;
        };
    }
}