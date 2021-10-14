package uk.gov.digital.ho.hocs.casework.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uk.gov.digital.ho.hocs.casework.api.dto.ActionDataDto;
import uk.gov.digital.ho.hocs.casework.domain.model.ActionDataAppeal;
import uk.gov.digital.ho.hocs.casework.domain.repository.ActionDataAppealsRepository;

import java.util.UUID;

@Service
@Slf4j
public class ActionDataAppealsService implements ActionService {

    private final ActionDataAppealsRepository appealsRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public ActionDataAppealsService(ActionDataAppealsRepository appealsRepository, ObjectMapper objectMapper) {
        this.appealsRepository = appealsRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public String getActionName() {
        return "APPEAL";
    }

    @Override
    public void create(UUID caseUuid, UUID stageUuid, String caseType, ActionDataDto actionData) {
    }

    @Override
    public void update(UUID caseUuid, UUID stageUuid, String caseType, UUID actionDataUuid, ActionDataDto actionData) {

        ActionDataAppeal currentAppealData = appealsRepository.findByCaseDataUuid(caseUuid);
    }
}
