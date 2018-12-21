package uk.gov.digital.ho.hocs.casework.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseDataType;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseNote;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@AllArgsConstructor
@Getter
public class CreateCaseNoteResponse {

    @JsonProperty("uuid")
    private UUID uuid;

    public static CreateCaseNoteResponse from(CaseNote caseNote) {
        return new CreateCaseNoteResponse(caseNote.getUuid());
    }
}