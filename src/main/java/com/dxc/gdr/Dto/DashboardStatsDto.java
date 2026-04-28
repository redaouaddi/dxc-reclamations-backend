package com.dxc.gdr.Dto;

public class DashboardStatsDto {

    private long usersCount;
    private long reclamationsCount;
    private long enCoursCount;
    private double slaRespecte;

    public DashboardStatsDto(long usersCount,
                             long reclamationsCount,
                             long enCoursCount,
                             double slaRespecte) {
        this.usersCount = usersCount;
        this.reclamationsCount = reclamationsCount;
        this.enCoursCount = enCoursCount;
        this.slaRespecte = slaRespecte;
    }

    public long getUsersCount() {
        return usersCount;
    }

    public long getReclamationsCount() {
        return reclamationsCount;
    }

    public long getEnCoursCount() {
        return enCoursCount;
    }

    public double getSlaRespecte() {
        return slaRespecte;
    }
}