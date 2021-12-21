package uk.gov.digital.ho.hocs.casework.domain.model;

import lombok.*;
import uk.gov.digital.ho.hocs.casework.api.dto.CaseDataType;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "active_case")
public class ActiveCaseViewData extends AbstractCaseData implements Serializable {

    @Getter
    @Column(name = "primary_case_uuid", insertable = false, updatable = false)
    private UUID previousCaseUUID;

    @Getter
    @Column(name = "primary_case_reference", insertable = false, updatable = false)
    private String previousCaseReference;

    @Getter
    @Column(name = "primary_stage_uuid", insertable = false, updatable = false)
    private UUID previousCaseStageUUID;

    @Getter
    @Column(name = "secondary_case_uuid", insertable = false, updatable = false)
    private UUID nextCaseUUID;

    @Getter
    @Column(name = "secondary_case_reference", insertable = false, updatable = false)
    private String nextCaseReference;

    public ActiveCaseViewData(CaseDataType type,
                              Long caseNumber,
                              Map<String, String> data,
                              LocalDate dateReceived) {
        this(type, caseNumber, dateReceived);
        this.setDataMap(data);
    }

    public ActiveCaseViewData(CaseDataType type, Long caseNumber, LocalDate dateReceived) {
        super(type, caseNumber, dateReceived);
    }

}
