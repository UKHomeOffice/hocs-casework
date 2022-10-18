package uk.gov.digital.ho.hocs.casework.migration.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.api.CorrespondentService;
import uk.gov.digital.ho.hocs.casework.client.auditclient.AuditClient;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;
import uk.gov.digital.ho.hocs.casework.domain.model.Address;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;
import uk.gov.digital.ho.hocs.casework.domain.model.Correspondent;
import uk.gov.digital.ho.hocs.casework.domain.model.Stage;
import uk.gov.digital.ho.hocs.casework.domain.repository.CorrespondentRepository;
import uk.gov.digital.ho.hocs.casework.migration.api.dto.ComplaintCorrespondent;
import uk.gov.digital.ho.hocs.casework.migration.api.dto.MigrationComplaintCorrespondent;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.CORRESPONDENT_CREATED;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.CORRESPONDENT_CREATE_FAILURE;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.EVENT;

@Service
@Slf4j
public class MigrationCaseService {

    protected final MigrationCaseDataService migrationCaseDataService;

    protected final MigrationStageService migrationStageService;

    private final CorrespondentService correspondentService;

    private final CorrespondentRepository correspondentRepository;


    private final AuditClient auditClient;



    public MigrationCaseService(MigrationCaseDataService migrationCaseDataService,
                                MigrationStageService migrationStageService,
                                CorrespondentService correspondentService,
                                CorrespondentRepository correspondentRepository,
                                AuditClient auditClient) {
        this.migrationCaseDataService = migrationCaseDataService;
        this.migrationStageService = migrationStageService;
        this.correspondentService = correspondentService;
        this.correspondentRepository = correspondentRepository;
        this.auditClient = auditClient;
    }

    CaseData createMigrationCase(String caseType, String stageType, Map<String, String> data, LocalDate dateReceived,
                                 MigrationComplaintCorrespondent primaryCorrespondent) {
        log.debug("Migrating Case of type: {}", caseType);

        CaseData caseData = migrationCaseDataService.createCompletedCase(caseType, data, dateReceived);
        Stage stage = migrationStageService.createStageForClosedCase(caseData.getUuid(), stageType);
        createPrimaryCorrespondent(primaryCorrespondent, caseData.getUuid(), stage.getUuid());

        return caseData;
    }

    void createPrimaryCorrespondent(MigrationComplaintCorrespondent primaryCorrespondent, UUID caseUUID, UUID stageUUID) {
        log.debug("Creating Correspondent of Type: {} for Migrated Case: {}", primaryCorrespondent.getCorrespondentType(), caseUUID);
        Correspondent correspondent = new Correspondent(caseUUID,
            primaryCorrespondent.getCorrespondentType().toString(),
            primaryCorrespondent.getFullName(),
            primaryCorrespondent.getOrganisation(),
            new Address(primaryCorrespondent.getPostcode(), primaryCorrespondent.getAddress1(),
                primaryCorrespondent.getAddress2(), primaryCorrespondent.getAddress3(),
                primaryCorrespondent.getCountry()
            ),
            primaryCorrespondent.getTelephone(),
            primaryCorrespondent.getEmail(),
            primaryCorrespondent.getReference(),
            primaryCorrespondent.getReference());

        try {
            correspondentRepository.save(correspondent);
            auditClient.createCorrespondentAudit(correspondent);

            Set<Correspondent> caseCorrespondents = correspondentRepository.findAllByCaseUUID(caseUUID);

            if (caseCorrespondents.size()==1) {
                migrationCaseDataService.updatePrimaryCorrespondent(caseUUID,
                    stageUUID,
                    caseCorrespondents.stream().findFirst().get().getCaseUUID());
            }

        } catch(DataIntegrityViolationException e) {
            throw new ApplicationExceptions.EntityCreationException(
                String.format("Failed to create correspondent %s for Migrated Case: %s", correspondent.getUuid(), caseUUID),
                CORRESPONDENT_CREATE_FAILURE, e);
        }
        log.info("Created Correspondent: {} for Migrated Case: {}", correspondent.getUuid(), caseUUID,
            value(EVENT, CORRESPONDENT_CREATED));
        }
}
