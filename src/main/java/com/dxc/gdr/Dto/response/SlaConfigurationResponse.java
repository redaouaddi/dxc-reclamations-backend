package com.dxc.gdr.Dto.response;

public class SlaConfigurationResponse {
    private Long id;
    private String priorite;
    private Integer delaiHeures;

    public SlaConfigurationResponse() {
    }

    public SlaConfigurationResponse(Long id, String priorite, Integer delaiHeures) {
        this.id = id;
        this.priorite = priorite;
        this.delaiHeures = delaiHeures;
    }

    public Long getId() {
        return id;
    }

    public String getPriorite() {
        return priorite;
    }

    public Integer getDelaiHeures() {
        return delaiHeures;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setPriorite(String priorite) {
        this.priorite = priorite;
    }

    public void setDelaiHeures(Integer delaiHeures) {
        this.delaiHeures = delaiHeures;
    }
}