package uk.gov.digital.ho.hocs.casework.api.dto;

import org.junit.Test;
import uk.gov.digital.ho.hocs.casework.domain.model.Address;
import uk.gov.digital.ho.hocs.casework.domain.model.Correspondent;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class CorrespondentDtoTest {

    @Test
    public void getCorrespondentDto() {

        UUID caseUUID = UUID.randomUUID();
        String type = "CORRESPONDENT";
        String fullName = "anyFullName";
        Address address = new Address("anyPostcode", "any1", "any2", "any3", "anyCountry");
        String phone = "anyPhone";
        String email = "anyEmail";
        String reference = "anyReference";

        Correspondent correspondent = new Correspondent(caseUUID, type, fullName, address, phone, email, reference);

        CorrespondentDto correspondentDto = CorrespondentDto.from(correspondent);

        assertThat(correspondentDto.getUuid()).isEqualTo(correspondent.getUuid());
        assertThat(correspondentDto.getCreated()).isEqualTo(correspondent.getCreated());
        assertThat(correspondentDto.getType()).isEqualTo(correspondent.getCorrespondentType());
        assertThat(correspondentDto.getCaseUUID()).isEqualTo(correspondent.getCaseUUID());
        assertThat(correspondentDto.getFullname()).isEqualTo(correspondent.getFullName());

        assertThat(correspondentDto.getAddress().getPostcode()).isEqualTo(correspondent.getPostcode());
        assertThat(correspondentDto.getAddress().getAddress1()).isEqualTo(correspondent.getAddress1());
        assertThat(correspondentDto.getAddress().getAddress2()).isEqualTo(correspondent.getAddress2());
        assertThat(correspondentDto.getAddress().getAddress3()).isEqualTo(correspondent.getAddress3());
        assertThat(correspondentDto.getAddress().getCountry()).isEqualTo(correspondent.getCountry());
        assertThat(correspondentDto.getTelephone()).isEqualTo(correspondent.getTelephone());
        assertThat(correspondentDto.getEmail()).isEqualTo(correspondent.getEmail());
        assertThat(correspondentDto.getReference()).isEqualTo(correspondent.getReference());

    }

    @Test
    public void getCorrespondentDtoNull() {

        UUID caseUUID = UUID.randomUUID();
        String type = "CORRESPONDENT";

        Correspondent correspondent = new Correspondent(caseUUID, type, null, null, null, null, null);

        CorrespondentDto correspondentDto = CorrespondentDto.from(correspondent);

        assertThat(correspondentDto.getUuid()).isEqualTo(correspondent.getUuid());
        assertThat(correspondentDto.getCreated()).isEqualTo(correspondent.getCreated());
        assertThat(correspondentDto.getType()).isEqualTo(correspondent.getCorrespondentType());
        assertThat(correspondentDto.getCaseUUID()).isEqualTo(correspondent.getCaseUUID());
        assertThat(correspondentDto.getFullname()).isEqualTo(correspondent.getFullName());
        assertThat(correspondentDto.getAddress().getPostcode()).isEqualTo(correspondent.getPostcode());
        assertThat(correspondentDto.getAddress().getAddress1()).isEqualTo(correspondent.getAddress1());
        assertThat(correspondentDto.getAddress().getAddress2()).isEqualTo(correspondent.getAddress2());
        assertThat(correspondentDto.getAddress().getAddress3()).isEqualTo(correspondent.getAddress3());
        assertThat(correspondentDto.getAddress().getCountry()).isEqualTo(correspondent.getCountry());
        assertThat(correspondentDto.getTelephone()).isEqualTo(correspondent.getTelephone());
        assertThat(correspondentDto.getEmail()).isEqualTo(correspondent.getEmail());
        assertThat(correspondentDto.getReference()).isEqualTo(correspondent.getReference());

    }

}