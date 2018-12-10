package uk.gov.digital.ho.hocs.casework.queue.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.api.dto.AddressDto;
import uk.gov.digital.ho.hocs.casework.domain.HocsCaseContext;
import uk.gov.digital.ho.hocs.casework.domain.HocsCommand;
import uk.gov.digital.ho.hocs.casework.domain.model.Address;

import java.util.UUID;

import static uk.gov.digital.ho.hocs.casework.queue.dto.CreateCorrespondentRequest.CREATE_CORRESPONDENT_COMMAND;

@Getter
@JsonTypeName(CREATE_CORRESPONDENT_COMMAND)
public class CreateCorrespondentRequest extends HocsCommand {

    static final String CREATE_CORRESPONDENT_COMMAND = "create_correspondent_command";

    private UUID caseUUID;

    private String type;

    private String fullname;

    private AddressDto address;

    private String telephone;

    private String email;

    private String reference;

    @JsonCreator
    public CreateCorrespondentRequest(
            @JsonProperty(value = "caseUUID", required = true) UUID caseUUID,
            @JsonProperty(value = "type", required = true) String correspondentType,
            @JsonProperty("fullname") String fullname,
            @JsonProperty("address") AddressDto address,
            @JsonProperty("telephone") String telephone,
            @JsonProperty("email") String email,
            @JsonProperty("reference") String reference) {
        super(CREATE_CORRESPONDENT_COMMAND);
        this.caseUUID = caseUUID;
        this.type = correspondentType;
        this.fullname = fullname;
        this.address = address;
        this.telephone = telephone;
        this.email = email;
        this.reference = reference;
    }

    @Override
    public void execute(HocsCaseContext hocsCaseContext) {
        initialiseDependencies(hocsCaseContext);
        Address addr = new Address(address.getPostcode(), address.getAddress1(), address.getAddress2(), address.getAddress3(), address.getCountry());
        correspondentService.createCorrespondent(caseUUID, type, fullname, addr, telephone, email, reference);
    }

}