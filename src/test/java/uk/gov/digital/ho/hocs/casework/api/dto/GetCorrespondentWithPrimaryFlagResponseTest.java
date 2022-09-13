package uk.gov.digital.ho.hocs.casework.api.dto;

import org.junit.Test;
import uk.gov.digital.ho.hocs.casework.domain.model.Address;
import uk.gov.digital.ho.hocs.casework.domain.model.Correspondent;
import uk.gov.digital.ho.hocs.casework.domain.model.CorrespondentWithPrimaryFlag;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class GetCorrespondentWithPrimaryFlagResponseTest {

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

        Correspondent correspondent = new Correspondent(caseUUID, type, fullName, organisation, address, phone, email,
            reference, externalKey);
        CorrespondentWithPrimaryFlag correspondentWithPrimaryFlag = new CorrespondentWithPrimaryFlag(correspondent,
            true);

        GetCorrespondentWithPrimaryFlagResponse getCorrespondentResponse = GetCorrespondentWithPrimaryFlagResponse.from(
            correspondentWithPrimaryFlag);

        assertThat(getCorrespondentResponse.getUuid()).isEqualTo(correspondentWithPrimaryFlag.getUuid());
        assertThat(getCorrespondentResponse.getCreated()).isEqualTo(correspondentWithPrimaryFlag.getCreated());
        assertThat(getCorrespondentResponse.getType()).isEqualTo(correspondentWithPrimaryFlag.getCorrespondentType());
        assertThat(getCorrespondentResponse.getCaseUUID()).isEqualTo(correspondentWithPrimaryFlag.getCaseUUID());
        assertThat(getCorrespondentResponse.getFullname()).isEqualTo(correspondentWithPrimaryFlag.getFullName());
        assertThat(getCorrespondentResponse.getOrganisation()).isEqualTo(
            correspondentWithPrimaryFlag.getOrganisation());
        assertThat(getCorrespondentResponse.getAddress().getPostcode()).isEqualTo(
            correspondentWithPrimaryFlag.getPostcode());
        assertThat(getCorrespondentResponse.getAddress().getAddress1()).isEqualTo(
            correspondentWithPrimaryFlag.getAddress1());
        assertThat(getCorrespondentResponse.getAddress().getAddress2()).isEqualTo(
            correspondentWithPrimaryFlag.getAddress2());
        assertThat(getCorrespondentResponse.getAddress().getAddress3()).isEqualTo(
            correspondentWithPrimaryFlag.getAddress3());
        assertThat(getCorrespondentResponse.getAddress().getCountry()).isEqualTo(
            correspondentWithPrimaryFlag.getCountry());
        assertThat(getCorrespondentResponse.getTelephone()).isEqualTo(correspondentWithPrimaryFlag.getTelephone());
        assertThat(getCorrespondentResponse.getEmail()).isEqualTo(correspondentWithPrimaryFlag.getEmail());
        assertThat(getCorrespondentResponse.getReference()).isEqualTo(correspondentWithPrimaryFlag.getReference());
        assertThat(getCorrespondentResponse.getExternalKey()).isEqualTo(correspondentWithPrimaryFlag.getExternalKey());
        assertThat(getCorrespondentResponse.getIsPrimary()).isEqualTo(correspondentWithPrimaryFlag.getIsPrimary());

    }

    @Test
    public void getCorrespondentDtoNull() {

        UUID caseUUID = UUID.randomUUID();
        String type = "CORRESPONDENT";

        Correspondent correspondent = new Correspondent(caseUUID, type, null, null, null, null, null, null, null);
        CorrespondentWithPrimaryFlag correspondentWithPrimaryFlag = new CorrespondentWithPrimaryFlag(correspondent,
            null);

        GetCorrespondentWithPrimaryFlagResponse getCorrespondentResponse = GetCorrespondentWithPrimaryFlagResponse.from(
            correspondentWithPrimaryFlag);

        assertThat(getCorrespondentResponse.getUuid()).isEqualTo(correspondentWithPrimaryFlag.getUuid());
        assertThat(getCorrespondentResponse.getCreated()).isEqualTo(correspondentWithPrimaryFlag.getCreated());
        assertThat(getCorrespondentResponse.getType()).isEqualTo(correspondentWithPrimaryFlag.getCorrespondentType());
        assertThat(getCorrespondentResponse.getCaseUUID()).isEqualTo(correspondentWithPrimaryFlag.getCaseUUID());
        assertThat(getCorrespondentResponse.getFullname()).isEqualTo(correspondentWithPrimaryFlag.getFullName());
        assertThat(getCorrespondentResponse.getOrganisation()).isEqualTo(
            correspondentWithPrimaryFlag.getOrganisation());
        assertThat(getCorrespondentResponse.getAddress().getPostcode()).isEqualTo(
            correspondentWithPrimaryFlag.getPostcode());
        assertThat(getCorrespondentResponse.getAddress().getAddress1()).isEqualTo(
            correspondentWithPrimaryFlag.getAddress1());
        assertThat(getCorrespondentResponse.getAddress().getAddress2()).isEqualTo(
            correspondentWithPrimaryFlag.getAddress2());
        assertThat(getCorrespondentResponse.getAddress().getAddress3()).isEqualTo(
            correspondentWithPrimaryFlag.getAddress3());
        assertThat(getCorrespondentResponse.getAddress().getCountry()).isEqualTo(
            correspondentWithPrimaryFlag.getCountry());
        assertThat(getCorrespondentResponse.getTelephone()).isEqualTo(correspondentWithPrimaryFlag.getTelephone());
        assertThat(getCorrespondentResponse.getEmail()).isEqualTo(correspondentWithPrimaryFlag.getEmail());
        assertThat(getCorrespondentResponse.getReference()).isEqualTo(correspondentWithPrimaryFlag.getReference());
        assertThat(getCorrespondentResponse.getExternalKey()).isEqualTo(correspondentWithPrimaryFlag.getExternalKey());
        assertThat(getCorrespondentResponse.getIsPrimary()).isEqualTo(correspondentWithPrimaryFlag.getIsPrimary());
    }

}
