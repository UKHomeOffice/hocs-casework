package uk.gov.digital.ho.hocs.casework.domain.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "action_data_external_interest")
@Getter
@Setter
@NoArgsConstructor
public class ActionDataExternalInterest implements Serializable {

    @Id
    @Column(name = "uuid")
    private UUID uuid;

    @Column(name = "action_uuid")
    private UUID caseTypeActionUuid;

    @Column(name = "action_label")
    private String caseTypeActionLabel;

    @Column(name = "case_data_type")
    private String caseDataType;

    @Column(name = "case_data_uuid")
    private UUID caseDataUuid;

    @Column(name = "party_type")
    private String partyType;

    @Column(name = "details_of_interest")
    private String detailsOfInterest;

    @Column(name = "created_timestamp")
    private LocalDateTime createTimestamp;

    @Column(name = "last_updated_timestamp")
    private LocalDateTime lastUpdateTimestamp;

    public ActionDataExternalInterest(UUID caseTypeActionUuid,
                                      String caseTypeActionLabel,
                                      String caseDataType,
                                      UUID caseDataUuid,
                                      String partyType,
                                      String detailsOfInterest) {
        this.uuid = UUID.randomUUID();

        this.caseTypeActionUuid = caseTypeActionUuid;
        this.caseTypeActionLabel = caseTypeActionLabel;
        this.caseDataType = caseDataType;

        this.caseDataUuid = caseDataUuid;
        this.partyType = partyType;
        this.detailsOfInterest = detailsOfInterest;
    }
}
