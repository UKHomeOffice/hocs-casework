package uk.gov.digital.ho.hocs.casework.casedetails.queuedto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.domain.HocsCaseContext;
import uk.gov.digital.ho.hocs.casework.domain.HocsCommand;

import java.util.Map;
import java.util.UUID;

import static uk.gov.digital.ho.hocs.casework.casedetails.queuedto.UpdateInputDataRequest.UPDATE_INPUT_DATA_COMMAND;

@Getter
@JsonTypeName(UPDATE_INPUT_DATA_COMMAND)
public class UpdateInputDataRequest extends HocsCommand {

    static final String UPDATE_INPUT_DATA_COMMAND = "update_input_data_command";

    private UUID caseUUID;

    private Map<String, String> data;

    @JsonCreator
    public UpdateInputDataRequest(@JsonProperty(value = "caseUUID", required = true) UUID caseUUID,
                                  @JsonProperty(value = "data", required = true) Map<String, String> data) {
        super(UPDATE_INPUT_DATA_COMMAND);
        this.caseUUID = caseUUID;
        this.data = data;
    }

    @Override
    public void execute(HocsCaseContext hocsCaseContext) {
        initialiseDependencies(hocsCaseContext);
        caseDataService.updateCaseData(caseUUID, data);

    }
}
