package uk.gov.digital.ho.hocs.casework.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
public class ActionDataAppeal implements ActionDataEntity,Serializable {

    @Id
    @Column(name = "uuid")
    private UUID uuid;

    @Column(name = "action_uuid")
    private UUID actionUuid;

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
}
