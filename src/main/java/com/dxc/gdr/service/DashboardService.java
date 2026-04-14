package com.dxc.gdr.service;

import com.dxc.gdr.Dto.response.ChartDataDto;
import com.dxc.gdr.dao.ReclamationRepository;

import org.springframework.stereotype.Service;

import java.util.List;

@Service

public class DashboardService {

    private final ReclamationRepository reclamationRepository;

    public DashboardService(ReclamationRepository reclamationRepository) {
        this.reclamationRepository = reclamationRepository;
    }
    public List<ChartDataDto> getReclamationsByStatus() {

        List<Object[]> results = reclamationRepository.countByStatus();

        return results.stream()
                .map(r -> new ChartDataDto(
                        r[0].toString(),
                        (Long) r[1]
                ))
                .toList();
    }

    public List<ChartDataDto> getReclamationsByPriorite() {

        List<Object[]> results = reclamationRepository.countByPriorite();

        return results.stream()
                .map(r -> new ChartDataDto(
                        r[0].toString(),
                        (Long) r[1]
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
                        (Long) r[1]
                ))
                .toList();
    }
}