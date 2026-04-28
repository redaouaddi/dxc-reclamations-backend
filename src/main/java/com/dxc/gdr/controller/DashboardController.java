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
    public List<ChartDataDto> getStatusChart(){
        return dashboardService.getReclamationsByStatus();
    }

    @GetMapping("/reclamations-priorite")
    public List<ChartDataDto> getPrioriteChart(){
        return dashboardService.getReclamationsByPriorite();
    }

    @GetMapping("/reclamations-month")
    public List<ChartDataDto> getMonthChart(){
        return dashboardService.getReclamationsByMonth();
    }

    @GetMapping("/reclamations-categorie")
    public List<ChartDataDto> getCategorieChart(){
        return dashboardService.getReclamationsByCategorie();
    }

    @GetMapping("/stats")
    public DashboardStatsDto getDashboardStats() {
        return dashboardService.getDashboardStats();
    }
}