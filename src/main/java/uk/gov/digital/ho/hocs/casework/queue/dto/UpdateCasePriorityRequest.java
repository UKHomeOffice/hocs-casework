package uk.gov.digital.ho.hocs.casework.queue.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.domain.HocsCaseContext;
import uk.gov.digital.ho.hocs.casework.domain.HocsCommand;

import java.util.UUID;

import static uk.gov.digital.ho.hocs.casework.queue.dto.UpdateCasePriorityRequest.UPDATE_CASE_PRIORITY_COMMAND;

@Getter
@JsonTypeName(UPDATE_CASE_PRIORITY_COMMAND)
public class UpdateCasePriorityRequest extends HocsCommand {

    static final String UPDATE_CASE_PRIORITY_COMMAND = "update_case_priority_command";

    private UUID caseUUID;

    private boolean priority;

    @JsonCreator
    public UpdateCasePriorityRequest(@JsonProperty(value = "caseUUID", required = true) UUID caseUUID,
                                     @JsonProperty(value = "priority", required = true) boolean priority) {
        super(UPDATE_CASE_PRIORITY_COMMAND);
        this.caseUUID = caseUUID;
        this.priority = priority;
    }

    @Override
    public void execute(HocsCaseContext hocsCaseContext) {
        initialiseDependencies(hocsCaseContext);
        caseDataService.updatePriority(caseUUID, priority);

    }
}
