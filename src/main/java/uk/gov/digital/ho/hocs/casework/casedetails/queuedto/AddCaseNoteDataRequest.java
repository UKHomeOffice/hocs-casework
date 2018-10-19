package uk.gov.digital.ho.hocs.casework.casedetails.queuedto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.domain.HocsCaseContext;
import uk.gov.digital.ho.hocs.casework.domain.HocsCommand;

import java.util.UUID;

import static uk.gov.digital.ho.hocs.casework.casedetails.queuedto.AddCaseNoteDataRequest.ADD_CASE_NOTE_DATA_COMMAND;

@Getter
@JsonTypeName(ADD_CASE_NOTE_DATA_COMMAND)
public class AddCaseNoteDataRequest extends HocsCommand {

    static final String ADD_CASE_NOTE_DATA_COMMAND = "add_case_note_data_command";

    private UUID caseUUID;

    private String caseNote;

    @JsonCreator
    public AddCaseNoteDataRequest(@JsonProperty(value = "caseUUID", required = true) UUID caseUUID,
                                  @JsonProperty(value = "casenote", required = true) String caseNote) {
        super(ADD_CASE_NOTE_DATA_COMMAND);
        this.caseUUID = caseUUID;
        this.caseNote = caseNote;
    }

    @Override
    public void execute(HocsCaseContext hocsCaseContext) {
        initialiseDependencies(hocsCaseContext);
        caseNoteService.createCaseNote(caseUUID, caseNote);

    }
}
