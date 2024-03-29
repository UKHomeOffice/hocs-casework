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

    private final Correspondent correspondent = new Correspondent(UUID.randomUUID(), "CORRESPONDENT", "anyFullName",
        "anyOrganisation", new Address("anyPostcode", "any1", "any2", "any3", "anyCountry"), "anyPhone", "anyEmail",
        "anyReference", "external key");

    @Test
    public void getGetCorrespondentsResponse() {
        Set<CorrespondentWithPrimaryFlag> correspondents = Set.of(
            new CorrespondentWithPrimaryFlag(correspondent, true));

        GetCorrespondentsResponse getCorrespondentsResponse = GetCorrespondentsResponse.from(correspondents);

        assertThat(getCorrespondentsResponse.getCorrespondents()).hasSize(1);
    }

    @Test
    public void getGetCorrespondentsResponse_withDisplayName() {
        CorrespondentWithPrimaryFlag correspondentVal = new CorrespondentWithPrimaryFlag(correspondent, true);
        String displayName = "Super Awesome Name";

        correspondentVal.setCorrespondentTypeName(displayName);

        Set<CorrespondentWithPrimaryFlag> correspondents = Set.of(correspondentVal);

        GetCorrespondentsResponse getCorrespondentsResponse = GetCorrespondentsResponse.from(correspondents);

        assertThat(getCorrespondentsResponse.getCorrespondents()).hasSize(1);
        assertThat(getCorrespondentsResponse.getCorrespondents().stream().findFirst().get().getTypeDisplayName())
            .isEqualTo(displayName);
    }

    @Test
    public void getGetCorrespondentsResponseEmpty() {
        Set<CorrespondentWithPrimaryFlag> correspondents = new HashSet<>();

        GetCorrespondentsResponse getCorrespondentsResponse = GetCorrespondentsResponse.from(correspondents);

        assertThat(getCorrespondentsResponse.getCorrespondents()).hasSize(0);
    }

}
