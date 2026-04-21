package com.dxc.gdr.service;

import com.dxc.gdr.Dto.request.SlaConfigurationRequest;
import com.dxc.gdr.Dto.response.SlaConfigurationResponse;
import com.dxc.gdr.common.exception.BadRequestException;
import com.dxc.gdr.dao.SlaConfigurationRepository;
import com.dxc.gdr.model.ReclamationPriority;
import com.dxc.gdr.model.SlaConfiguration;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SlaAdminService {

    private final SlaConfigurationRepository slaConfigurationRepository;

    public SlaAdminService(SlaConfigurationRepository slaConfigurationRepository) {
        this.slaConfigurationRepository = slaConfigurationRepository;
    }

    public List<SlaConfigurationResponse> getAll() {
        return slaConfigurationRepository.findAll().stream()
                .map(cfg -> new SlaConfigurationResponse(
                        cfg.getId(),
                        cfg.getPriorite().name(),
                        cfg.getDelaiHeures()
                ))
                .toList();
    }

    public SlaConfigurationResponse saveOrUpdate(SlaConfigurationRequest request) {
        ReclamationPriority priorite;

        try {
            priorite = ReclamationPriority.valueOf(request.getPriorite().trim().toUpperCase());
        } catch (Exception e) {
            throw new BadRequestException("Priorité invalide");
        }

        if (request.getDelaiHeures() == null || request.getDelaiHeures() <= 0) {
            throw new BadRequestException("Le délai doit être supérieur à 0");
        }

        SlaConfiguration config = slaConfigurationRepository.findByPriorite(priorite)
                .orElse(new SlaConfiguration());

        config.setPriorite(priorite);
        config.setDelaiHeures(request.getDelaiHeures());

        SlaConfiguration saved = slaConfigurationRepository.save(config);

        return new SlaConfigurationResponse(
                saved.getId(),
                saved.getPriorite().name(),
                saved.getDelaiHeures()
        );
    }
}