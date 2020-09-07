package uk.gov.digital.ho.hocs.casework.api.dto;

import org.junit.Test;
import uk.gov.digital.ho.hocs.casework.domain.model.Address;
import uk.gov.digital.ho.hocs.casework.domain.model.Correspondent;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class GetCorrespondentOutlineResponseTest {

    @Test
    public void getCorrespondentOutlineResponseFromDto() {
        UUID caseUUID = UUID.randomUUID();
        String type = "CORRESPONDENT";
        String fullName = "anyFullName";
        Address address = new Address("anyPostcode", "any1", "any2", "any3", "anyCountry");
        String phone = "anyPhone";
        String email = "anyEmail";
        String reference = "anyReference";
        String externalKey = "external key";
        Correspondent correspondent = new Correspondent(caseUUID, type, fullName, address, phone, email, reference, externalKey);

        GetCorrespondentOutlineResponse getCorrespondentOutlineResponse = GetCorrespondentOutlineResponse.from(correspondent);

        assertThat(getCorrespondentOutlineResponse.getUuid()).isEqualTo(correspondent.getUuid());
        assertThat(getCorrespondentOutlineResponse.getFullname()).isEqualTo(correspondent.getFullName());
    }

    @Test
    public void getCorrespondentOutlineResponseFromNullDto() {
        UUID caseUUID = UUID.randomUUID();
        String type = "CORRESPONDENT";
        Correspondent correspondent = new Correspondent(caseUUID, type, null, null, null, null, null, null);

        GetCorrespondentOutlineResponse getCorrespondentOutlineResponse = GetCorrespondentOutlineResponse.from(correspondent);

        assertThat(getCorrespondentOutlineResponse.getUuid()).isEqualTo(correspondent.getUuid());
        assertThat(getCorrespondentOutlineResponse.getFullname()).isEqualTo(correspondent.getFullName());
    }
}
