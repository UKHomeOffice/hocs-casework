package uk.gov.digital.ho.hocs.casework.domain.model;

import lombok.AllArgsConstructor;
import uk.gov.digital.ho.hocs.casework.api.dto.CaseDataType;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@AllArgsConstructor
@Entity
@Table(name = "case_data")
public class CaseData extends AbstractCaseData implements Serializable {

    public CaseData(CaseDataType type, Long caseNumber, Map<String, String> data, LocalDate dateReceived) {
        super(type, caseNumber, data, dateReceived);
    }

    public CaseData(CaseDataType type, Long caseNumber, Map<String, String> data, LocalDate dateReceived, LocalDateTime dateCreated, String migratedReference) {
        super(type, caseNumber, data, dateReceived, dateCreated, migratedReference);
    }

    public CaseData(CaseDataType type, Long caseNumber, LocalDate dateReceived) {
        super(type, caseNumber, dateReceived);
    }

    public CaseData(Long id,
                    UUID uuid,
                    LocalDateTime created,
                    String type,
                    String reference,
                    boolean deleted,
                    Map<String, String> data,
                    UUID primaryTopicUUID,
                    Topic primaryTopic,
                    UUID primaryCorrespondentUUID,
                    Correspondent primaryCorrespondent,
                    LocalDate caseDeadline,
                    LocalDate caseDeadlineWarning,
                    LocalDate dateReceived,
                    boolean completed,
                    LocalDateTime dateCompleted,
                    Set<ActiveStage> activeStages,
                    Set<CaseNote> caseNotes) {
        super(id, uuid, created, type, reference, null, deleted, data, primaryTopicUUID, primaryTopic,
            primaryCorrespondentUUID, primaryCorrespondent, caseDeadline, caseDeadlineWarning, dateReceived, completed,
            dateCompleted,activeStages, caseNotes);
    }

}
