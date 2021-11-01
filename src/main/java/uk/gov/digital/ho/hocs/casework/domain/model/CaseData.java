package uk.gov.digital.ho.hocs.casework.domain.model;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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
@Entity
@Table(name = "case_data")
public class CaseData extends AbstractCaseData implements Serializable {

    public CaseData(CaseDataType type,
                    Long caseNumber,
                    Map<String, String> data,
                    ObjectMapper objectMapper,
                    LocalDate dateReceived) {
        super(type, caseNumber, data, objectMapper, dateReceived);
    }

    public CaseData(CaseDataType type, Long caseNumber, LocalDate dateReceived) {
        super(type, caseNumber, dateReceived);
    }

    private static UUID randomUUID(String shortCode) {
        if (shortCode != null) {
            String uuid = UUID.randomUUID().toString().substring(0, 33);
            return UUID.fromString(uuid.concat(shortCode));
        } else {
            throw new ApplicationExceptions.EntityCreationException("shortCode is null", CASE_CREATE_FAILURE);
        }
    }


    // --------  Migration Code Start --------
    public CaseData(CaseDataType type, String caseReference, Map<String, String> data, ObjectMapper objectMapper, LocalDate caseDeadline, LocalDate dateReceived, LocalDateTime caseCreated) {
        this(type, caseReference, caseDeadline, dateReceived, caseCreated);
        update(data, objectMapper);
    }

    public CaseData(CaseDataType type, String caseReference, LocalDate caseDeadline, LocalDate dateReceived, LocalDateTime caseCreated) {
        super(type, caseReference, caseDeadline, dateReceived, caseCreated);
    }

    // --------  Migration Code End --------

    public CaseData(Long id,
                    UUID uuid,
                    LocalDateTime created,
                    String type,
                    String reference,
                    boolean deleted,
                    String data,
                    UUID primaryTopicUUID,
                    Topic primaryTopic,
                    UUID primaryCorrespondentUUID,
                    Correspondent primaryCorrespondent,
                    LocalDate caseDeadline,
                    LocalDate caseDeadlineWarning,
                    LocalDate dateReceived,
                    boolean completed,
                    Set<ActiveStage> activeStages,
                    Set<CaseNote> caseNotes) {
        super(
                id,
                uuid,
                created,
                type,
                reference,
                deleted,
                data,
                primaryTopicUUID,
                primaryTopic,
                primaryCorrespondentUUID,
                primaryCorrespondent,
                caseDeadline,
                caseDeadlineWarning,
                dateReceived,
                completed,
                activeStages,
                caseNotes
        );
    }
}
