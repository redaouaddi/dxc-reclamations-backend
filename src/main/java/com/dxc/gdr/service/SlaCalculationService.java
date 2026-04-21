package com.dxc.gdr.service;

import com.dxc.gdr.model.Reclamation;
import com.dxc.gdr.model.ReclamationStatus;
import com.dxc.gdr.model.SlaStatus;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class SlaCalculationService {

    private final SlaConfigService slaConfigService;

    public SlaCalculationService(SlaConfigService slaConfigService) {
        this.slaConfigService = slaConfigService;
    }

    public void initialiserSla(Reclamation reclamation) {
        LocalDateTime now = LocalDateTime.now();

        if (reclamation.getDateCreation() == null) {
            reclamation.setDateCreation(now);
        }

        long delaiHeures = slaConfigService.getDelaiEnHeures(reclamation.getPriorite());
        reclamation.setSlaDeadline(reclamation.getDateCreation().plusHours(delaiHeures));
        reclamation.setSlaStatus(SlaStatus.EN_COURS);
    }

    public SlaStatus calculerSlaStatus(Reclamation reclamation) {
        if (reclamation.getSlaDeadline() == null) {
            return SlaStatus.EN_COURS;
        }

        if (estResolue(reclamation)) {
            if (reclamation.getDateResolution() != null
                    && !reclamation.getDateResolution().isAfter(reclamation.getSlaDeadline())) {
                return SlaStatus.RESPECTE;
            }
            return SlaStatus.DEPASSE;
        }

        LocalDateTime now = LocalDateTime.now();

        if (now.isAfter(reclamation.getSlaDeadline())) {
            return SlaStatus.DEPASSE;
        }

        Duration restant = Duration.between(now, reclamation.getSlaDeadline());

        if (restant.toMinutes() <= 60) {
            return SlaStatus.PROCHE_DEPASSEMENT;
        }

        return SlaStatus.EN_COURS;
    }

    public void recalculerSlaStatus(Reclamation reclamation) {
        reclamation.setSlaStatus(calculerSlaStatus(reclamation));
    }

    private boolean estResolue(Reclamation reclamation) {
        if (reclamation.getStatut() == null) {
            return false;
        }

        return reclamation.getStatut() == ReclamationStatus.TRAITEE;
    }
}