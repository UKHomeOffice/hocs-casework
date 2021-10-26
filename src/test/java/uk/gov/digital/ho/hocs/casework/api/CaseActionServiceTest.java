package uk.gov.digital.ho.hocs.casework.api;

import lombok.NoArgsConstructor;
import org.junit.Before;
import org.junit.Test;
import org.mockito.*;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.api.dto.ActionDataDto;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.domain.repository.CaseDataRepository;

import java.util.List;
import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class CaseActionServiceTest {

    private CaseActionService caseActionService;

    @Mock
    private CaseDataRepository caseDataRepository;

    @Mock
    private InfoClient infoClient;

    @Mock
    private Fake2ServiceDtoService fake2ServiceDtoService;

    @Captor
    private ArgumentCaptor<String> serviceTypeStringCaptor = ArgumentCaptor.forClass(String.class);

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        when(fake2ServiceDtoService.getServiceDtoTypeKey()).thenReturn(Fake2Dto.class.getSimpleName());
        caseActionService = new CaseActionService(caseDataRepository, infoClient, List.of(fake2ServiceDtoService));
    }

    @Test
    public void createActionDataForCase_throwUnsupportedOperationExceptionWhenUnknownDtoType() {

        // WHEN
        assertThrows(UnsupportedOperationException.class, () -> {
            caseActionService.createActionDataForCase(UUID.randomUUID(), UUID.randomUUID(), new FakeDto());
        });
    }

    @Test
    public void createActionDataForCase_shouldInvokeRequiredService() {
        UUID caseUUID = UUID.randomUUID();
        UUID stageUUID = UUID.randomUUID();
        String caseType = "CASE_TYPE";
        ActionDataDto expectedDtoType = new Fake2Dto();

        caseActionService.createActionDataForCase(caseUUID, stageUUID, expectedDtoType);

        verify(fake2ServiceDtoService, times(1)).create(caseUUID, stageUUID, expectedDtoType);
    }

    @Test
    public void updateActionDataForCase_throwUnsupportedOperationExceptionWhenUnknownDtoType() {

        // WHEN
        assertThrows(UnsupportedOperationException.class, () -> {
            caseActionService.updateActionDataForCase(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), new FakeDto());
        });
    }

    @Test
    public void updateActionDataForCase_shouldInvokeRequiredService() {
        UUID caseUUID = UUID.randomUUID();
        UUID stageUUID = UUID.randomUUID();
        UUID actionEntityId = UUID.randomUUID();
        String caseType = "CASE_TYPE";
        ActionDataDto expectedDtoType = new Fake2Dto();

        caseActionService.updateActionDataForCase(caseUUID, stageUUID, actionEntityId, expectedDtoType);

        verify(fake2ServiceDtoService, times(1)).update(caseUUID, stageUUID, actionEntityId, expectedDtoType);
    }

    @NoArgsConstructor
    static class FakeDto extends ActionDataDto {}

    @NoArgsConstructor
    static class Fake2Dto extends ActionDataDto {}

    @Service
    static class Fake2ServiceDtoService implements ActionService {

        public String getServiceDtoTypeKey() {return Fake2Dto.class.getSimpleName();}

        @Override
        public String getServiceMapKey() {
            return "fake";
        }

        @Override
        public void create(UUID caseUuid, UUID stageUuid, ActionDataDto actionData) {}

        @Override
        public void update(UUID caseUuid, UUID stageUuid, UUID actionEntityId, ActionDataDto actionData) {}

        @Override
        public List<ActionDataDto> getAllActionsForCase(UUID caseUUID) {
            return List.of();
        }
    }
}