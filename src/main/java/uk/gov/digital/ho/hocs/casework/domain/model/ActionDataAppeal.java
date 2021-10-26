package uk.gov.digital.ho.hocs.casework.domain.model;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "action_data_appeals")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class ActionDataAppeal implements Serializable {

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

    @Column(name = "status")
    private String status;

    @Column(name = "date_sent_rms")
    private LocalDate dateSentRMS;

    @Column(name = "outcome")
    private String outcome;

    @Column(name = "complex_case")
    private Boolean complexCase;

    @Column(name = "note")
    private String note;

    @Column(name = "appeal_officer_data")
    private String appealOfficerData;

    @Column(name = "created_timestamp")
    private LocalDateTime createTimestamp;

    @Column(name = "last_updated_timestamp")
    private LocalDateTime lastUpdateTimestamp;

    public ActionDataAppeal(UUID caseTypeActionUuid,
                            String caseTypeActionLabel,
                            String caseDataType, UUID caseDataUuid,
                            String status,
                            LocalDate dateSentRMS,
                            String outcome,
                            Boolean complexCase,
                            String note,
                            String appealOfficerData) {
        this.uuid = UUID.randomUUID();
        this.caseTypeActionUuid = caseTypeActionUuid;
        this.caseTypeActionLabel = caseTypeActionLabel;
        this.caseDataType = caseDataType;
        this.caseDataUuid = caseDataUuid;
        this.status = status;
        this.dateSentRMS = dateSentRMS;
        this.outcome = outcome;
        this.complexCase = complexCase;
        this.note = note;
        this.appealOfficerData = appealOfficerData;
    }
}
