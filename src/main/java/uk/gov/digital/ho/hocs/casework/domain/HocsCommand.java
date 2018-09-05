package uk.gov.digital.ho.hocs.casework.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.casedetails.*;
import uk.gov.digital.ho.hocs.casework.casedetails.queuedto.*;


@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "command"
)
@JsonSubTypes({ // Keep this list alphabetical
        @JsonSubTypes.Type(value = CreateCorrespondentRequest.class),
        @JsonSubTypes.Type(value = CreateReferenceRequest.class),
        @JsonSubTypes.Type(value = UpdateDeadlinesRequest.class),
        @JsonSubTypes.Type(value = UpdateDocumentRequest.class),
        @JsonSubTypes.Type(value = UpdateInputDataRequest.class)
})

public abstract class HocsCommand implements Command {

    protected CaseDataService caseDataService;
    protected CorrespondentDataService correspondentDataService;
    protected DeadlineDataService deadlineDataService;
    protected DocumentDataService documentDataService;
    protected InputDataService inputDataService;
    protected ReferenceDataService referenceDataService;
    protected StageDataService stageDataService;

    @Getter
    @JsonProperty("command")
    protected String command;


    @JsonCreator
    public HocsCommand(String command) {
        this.command = command;
    }

    protected void initialiseDependencies(HocsCaseContext hocsCaseContext) {
        caseDataService = hocsCaseContext.getCaseDataService();
        correspondentDataService = hocsCaseContext.getCorrespondentDataService();
        deadlineDataService = hocsCaseContext.getDeadlineDataService();
        documentDataService = hocsCaseContext.getDocumentDataService();
        inputDataService = hocsCaseContext.getInputDataService();
        referenceDataService = hocsCaseContext.getReferenceDataService();
        stageDataService = hocsCaseContext.getStageDataService();
    }

}