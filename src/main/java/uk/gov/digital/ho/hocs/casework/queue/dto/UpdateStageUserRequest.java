package uk.gov.digital.ho.hocs.casework.queue.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.domain.HocsCaseContext;
import uk.gov.digital.ho.hocs.casework.domain.HocsCommand;

import java.util.UUID;

import static uk.gov.digital.ho.hocs.casework.queue.dto.UpdateStageUserRequest.UPDATE_STAGE_USER_COMMAND;

@Getter
@JsonTypeName(UPDATE_STAGE_USER_COMMAND)
public class UpdateStageUserRequest extends HocsCommand {

    static final String UPDATE_STAGE_USER_COMMAND = "update_stage_user_command";

    private UUID caseUUID;

    private UUID stageUUID;

    private UUID userUUID;

    @JsonCreator
    public UpdateStageUserRequest(@JsonProperty(value = "caseUUID", required = true) UUID caseUUID,
                                  @JsonProperty(value = "stageUUID", required = true) UUID stageUUID,
                                  @JsonProperty(value = "userUUID", required = true) UUID userUUID) {
        super(UPDATE_STAGE_USER_COMMAND);
        this.caseUUID = caseUUID;
        this.stageUUID = stageUUID;
        this.userUUID = userUUID;

    }

    @Override
    public void execute(HocsCaseContext hocsCaseContext) {
        initialiseDependencies(hocsCaseContext);
        stageService.updateUser(caseUUID, stageUUID, userUUID);

    }
}