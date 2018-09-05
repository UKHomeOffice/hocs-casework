package uk.gov.digital.ho.hocs.casework.casedetails.queuedto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.casedetails.model.StageType;
import uk.gov.digital.ho.hocs.casework.domain.HocsCaseContext;
import uk.gov.digital.ho.hocs.casework.domain.HocsCommand;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

import static uk.gov.digital.ho.hocs.casework.casedetails.queuedto.UpdateDeadlinesRequest.UPDATE_DEADLINES_COMMAND;


@Getter
@JsonTypeName(UPDATE_DEADLINES_COMMAND)
public class UpdateDeadlinesRequest extends HocsCommand {

    static final String UPDATE_DEADLINES_COMMAND = "update_deadlines_command";

    private UUID caseUUID;

    private Map<StageType, LocalDate> deadlines;

    @JsonCreator
    public UpdateDeadlinesRequest(@JsonProperty(value = "caseUUID", required = true) UUID caseUUID,
                                  @JsonProperty(value = "deadlines", required = true) Map<StageType, LocalDate> deadlines) {
        super(UPDATE_DEADLINES_COMMAND);
        this.caseUUID = caseUUID;
        this.deadlines = deadlines;
    }

    @Override
    public void execute(HocsCaseContext hocsCaseContext) {
        initialiseDependencies(hocsCaseContext);
        deadlineDataService.updateDeadlines(caseUUID, deadlines);
    }

}
