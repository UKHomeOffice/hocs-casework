package uk.gov.digital.ho.hocs.casework.queue.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.domain.HocsCaseContext;
import uk.gov.digital.ho.hocs.casework.domain.HocsCommand;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseNoteType;

import java.util.UUID;

import static uk.gov.digital.ho.hocs.casework.queue.dto.CreateCaseNoteRequest.ADD_CASE_NOTE_DATA_COMMAND;

@Getter
@JsonTypeName(ADD_CASE_NOTE_DATA_COMMAND)
public class CreateCaseNoteRequest extends HocsCommand {

    static final String ADD_CASE_NOTE_DATA_COMMAND = "create_case_note_command";

    private UUID caseUUID;

    private CaseNoteType caseNoteType;

    private String caseNote;

    @JsonCreator
    public CreateCaseNoteRequest(@JsonProperty(value = "caseUUID", required = true) UUID caseUUID,
                                 @JsonProperty(value = "caseNoteType", required = true) CaseNoteType caseNoteType,
                                 @JsonProperty(value = "casenote", required = true) String caseNote) {
        super(ADD_CASE_NOTE_DATA_COMMAND);
        this.caseUUID = caseUUID;
        this.caseNoteType = caseNoteType;
        this.caseNote = caseNote;
    }

    @Override
    public void execute(HocsCaseContext hocsCaseContext) {
        initialiseDependencies(hocsCaseContext);
        caseNoteService.createCaseNote(caseUUID, caseNoteType, caseNote);

    }
}
