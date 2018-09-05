package uk.gov.digital.ho.hocs.casework.casedetails.queuedto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.casedetails.model.ReferenceType;
import uk.gov.digital.ho.hocs.casework.domain.HocsCaseContext;
import uk.gov.digital.ho.hocs.casework.domain.HocsCommand;

import java.util.UUID;

import static uk.gov.digital.ho.hocs.casework.casedetails.queuedto.CreateReferenceRequest.CREATE_REFERENCE_COMMAND;


@Getter
@JsonTypeName(CREATE_REFERENCE_COMMAND)
public class CreateReferenceRequest extends HocsCommand {

    static final String CREATE_REFERENCE_COMMAND = "create_reference_command";

    @JsonProperty("caseUUID")
    private UUID caseUUID;

    @JsonProperty("type")
    private ReferenceType type;

    @JsonProperty("reference")
    private String reference;

    @JsonCreator
    public CreateReferenceRequest(@JsonProperty(value = "caseUUID", required = true) UUID caseUUID,
                                  @JsonProperty(value = "reference", required = true) String reference,
                                  @JsonProperty(value = "type", required = true) ReferenceType type) {
        super(CREATE_REFERENCE_COMMAND);
        this.caseUUID = caseUUID;
        this.reference = reference;
        this.type = type;
    }

    @Override
    public void execute(HocsCaseContext hocsCaseContext) {
        initialiseDependencies(hocsCaseContext);
        referenceDataService.createReference(caseUUID, reference, type);
    }
}
