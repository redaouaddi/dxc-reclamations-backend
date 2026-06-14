package com.dxc.gdr.controller;

import com.dxc.gdr.Dto.response.ChartDataDto;
import com.dxc.gdr.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.dxc.gdr.Dto.DashboardStatsDto;
import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;
    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }
    @GetMapping("/reclamations-status")
    public List<ChartDataDto> getStatusChart(
            @org.springframework.web.bind.annotation.RequestParam(required = false) Integer year,
            @org.springframework.web.bind.annotation.RequestParam(required = false) Integer month,
            @org.springframework.web.bind.annotation.RequestParam(required = false) com.dxc.gdr.model.ReclamationPriority priorite,
            @org.springframework.web.bind.annotation.RequestParam(required = false) Long equipeId){
        return dashboardService.getReclamationsByStatus(year, month, priorite, equipeId);
    }

    @GetMapping("/reclamations-priorite")
    public List<ChartDataDto> getPrioriteChart(
            @org.springframework.web.bind.annotation.RequestParam(required = false) Integer year,
            @org.springframework.web.bind.annotation.RequestParam(required = false) Integer month,
            @org.springframework.web.bind.annotation.RequestParam(required = false) com.dxc.gdr.model.ReclamationPriority priorite,
            @org.springframework.web.bind.annotation.RequestParam(required = false) Long equipeId){
        return dashboardService.getReclamationsByPriorite(year, month, priorite, equipeId);
    }

    @GetMapping("/reclamations-month")
    public List<ChartDataDto> getMonthChart(
            @org.springframework.web.bind.annotation.RequestParam(required = false) Integer year,
            @org.springframework.web.bind.annotation.RequestParam(required = false) Integer month,
            @org.springframework.web.bind.annotation.RequestParam(required = false) com.dxc.gdr.model.ReclamationPriority priorite,
            @org.springframework.web.bind.annotation.RequestParam(required = false) Long equipeId){
        return dashboardService.getReclamationsByMonth(year, month, priorite, equipeId);
    }

    @GetMapping("/reclamations-categorie")
    public List<ChartDataDto> getCategorieChart(
            @org.springframework.web.bind.annotation.RequestParam(required = false) Integer year,
            @org.springframework.web.bind.annotation.RequestParam(required = false) Integer month,
            @org.springframework.web.bind.annotation.RequestParam(required = false) com.dxc.gdr.model.ReclamationPriority priorite,
            @org.springframework.web.bind.annotation.RequestParam(required = false) Long equipeId){
        return dashboardService.getReclamationsByCategorie(year, month, priorite, equipeId);
    }

    @GetMapping("/stats")
    public DashboardStatsDto getDashboardStats(
            @org.springframework.web.bind.annotation.RequestParam(required = false) Integer year,
            @org.springframework.web.bind.annotation.RequestParam(required = false) Integer month,
            @org.springframework.web.bind.annotation.RequestParam(required = false) com.dxc.gdr.model.ReclamationPriority priorite,
            @org.springframework.web.bind.annotation.RequestParam(required = false) Long equipeId) {
        return dashboardService.getDashboardStats(year, month, priorite, equipeId);
    }
}