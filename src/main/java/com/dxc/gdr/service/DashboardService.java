package com.dxc.gdr.service;

import com.dxc.gdr.Dto.response.ChartDataDto;
import com.dxc.gdr.Dto.DashboardStatsDto;
import com.dxc.gdr.dao.ReclamationRepository;
import com.dxc.gdr.dao.UserRepository;
import com.dxc.gdr.model.ReclamationStatus;
import com.dxc.gdr.model.SlaStatus;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DashboardService {

    private final ReclamationRepository reclamationRepository;
    private final UserRepository userRepository;

    public DashboardService(ReclamationRepository reclamationRepository,
                            UserRepository userRepository) {
        this.reclamationRepository = reclamationRepository;
        this.userRepository = userRepository;
    }

    public DashboardStatsDto getDashboardStats() {

        long usersCount = userRepository.count();
        long reclamationsCount = reclamationRepository.count();

        long enCoursCount = reclamationRepository.countByStatut(
                ReclamationStatus.EN_COURS
        );

        long slaRespecteCount = reclamationRepository.countBySlaStatus(
                SlaStatus.RESPECTE
        );

        double slaRespecte = 0;

        if (reclamationsCount > 0) {
            slaRespecte = Math.round(
                    (slaRespecteCount * 100.0) / reclamationsCount
            );
        }

        return new DashboardStatsDto(
                usersCount,
                reclamationsCount,
                enCoursCount,
                slaRespecte
        );
    }

    public List<ChartDataDto> getReclamationsByStatus() {

        List<Object[]> results = reclamationRepository.countByStatus();

        return results.stream()
                .map(r -> new ChartDataDto(
                        r[0].toString(),
                        ((Number) r[1]).longValue()
                ))
                .toList();
    }

    public List<ChartDataDto> getReclamationsByPriorite() {

        List<Object[]> results = reclamationRepository.countByPriorite();

        return results.stream()
                .map(r -> new ChartDataDto(
                        r[0].toString(),
                        ((Number) r[1]).longValue()
                ))
                .toList();
    }

    public List<ChartDataDto> getReclamationsByMonth() {

        List<Object[]> results = reclamationRepository.countByMonth();

        return results.stream()
                .map(r -> new ChartDataDto(
                        String.valueOf(((Number) r[0]).intValue()),
                        ((Number) r[1]).longValue()
                ))
                .toList();
    }

    public List<ChartDataDto> getReclamationsByCategorie() {

        List<Object[]> results = reclamationRepository.countByCategorie();

        return results.stream()
                .map(r -> new ChartDataDto(
                        r[0].toString(),
                        ((Number) r[1]).longValue()
                ))
                .toList();
    }
}