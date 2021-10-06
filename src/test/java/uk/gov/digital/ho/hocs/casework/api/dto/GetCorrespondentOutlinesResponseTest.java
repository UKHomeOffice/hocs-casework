package uk.gov.digital.ho.hocs.casework.api.dto;

import org.junit.Test;
import uk.gov.digital.ho.hocs.casework.domain.model.Address;
import uk.gov.digital.ho.hocs.casework.domain.model.Correspondent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class GetCorrespondentOutlinesResponseTest {

    @Test
    public void getGetCorrespondentOutlinesResponse() {

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
        Set<Correspondent> correspondents = Set.of(correspondent);

        GetCorrespondentOutlinesResponse getCorrespondentOutlinesResponse = GetCorrespondentOutlinesResponse.from(correspondents);

        assertThat(getCorrespondentOutlinesResponse.getCorrespondents()).hasSize(1);
    }

    @Test
    public void getGetCorrespondentOutlinesResponseEmpty() {
        Set<Correspondent> correspondents = new HashSet<>();

        GetCorrespondentOutlinesResponse getCorrespondentOutlinesResponse = GetCorrespondentOutlinesResponse.from(correspondents);

        assertThat(getCorrespondentOutlinesResponse.getCorrespondents()).hasSize(0);
    }
}
