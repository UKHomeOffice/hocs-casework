package uk.gov.digital.ho.hocs.casework.domain.model;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Entity
@Table(name = "exemption")
public class Exemption {
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    @Getter
    @Column(name = "uuid")
    protected UUID uuid;

    @Getter
    @Column(name = "case_uuid")
    protected UUID caseUUID;

    @Getter
    @Column(name = "type")
    protected String exemptionType;

    @Setter
    @Getter
    @Column(name = "created")
    protected LocalDateTime created;

    public Exemption(UUID caseUUID, String exemptionType) {
        this.caseUUID = caseUUID;
        this.exemptionType = exemptionType;
        this.uuid = UUID.randomUUID();
        this.created = LocalDateTime.now();

    }
}
