package com.dxc.gdr.Dto.request;

public class SlaConfigurationRequest {
    private String priorite;
    private Integer delaiHeures;

    public String getPriorite() {
        return priorite;
    }

    public void setPriorite(String priorite) {
        this.priorite = priorite;
    }

    public Integer getDelaiHeures() {
        return delaiHeures;
    }

    public void setDelaiHeures(Integer delaiHeures) {
        this.delaiHeures = delaiHeures;
    }
}