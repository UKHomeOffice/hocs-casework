package uk.gov.digital.ho.hocs.casework.queue.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.domain.HocsCaseContext;
import uk.gov.digital.ho.hocs.casework.domain.HocsCommand;

import java.time.LocalDate;
import java.util.UUID;

import static uk.gov.digital.ho.hocs.casework.queue.dto.UpdateStageDeadlineRequest.UPDATE_STAGE_DEADLINE_COMMAND;

@Getter
@JsonTypeName(UPDATE_STAGE_DEADLINE_COMMAND)
public class UpdateStageDeadlineRequest extends HocsCommand {

    static final String UPDATE_STAGE_DEADLINE_COMMAND = "update_stage_deadline_command";

    private UUID caseUUID;

    private UUID stageUUID;

    private LocalDate deadline;

    @JsonCreator
    public UpdateStageDeadlineRequest(@JsonProperty(value = "caseUUID", required = true) UUID caseUUID,
                                      @JsonProperty(value = "stageUUID", required = true) UUID stageUUID,
                                      @JsonProperty(value = "deadline", required = true) LocalDate deadline) {
        super(UPDATE_STAGE_DEADLINE_COMMAND);
        this.caseUUID = caseUUID;
        this.stageUUID = stageUUID;
        this.deadline = deadline;

    }

    @Override
    public void execute(HocsCaseContext hocsCaseContext) {
        initialiseDependencies(hocsCaseContext);
        stageService.updateDeadline(caseUUID, stageUUID, deadline);

    }
}