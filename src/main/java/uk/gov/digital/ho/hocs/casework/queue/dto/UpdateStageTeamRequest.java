package uk.gov.digital.ho.hocs.casework.queue.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.domain.HocsCaseContext;
import uk.gov.digital.ho.hocs.casework.domain.HocsCommand;

import java.util.UUID;

import static uk.gov.digital.ho.hocs.casework.queue.dto.UpdateStageTeamRequest.UPDATE_STAGE_TEAM_COMMAND;

@Getter
@JsonTypeName(UPDATE_STAGE_TEAM_COMMAND)
public class UpdateStageTeamRequest extends HocsCommand {

    static final String UPDATE_STAGE_TEAM_COMMAND = "update_stage_team_command";

    private UUID caseUUID;

    private UUID stageUUID;

    private UUID teamUUID;

    @JsonCreator
    public UpdateStageTeamRequest(@JsonProperty(value = "caseUUID", required = true) UUID caseUUID,
                                  @JsonProperty(value = "stageUUID", required = true) UUID stageUUID,
                                  @JsonProperty(value = "teamUUID", required = true) UUID teamUUID) {
        super(UPDATE_STAGE_TEAM_COMMAND);
        this.caseUUID = caseUUID;
        this.stageUUID = stageUUID;
        this.teamUUID = teamUUID;

    }

    @Override
    public void execute(HocsCaseContext hocsCaseContext) {
        initialiseDependencies(hocsCaseContext);
        stageService.updateTeam(caseUUID, stageUUID, teamUUID);

    }
}