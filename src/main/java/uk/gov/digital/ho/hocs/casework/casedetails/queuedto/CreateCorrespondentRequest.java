package uk.gov.digital.ho.hocs.casework.casedetails.queuedto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CorrespondentData;
import uk.gov.digital.ho.hocs.casework.casedetails.model.CorrespondentType;
import uk.gov.digital.ho.hocs.casework.domain.HocsCaseContext;
import uk.gov.digital.ho.hocs.casework.domain.HocsCommand;

import java.util.UUID;

import static uk.gov.digital.ho.hocs.casework.casedetails.queuedto.CreateCorrespondentRequest.CREATE_CORRESPONDENT_COMMAND;

@Getter
@JsonTypeName(CREATE_CORRESPONDENT_COMMAND)
public class CreateCorrespondentRequest extends HocsCommand {

    static final String CREATE_CORRESPONDENT_COMMAND = "create_correspondent_command";

    private UUID caseUUID;

    private String fullname;

    private String postcode;

    private String address1;

    private String address2;

    private String address3;

    private String country;

    private String telephone;

    private String email;

    private CorrespondentType type;

    @JsonCreator
    public CreateCorrespondentRequest(
            @JsonProperty(value = "caseUUID", required = true) UUID caseUUID,
            @JsonProperty("fullname") String fullname,
            @JsonProperty("postcode") String postcode,
            @JsonProperty("address1") String address1,
            @JsonProperty("address2") String address2,
            @JsonProperty("address3") String address3,
            @JsonProperty("country") String country,
            @JsonProperty("telephone") String telephone,
            @JsonProperty("email") String email,
            @JsonProperty(value = "type", required = true) CorrespondentType correspondentType) {
        super(CREATE_CORRESPONDENT_COMMAND);
        this.caseUUID = caseUUID;
        this.fullname = fullname;
        this.postcode = postcode;
        this.address1 = address1;
        this.address2 = address2;
        this.address3 = address3;
        this.country = country;
        this.telephone = telephone;
        this.email = email;
        this.type = correspondentType;
    }

    public static CreateCorrespondentRequest from(CorrespondentData correspondentData, CorrespondentType correspondentType) {
        return new CreateCorrespondentRequest(
                correspondentData.getUuid(),
                correspondentData.getFullName(),
                correspondentData.getPostcode(),
                correspondentData.getAddress1(),
                correspondentData.getAddress2() ,
                correspondentData.getAddress3() ,
                correspondentData.getCountry(),
                correspondentData.getTelephone(),
                correspondentData.getEmail(),
                correspondentType);
    }

    @Override
    public void execute(HocsCaseContext hocsCaseContext) {
        initialiseDependencies(hocsCaseContext);
        correspondentDataService.createCorrespondent(caseUUID, fullname, postcode, address1, address2, address3, country, telephone, email, type);
    }

}
