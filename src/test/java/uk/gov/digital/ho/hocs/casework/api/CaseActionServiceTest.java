package uk.gov.digital.ho.hocs.casework.api;

import lombok.NoArgsConstructor;
import org.junit.Before;
import org.junit.Test;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.api.dto.ActionDataDto;

import java.util.UUID;

import static org.junit.Assert.*;

public class CaseActionServiceTest {

    private CaseActionService caseActionService;

    @Before
    public void setUp() throws Exception {
        caseActionService = new CaseActionService();
    }

    @Test
    public void createActionDataForCase_throwUnsupportedOperationExceptionWhenUnknownDtoType() {

        // WHEN
        assertThrows(UnsupportedOperationException.class, () -> {
            caseActionService.createActionDataForCase(UUID.randomUUID(), UUID.randomUUID(),"CASE_TYPE", new FakeDto());
        });
    }

    @NoArgsConstructor
    static class FakeDto extends ActionDataDto {}

    @NoArgsConstructor
    static class Fake2Dto extends ActionDataDto {}

    @Service
    static class Fake2ServiceDtoService implements ActionService {

        public String getActionName() {return Fake2Dto.class.getSimpleName();}

        @Override
        public void create(UUID caseUuid, UUID stageUuid, String caseType, ActionDataDto actionData) {}

        @Override
        public void update(UUID caseUuid, UUID stageUuid, String caseType, UUID actionEntityId, ActionDataDto actionData) {}
    }
}