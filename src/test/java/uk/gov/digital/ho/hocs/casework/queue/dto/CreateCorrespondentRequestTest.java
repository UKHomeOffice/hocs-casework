package uk.gov.digital.ho.hocs.casework.queue.dto;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.api.*;
import uk.gov.digital.ho.hocs.casework.api.dto.AddressDto;
import uk.gov.digital.ho.hocs.casework.domain.HocsCaseContext;
import uk.gov.digital.ho.hocs.casework.domain.model.Address;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class CreateCorrespondentRequestTest {

    @Mock
    CaseDataService caseDataService;

    @Mock
    CaseNoteService caseNoteService;

    @Mock
    CorrespondentService correspondentService;

    @Mock
    StageService stageService;

    @Mock
    TopicService topicService;

    private HocsCaseContext hocsCaseContext;

    private String commandName = "create_correspondent_command";

    @Before
    public void setUp() {
        this.hocsCaseContext = new HocsCaseContext(caseDataService, caseNoteService, correspondentService, stageService, topicService);
    }

    @Test
    public void getCompleteStageRequest() {

        UUID caseUUID = UUID.randomUUID();
        String correspondentType = "CORRESPONDENT";
        String fullname = "anyName";
        String postcode = "anyPostcode";
        String address1 = "anyAddress1";
        String address2 = "anyAddress2";
        String address3 = "anyAddress3";
        String country = "anyCountry";
        String telephone = "anyPhone";
        String email = "anyEmail";
        String reference = "anyRef";

        AddressDto addressDto = new AddressDto(postcode,
                address1,
                address2,
                address3,
                country);
        CreateCorrespondentRequest createCorrespondentRequest =
                new CreateCorrespondentRequest(caseUUID,
                        correspondentType,
                        fullname,
                        addressDto,
                        telephone,
                        email,
                        reference);

        assertThat(createCorrespondentRequest.getCommand()).isEqualTo(commandName);
        assertThat(createCorrespondentRequest.getCaseUUID()).isEqualTo(caseUUID);
        assertThat(createCorrespondentRequest.getType()).isEqualTo(correspondentType);
        assertThat(createCorrespondentRequest.getFullname()).isEqualTo(fullname);
        assertThat(createCorrespondentRequest.getAddress().getPostcode()).isEqualTo(postcode);
        assertThat(createCorrespondentRequest.getAddress().getAddress1()).isEqualTo(address1);
        assertThat(createCorrespondentRequest.getAddress().getAddress2()).isEqualTo(address2);
        assertThat(createCorrespondentRequest.getAddress().getAddress3()).isEqualTo(address3);
        assertThat(createCorrespondentRequest.getAddress().getCountry()).isEqualTo(country);
        assertThat(createCorrespondentRequest.getTelephone()).isEqualTo(telephone);
        assertThat(createCorrespondentRequest.getEmail()).isEqualTo(email);
        assertThat(createCorrespondentRequest.getReference()).isEqualTo(reference);
    }

    @Test
    public void shouldCallCollaboratorsExecute() {
        UUID caseUUID = UUID.randomUUID();
        String correspondentType = "CORRESPONDENT";
        String fullname = "anyName";
        String postcode = "anyPostcode";
        String address1 = "anyAddress1";
        String address2 = "anyAddress2";
        String address3 = "anyAddress3";
        String country = "anyCountry";
        String telephone = "anyPhone";
        String email = "anyEmail";
        String reference = "anyRef";

        Address address = new Address(postcode, address1, address2, address3, country);
        doNothing().when(correspondentService).createCorrespondent(caseUUID, correspondentType, fullname, address, telephone, email, reference);

        AddressDto addressDto = new AddressDto(postcode,
                address1,
                address2,
                address3,
                country);
        CreateCorrespondentRequest createCorrespondentRequest = new CreateCorrespondentRequest(caseUUID, correspondentType, fullname, addressDto, telephone, email, reference);

        createCorrespondentRequest.execute(hocsCaseContext);

        verify(correspondentService, times(1)).createCorrespondent(eq(caseUUID), eq(correspondentType), eq(fullname), eq(address), eq(telephone), eq(email), eq(reference));

        verifyZeroInteractions(caseDataService);
        verifyZeroInteractions(caseNoteService);
        verifyNoMoreInteractions(correspondentService);
        verifyZeroInteractions(stageService);
        verifyZeroInteractions(topicService);

    }

}