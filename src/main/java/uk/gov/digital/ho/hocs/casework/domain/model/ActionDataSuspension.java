package uk.gov.digital.ho.hocs.casework.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "action_data_suspensions")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ActionDataSuspension implements Serializable {

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

    @Column(name = "date_suspension_applied")
    private LocalDate dateSuspensionApplied;

    @Column(name = "date_suspension_removed")
    private LocalDate dateSuspensionRemoved;

    public ActionDataSuspension(UUID caseTypeActionUuid,
                                String actionSubtype,
                                String caseTypeActionLabel,
                                String caseDataType,
                                UUID caseDataUuid,
                                LocalDate dateSuspensionApplied,
                                LocalDate dateSuspensionRemoved) {

        this.uuid = UUID.randomUUID();
        this.caseTypeActionUuid = caseTypeActionUuid;
        this.actionSubtype = actionSubtype;
        this.caseTypeActionLabel = caseTypeActionLabel;
        this.caseDataType = caseDataType;
        this.caseDataUuid = caseDataUuid;
        this.dateSuspensionApplied = dateSuspensionApplied;
        this.dateSuspensionRemoved = dateSuspensionRemoved;
    }

}
