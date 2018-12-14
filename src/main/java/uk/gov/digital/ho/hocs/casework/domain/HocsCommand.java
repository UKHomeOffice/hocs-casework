package uk.gov.digital.ho.hocs.casework.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.api.CaseDataService;
import uk.gov.digital.ho.hocs.casework.api.CaseNoteService;
import uk.gov.digital.ho.hocs.casework.api.StageService;
import uk.gov.digital.ho.hocs.casework.queue.dto.UpdateCaseDataRequest;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "command"
)
@JsonSubTypes({ // Keep this list alphabetical
        @JsonSubTypes.Type(value = UpdateCaseDataRequest.class),
})

public abstract class HocsCommand implements Command {

    protected CaseDataService caseDataService;
    protected CaseNoteService caseNoteService;
    protected StageService stageService;

    @Getter
    @JsonProperty("command")
    protected String command;

    @JsonCreator
    public HocsCommand(String command) {
        this.command = command;
    }

    protected void initialiseDependencies(HocsCaseContext hocsCaseContext) {
        caseDataService = hocsCaseContext.getCaseDataService();
        caseNoteService = hocsCaseContext.getCaseNoteService();
        stageService = hocsCaseContext.getStageService();
    }

}