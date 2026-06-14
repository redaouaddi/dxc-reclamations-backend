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

    private boolean noFilters(Integer year, Integer month, com.dxc.gdr.model.ReclamationPriority priorite, Long equipeId) {
        return year == null && month == null && priorite == null && equipeId == null;
    }

    public DashboardStatsDto getDashboardStats(Integer year, Integer month, com.dxc.gdr.model.ReclamationPriority priorite, Long equipeId) {

        long usersCount = userRepository.count();
        long reclamationsCount, enCoursCount, slaRespecteCount;

        if (noFilters(year, month, priorite, equipeId)) {
            reclamationsCount = reclamationRepository.count();
            enCoursCount = reclamationRepository.countByStatut(ReclamationStatus.EN_COURS);
            slaRespecteCount = reclamationRepository.countBySlaStatus(SlaStatus.RESPECTE);
        } else {
            reclamationsCount = reclamationRepository.countFiltered(year, month, priorite, equipeId);
            enCoursCount = reclamationRepository.countByStatutFiltered(ReclamationStatus.EN_COURS, year, month, priorite, equipeId);
            slaRespecteCount = reclamationRepository.countBySlaStatusFiltered(SlaStatus.RESPECTE, year, month, priorite, equipeId);
        }

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

    public List<ChartDataDto> getReclamationsByStatus(Integer year, Integer month, com.dxc.gdr.model.ReclamationPriority priorite, Long equipeId) {

        List<Object[]> results;
        if (noFilters(year, month, priorite, equipeId)) {
            results = reclamationRepository.countByStatus();
        } else {
            results = reclamationRepository.countByStatusGroupFiltered(year, month, priorite, equipeId);
        }

        return results.stream()
                .map(r -> new ChartDataDto(
                        r[0].toString(),
                        ((Number) r[1]).longValue()
                ))
                .toList();
    }

    public List<ChartDataDto> getReclamationsByPriorite(Integer year, Integer month, com.dxc.gdr.model.ReclamationPriority priorite, Long equipeId) {

        List<Object[]> results;
        if (noFilters(year, month, priorite, equipeId)) {
            results = reclamationRepository.countByPriorite();
        } else {
            results = reclamationRepository.countByPrioriteGroupFiltered(year, month, priorite, equipeId);
        }

        return results.stream()
                .map(r -> new ChartDataDto(
                        r[0].toString(),
                        ((Number) r[1]).longValue()
                ))
                .toList();
    }

    public List<ChartDataDto> getReclamationsByMonth(Integer year, Integer month, com.dxc.gdr.model.ReclamationPriority priorite, Long equipeId) {

        List<Object[]> results;
        if (noFilters(year, month, priorite, equipeId)) {
            results = reclamationRepository.countByMonth();
        } else {
            results = reclamationRepository.countByMonthGroupFiltered(year, month, priorite, equipeId);
        }

        return results.stream()
                .map(r -> new ChartDataDto(
                        String.valueOf(((Number) r[0]).intValue()),
                        ((Number) r[1]).longValue()
                ))
                .toList();
    }

    public List<ChartDataDto> getReclamationsByCategorie(Integer year, Integer month, com.dxc.gdr.model.ReclamationPriority priorite, Long equipeId) {

        List<Object[]> results;
        if (noFilters(year, month, priorite, equipeId)) {
            results = reclamationRepository.countByCategorie();
        } else {
            results = reclamationRepository.countByCategorieGroupFiltered(year, month, priorite, equipeId);
        }

        return results.stream()
                .map(r -> new ChartDataDto(
                        r[0].toString(),
                        ((Number) r[1]).longValue()
                ))
                .toList();
    }
}