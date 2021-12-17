package uk.gov.digital.ho.hocs.casework.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.LocalDate;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "action_data_extensions")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ActionDataDeadlineExtension implements Serializable {

    @Id
    @Column(name = "uuid")
    private UUID uuid;

    @Column(name = "action_uuid")
    private UUID caseTypeActionUuid;

    @Column(name = "action_subtype")
    private String actionSubtype;

    @Column(name = "action_label")
    private String caseTypeActionLabel;

    @Column(name = "case_data_type")
    private String caseDataType;

    @Column(name = "case_data_uuid")
    private UUID caseDataUuid;

    @Column(name = "original_deadline")
    private LocalDate originalDeadline;

    @Column(name = "updated_deadline")
    private LocalDate updatedDeadline;

    @Column(name = "note")
    private String note;

    @Column(name = "reasons")
    private String reasons;


    @Column(name = "created_timestamp")
    private LocalDateTime createTimestamp;

    @Column(name = "last_updated_timestamp")
    private LocalDateTime lastUpdateTimestamp;

    public ActionDataDeadlineExtension(UUID caseTypeActionUuid,
                                       String caseTypeActionLabel,
                                       String caseDataType, UUID caseDataUuid,
                                       LocalDate originalDeadline, LocalDate updatedDeadline,
                                       String note,
                                       String reasons) {
        this.uuid = UUID.randomUUID();
        this.caseTypeActionUuid = caseTypeActionUuid;
        this.caseTypeActionLabel = caseTypeActionLabel;
        this.caseDataType = caseDataType;
        this.caseDataUuid = caseDataUuid;
        this.originalDeadline = originalDeadline;
        this.updatedDeadline = updatedDeadline;
        this.note = note;
        this.reasons = reasons;
    }

    @PrePersist
    private void setCreatedAndLastUpdatedTimestamps() {
        this.createTimestamp = LocalDateTime.now();
        this.lastUpdateTimestamp = LocalDateTime.now();
    }

    @PreUpdate
    private void updateLastUpdatedTimestamp() {
        this.lastUpdateTimestamp = LocalDateTime.now();
    }
}
