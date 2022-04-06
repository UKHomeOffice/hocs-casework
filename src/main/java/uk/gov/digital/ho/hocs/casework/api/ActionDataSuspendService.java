package uk.gov.digital.ho.hocs.casework.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.api.dto.ActionDataDto;
import uk.gov.digital.ho.hocs.casework.api.dto.ActionDataSuspendDto;
import uk.gov.digital.ho.hocs.casework.client.auditclient.AuditClient;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.domain.repository.CaseDataRepository;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class ActionDataSuspendService implements ActionService {

    private final CaseDataRepository caseDataRepository;
    private final InfoClient infoClient;
    private final AuditClient auditClient;

    @Autowired
    public ActionDataSuspendService(CaseDataRepository caseDataRepository, InfoClient infoClient, AuditClient auditClient) {
        this.caseDataRepository = caseDataRepository;
        this.infoClient = infoClient;
        this.auditClient = auditClient;
    }

    @Override
    public String getServiceMapKey() {
        return null;
    }

    @Override
    public List<ActionDataDto> getAllActionsForCase(UUID caseUUID) {
        return null;
    }

    public void suspend(UUID uuid, UUID existingStageUuid, ActionDataSuspendDto suspendDto) {

    }
}
