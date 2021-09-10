package uk.gov.digital.ho.hocs.casework.api.dto;

import org.junit.Test;
import uk.gov.digital.ho.hocs.casework.domain.model.Address;
import uk.gov.digital.ho.hocs.casework.domain.model.Correspondent;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class GetCorrespondentResponseTest {

    @Test
    public void getCorrespondentDto() {

        UUID caseUUID = UUID.randomUUID();
        String type = "CORRESPONDENT";
        String fullName = "anyFullName";
        String organisation = "An Organisation";
        Address address = new Address("anyPostcode", "any1", "any2", "any3", "anyCountry");
        String phone = "anyPhone";
        String email = "anyEmail";
        String reference = "anyReference";
        String externalKey = "external key";

        Correspondent correspondent = new Correspondent(caseUUID, type, fullName, organisation, address, phone, email, reference, externalKey);

        GetCorrespondentResponse getCorrespondentResponse = GetCorrespondentResponse.from(correspondent);

        assertThat(getCorrespondentResponse.getUuid()).isEqualTo(correspondent.getUuid());
        assertThat(getCorrespondentResponse.getCreated()).isEqualTo(correspondent.getCreated());
        assertThat(getCorrespondentResponse.getType()).isEqualTo(correspondent.getCorrespondentType());
        assertThat(getCorrespondentResponse.getCaseUUID()).isEqualTo(correspondent.getCaseUUID());
        assertThat(getCorrespondentResponse.getFullname()).isEqualTo(correspondent.getFullName());
        assertThat(getCorrespondentResponse.getOrganisation()).isEqualTo(correspondent.getOrganisation());
        assertThat(getCorrespondentResponse.getAddress().getPostcode()).isEqualTo(correspondent.getPostcode());
        assertThat(getCorrespondentResponse.getAddress().getAddress1()).isEqualTo(correspondent.getAddress1());
        assertThat(getCorrespondentResponse.getAddress().getAddress2()).isEqualTo(correspondent.getAddress2());
        assertThat(getCorrespondentResponse.getAddress().getAddress3()).isEqualTo(correspondent.getAddress3());
        assertThat(getCorrespondentResponse.getAddress().getCountry()).isEqualTo(correspondent.getCountry());
        assertThat(getCorrespondentResponse.getTelephone()).isEqualTo(correspondent.getTelephone());
        assertThat(getCorrespondentResponse.getEmail()).isEqualTo(correspondent.getEmail());
        assertThat(getCorrespondentResponse.getReference()).isEqualTo(correspondent.getReference());
        assertThat(getCorrespondentResponse.getExternalKey()).isEqualTo(correspondent.getExternalKey());


    }

    @Test
    public void getCorrespondentDtoNull() {

        UUID caseUUID = UUID.randomUUID();
        String type = "CORRESPONDENT";

        Correspondent correspondent = new Correspondent(caseUUID, type, null, null, null, null, null, null, null);

        GetCorrespondentResponse getCorrespondentResponse = GetCorrespondentResponse.from(correspondent);

        assertThat(getCorrespondentResponse.getUuid()).isEqualTo(correspondent.getUuid());
        assertThat(getCorrespondentResponse.getCreated()).isEqualTo(correspondent.getCreated());
        assertThat(getCorrespondentResponse.getType()).isEqualTo(correspondent.getCorrespondentType());
        assertThat(getCorrespondentResponse.getCaseUUID()).isEqualTo(correspondent.getCaseUUID());
        assertThat(getCorrespondentResponse.getFullname()).isEqualTo(correspondent.getFullName());
        assertThat(getCorrespondentResponse.getOrganisation()).isEqualTo(correspondent.getOrganisation());
        assertThat(getCorrespondentResponse.getAddress().getPostcode()).isEqualTo(correspondent.getPostcode());
        assertThat(getCorrespondentResponse.getAddress().getAddress1()).isEqualTo(correspondent.getAddress1());
        assertThat(getCorrespondentResponse.getAddress().getAddress2()).isEqualTo(correspondent.getAddress2());
        assertThat(getCorrespondentResponse.getAddress().getAddress3()).isEqualTo(correspondent.getAddress3());
        assertThat(getCorrespondentResponse.getAddress().getCountry()).isEqualTo(correspondent.getCountry());
        assertThat(getCorrespondentResponse.getTelephone()).isEqualTo(correspondent.getTelephone());
        assertThat(getCorrespondentResponse.getEmail()).isEqualTo(correspondent.getEmail());
        assertThat(getCorrespondentResponse.getReference()).isEqualTo(correspondent.getReference());
        assertThat(getCorrespondentResponse.getExternalKey()).isEqualTo(correspondent.getExternalKey());

    }

}