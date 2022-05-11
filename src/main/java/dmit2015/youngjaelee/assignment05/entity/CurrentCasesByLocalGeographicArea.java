package dmit2015.youngjaelee.assignment05.entity;

import jakarta.json.bind.annotation.JsonbTransient;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.locationtech.jts.geom.MultiPolygon;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "ylee39CurrentCasesByLocalGeographicArea")
@Getter
@Setter
public class CurrentCasesByLocalGeographicArea implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    LocalDate date;

    String location;

    Integer vaccinationDataPopulation;

    Integer caseDataPopulation;

    Integer totalCases;

    Integer activeCases;

    Double activeCaseRate;

    Integer recoveredCases;

    Integer deaths;

    Integer oneDosedPopulation;

    Integer fullyDosedPopulation;

    Integer totalDosesAdministered;

    Double percentAtLeastOneDose;

    Double percentFullyImmunized;

    @JsonbTransient
    @Column(name = "polygon")
    private MultiPolygon polygon;

    @JsonbTransient
    private LocalDateTime createdDateTime;

    @PrePersist
    private void beforePersist() {
        createdDateTime = LocalDateTime.now();
    }
}
