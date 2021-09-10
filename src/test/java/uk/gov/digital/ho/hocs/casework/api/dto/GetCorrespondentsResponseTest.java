package uk.gov.digital.ho.hocs.casework.api.dto;

import org.junit.Test;
import uk.gov.digital.ho.hocs.casework.domain.model.Address;
import uk.gov.digital.ho.hocs.casework.domain.model.Correspondent;
import uk.gov.digital.ho.hocs.casework.domain.model.CorrespondentWithPrimaryFlag;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class GetCorrespondentsResponseTest {

    @Test
    public void getGetCorrespondentsResponse() {

        UUID caseUUID = UUID.randomUUID();
        String type = "CORRESPONDENT";
        String fullName = "anyFullName";
        String organisation = "An Organisation";
        Address address = new Address("anyPostcode", "any1", "any2", "any3", "anyCountry");
        String phone = "anyPhone";
        String email = "anyEmail";
        String reference = "anyReference";
        String externalKey = "external key";
        Boolean isPrimary = true;

        CorrespondentWithPrimaryFlag correspondent = new CorrespondentWithPrimaryFlag(
                caseUUID,
                type,
                fullName,
                organisation,
                address,
                phone,
                email,
                reference,
                externalKey,
                isPrimary
        );

        Set<CorrespondentWithPrimaryFlag> correspondents = new HashSet<>();
        correspondents.add(correspondent);

        GetCorrespondentsResponse getCorrespondentsResponse = GetCorrespondentsResponse.from(correspondents);

        assertThat(getCorrespondentsResponse.getCorrespondents()).hasSize(1);

    }

    @Test
    public void getGetCorrespondentsResponseEmpty() {

        Set<CorrespondentWithPrimaryFlag> correspondents = new HashSet<>();

        GetCorrespondentsResponse getCorrespondentsResponse = GetCorrespondentsResponse.from(correspondents);

        assertThat(getCorrespondentsResponse.getCorrespondents()).hasSize(0);

    }
}
