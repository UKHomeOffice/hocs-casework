package uk.gov.digital.ho.hocs.casework.api.dto;

import org.junit.Test;
import uk.gov.digital.ho.hocs.casework.domain.model.Address;
import uk.gov.digital.ho.hocs.casework.domain.model.Correspondent;
import uk.gov.digital.ho.hocs.casework.domain.model.CorrespondentType;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class GetCorrespondentsResponseTest {

    @Test
    public void getGetCorrespondentsResponse() {

        UUID caseUUID = UUID.randomUUID();
        CorrespondentType type = CorrespondentType.CORRESPONDENT;
        String fullName = "anyFullName";
        Address address = new Address("anyPostcode", "any1", "any2", "any3", "anyCountry");
        String phone = "anyPhone";
        String email = "anyEmail";
        String reference = "anyReference";

        Correspondent correspondent = new Correspondent(caseUUID, type, fullName, address, phone, email, reference);

        Set<Correspondent> correspondents = new HashSet<>();
        correspondents.add(correspondent);

        GetCorrespondentsResponse getCorrespondentsResponse = GetCorrespondentsResponse.from(correspondents);

        assertThat(getCorrespondentsResponse.getCorrespondents()).hasSize(1);

    }

    @Test
    public void getGetCorrespondentsResponseEmpty() {

        Set<Correspondent> correspondents = new HashSet<>();

        GetCorrespondentsResponse getCorrespondentsResponse = GetCorrespondentsResponse.from(correspondents);

        assertThat(getCorrespondentsResponse.getCorrespondents()).hasSize(0);

    }
}