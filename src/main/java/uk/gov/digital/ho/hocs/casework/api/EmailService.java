package uk.gov.digital.ho.hocs.casework.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.client.notifyclient.NotifyClient;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;
import uk.gov.digital.ho.hocs.casework.domain.model.StageWithCaseData;

import java.util.UUID;

@Slf4j
@Service
public class EmailService {

    private final NotifyClient notifyClient;
    private final CaseDataService caseDataService;

    @Autowired
    public EmailService(NotifyClient notifyClient,
                        CaseDataService caseDataService) {
        this.notifyClient = notifyClient;
        this.caseDataService = caseDataService;
    }

    public void sendTeamEmail(UUID caseUUID, UUID stageUUID, UUID newTeamUUID, String emailType) {
        CaseData caseData = caseDataService.getCaseInternal(caseUUID);
        notifyClient.sendTeamEmail(caseUUID, stageUUID, newTeamUUID, caseData.getReference(), emailType);
    }

    public void sendUserEmail(UUID caseUUID, UUID stageUUID, UUID currentUserUUID, UUID newUserUUID) {
        CaseData caseData = caseDataService.getCaseInternal(caseUUID);
        notifyClient.sendUserEmail(caseUUID, stageUUID, currentUserUUID, newUserUUID, caseData.getReference());
    }

    /*
    TECH DEBT: This is in the wrong place.
    This method is checking specific stages and checking for values put in the data by a workflow
    and should be performed in bpmnService in hocs-workflow as an output of those stages.
     */
    public void sendOfflineQAEmail(UUID caseUUID, UUID stageUUID, String stageType, UUID stageUserUUID) {
        if (stageType.equals(StageWithCaseData.DCU_DTEN_INITIAL_DRAFT) ||
                stageType.equals(StageWithCaseData.DCU_TRO_INITIAL_DRAFT) ||
                stageType.equals(StageWithCaseData.DCU_MIN_INITIAL_DRAFT)) {

            if(stageUserUUID != null) {
                CaseData caseData = caseDataService.getCaseInternal(caseUUID);
                final String offlineQaUser = caseData.getData(StageWithCaseData.OFFLINE_QA_USER);
                if (offlineQaUser != null) {
                    UUID offlineQaUserUUID = UUID.fromString(offlineQaUser);
                    notifyClient.sendOfflineQaEmail(caseUUID, stageUUID, stageUserUUID, offlineQaUserUUID, caseData.getReference());
                }
            }
        }
    }

}
