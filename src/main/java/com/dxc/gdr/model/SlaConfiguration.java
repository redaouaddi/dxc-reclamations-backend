package com.dxc.gdr.model;

import jakarta.persistence.*;

@Entity
@Table(name = "sla_configurations")
public class SlaConfiguration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private ReclamationPriority priorite;

    @Column(nullable = false)
    private Integer delaiHeures;

    public SlaConfiguration() {
    }

    public SlaConfiguration(ReclamationPriority priorite, Integer delaiHeures) {
        this.priorite = priorite;
        this.delaiHeures = delaiHeures;
    }

    public Long getId() {
        return id;
    }

    public ReclamationPriority getPriorite() {
        return priorite;
    }

    public void setPriorite(ReclamationPriority priorite) {
        this.priorite = priorite;
    }

    public Integer getDelaiHeures() {
        return delaiHeures;
    }

    public void setDelaiHeures(Integer delaiHeures) {
        this.delaiHeures = delaiHeures;
    }
}