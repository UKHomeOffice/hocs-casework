package uk.gov.digital.ho.hocs.casework.queue.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.domain.HocsCaseContext;
import uk.gov.digital.ho.hocs.casework.domain.HocsCommand;

import java.util.UUID;

import static uk.gov.digital.ho.hocs.casework.queue.dto.CompleteStageRequest.COMPLETE_STAGE_COMMAND;

@Getter
@JsonTypeName(COMPLETE_STAGE_COMMAND)
public class CompleteStageRequest extends HocsCommand {

    static final String COMPLETE_STAGE_COMMAND = "complete_stage_command";

    private UUID caseUUID;

    private UUID stageUUID;

    @JsonCreator
    public CompleteStageRequest(@JsonProperty(value = "caseUUID", required = true) UUID caseUUID,
                                @JsonProperty(value = "stageUUID", required = true) UUID stageUUID) {
        super(COMPLETE_STAGE_COMMAND);
        this.caseUUID = caseUUID;
        this.stageUUID = stageUUID;

    }

    @Override
    public void execute(HocsCaseContext hocsCaseContext) {
        initialiseDependencies(hocsCaseContext);
        stageService.completeStage(caseUUID, stageUUID);

    }
}