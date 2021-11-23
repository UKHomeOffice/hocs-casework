package uk.gov.digital.ho.hocs.casework.domain.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.*;
import org.hibernate.annotations.Where;
import uk.gov.digital.ho.hocs.casework.api.dto.CaseDataType;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static uk.gov.digital.ho.hocs.casework.application.LogEvent.CASE_CREATE_FAILURE;

@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
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
                              ObjectMapper objectMapper,
                              LocalDate dateReceived) {
        this(type, caseNumber, dateReceived);
        update(data, objectMapper);
    }

    public ActiveCaseViewData(CaseDataType type, Long caseNumber, LocalDate dateReceived) {
        super(type, caseNumber, dateReceived);
    }

}
