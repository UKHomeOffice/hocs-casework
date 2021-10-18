package uk.gov.digital.ho.hocs.casework.domain.model;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "action_appeals")
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

    @Column(name = "data")
    private String data;

    @Column(name = "created_timestamp")
    private LocalDateTime createTimestamp;

    @Column(name = "last_updated_timestamp")
    private LocalDateTime lastUpdateTimestamp;

    public ActionDataAppeal(UUID caseTypeActionUuid,
                            String caseTypeActionLabel,
                            String caseDataType, UUID caseDataUuid, String data) {
        this.caseTypeActionUuid = caseTypeActionUuid;
        this.caseTypeActionLabel = caseTypeActionLabel;
        this.caseDataType = caseDataType;
        this.caseDataUuid = caseDataUuid;
        this.data = data;
    }
}
