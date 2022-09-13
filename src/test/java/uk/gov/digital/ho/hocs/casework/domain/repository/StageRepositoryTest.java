package uk.gov.digital.ho.hocs.casework.domain.repository;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.digital.ho.hocs.casework.api.dto.CaseDataType;
import uk.gov.digital.ho.hocs.casework.api.utils.CaseDataTypeFactory;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;
import uk.gov.digital.ho.hocs.casework.domain.model.Stage;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.SqlConfig.TransactionMode.ISOLATED;

@RunWith(SpringRunner.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = "classpath:stage/afterTest.sql",
     config = @SqlConfig(transactionMode = ISOLATED),
     executionPhase = AFTER_TEST_METHOD)
@ActiveProfiles("test")
public class StageRepositoryTest {

    @Autowired
    private StageRepository stageRepository;

    @Autowired
    private CaseDataRepository caseDataRepository;

    @Test
    public void findActiveBasicStageByCaseUuidStageUUID() {
        Stage stage = createActiveStageWithActiveCase();

        var returnedStage = stageRepository.findActiveBasicStageByCaseUuidStageUUID(stage.getCaseUUID(),
            stage.getUuid());

        assertNotNull(returnedStage);
        assertEquals(returnedStage, stage);
    }

    @Test
    public void findActiveBasicStageByCaseUuidStageUUID_InactiveStage() {
        Stage stage = createInactiveStageWithActiveCase();

        var returnedStage = stageRepository.findActiveBasicStageByCaseUuidStageUUID(stage.getCaseUUID(),
            stage.getUuid());

        assertNull(returnedStage);
    }

    @Test
    public void findActiveBasicStageByCaseUuidStageUUID_DeletedCase() {
        Stage stage = createActiveStageWithDeletedCase();

        var returnedStage = stageRepository.findActiveBasicStageByCaseUuidStageUUID(stage.getCaseUUID(),
            stage.getUuid());

        assertNull(returnedStage);
    }

    @Test
    public void findBasicStageByCaseUuidStageUUID() {
        Stage stage = createActiveStageWithActiveCase();

        var returnedStage = stageRepository.findBasicStageByCaseUuidAndStageUuid(stage.getCaseUUID(), stage.getUuid());

        assertNotNull(returnedStage);
        assertEquals(returnedStage, stage);
    }

    @Test
    public void findBasicStageByCaseUuidStageUUID_InactiveStage() {
        Stage stage = createInactiveStageWithActiveCase();

        var returnedStage = stageRepository.findBasicStageByCaseUuidAndStageUuid(stage.getCaseUUID(), stage.getUuid());

        assertNotNull(returnedStage);
        assertEquals(returnedStage, stage);
    }

    @Test
    public void findBasicStageByCaseUuidStageUUID_DeletedCase() {
        Stage stage = createActiveStageWithDeletedCase();

        var returnedStage = stageRepository.findBasicStageByCaseUuidAndStageUuid(stage.getCaseUUID(), stage.getUuid());

        assertNotNull(returnedStage);
        assertEquals(returnedStage, stage);
    }

    @Test
    public void findActiveByCaseUuidStageUUID() {
        Stage stage = createActiveStageWithActiveCase();

        var returnedStage = stageRepository.findActiveByCaseUuidStageUUID(stage.getCaseUUID(), stage.getUuid());

        assertNotNull(returnedStage);
        assertEquals(stage.getUuid(), returnedStage.getUuid());
    }

    @Test
    public void findActiveByCaseUuidStageUUID_InactiveStage() {
        Stage stage = createInactiveStageWithActiveCase();

        var returnedStage = stageRepository.findActiveByCaseUuidStageUUID(stage.getCaseUUID(), stage.getUuid());

        assertNull(returnedStage);
    }

    @Test
    public void findActiveByCaseUuidStageUUID_DeletedCase() {
        Stage stage = createActiveStageWithDeletedCase();

        var returnedStage = stageRepository.findActiveByCaseUuidStageUUID(stage.getCaseUUID(), stage.getUuid());

        assertNull(returnedStage);
    }

    @Test
    public void findByCaseUuidStageUUID() {
        Stage stage = createActiveStageWithActiveCase();

        var returnedStage = stageRepository.findByCaseUuidStageUUID(stage.getCaseUUID(), stage.getUuid());

        assertNotNull(returnedStage);
        assertEquals(stage.getUuid(), returnedStage.getUuid());
    }

    @Test
    public void findByCaseUuidStageUUID_InactiveStage() {
        Stage stage = createInactiveStageWithActiveCase();

        var returnedStage = stageRepository.findByCaseUuidStageUUID(stage.getCaseUUID(), stage.getUuid());

        assertNotNull(returnedStage);
        assertEquals(stage.getUuid(), returnedStage.getUuid());
    }

    @Test
    public void findByCaseUuidStageUUID_DeletedCase() {
        Stage stage = createActiveStageWithDeletedCase();

        var returnedStage = stageRepository.findByCaseUuidStageUUID(stage.getCaseUUID(), stage.getUuid());

        assertNull(returnedStage);
    }

    @Test
    public void findAllActiveByCaseUUID() {
        Stage stage = createActiveStageWithActiveCase();

        var returnedStage = stageRepository.findAllActiveByCaseUUID(stage.getCaseUUID());

        assertNotNull(returnedStage);
        assertEquals(stage.getUuid(), returnedStage.stream().findFirst().get().getUuid());
    }

    @Test
    public void findAllActiveByCaseUUID_InactiveStage() {
        Stage stage = createInactiveStageWithActiveCase();

        var returnedStages = stageRepository.findAllActiveByCaseUUID(stage.getCaseUUID());

        assertEquals(0, returnedStages.size());
    }

    @Test
    public void findAllActiveByCaseUUID_DeletedCase() {
        Stage stage = createActiveStageWithDeletedCase();

        var returnedStages = stageRepository.findAllActiveByCaseUUID(stage.getCaseUUID());

        assertEquals(0, returnedStages.size());
    }

    @Test
    public void findAllByCaseUUID() {
        Stage stage = createActiveStageWithActiveCase();

        var returnedStage = stageRepository.findAllByCaseUUID(stage.getCaseUUID());

        assertNotNull(returnedStage);
        assertEquals(stage.getUuid(), returnedStage.stream().findFirst().get().getUuid());
    }

    @Test
    public void findAllByCaseUUID_InactiveStage() {
        Stage stage = createInactiveStageWithActiveCase();

        var returnedStage = stageRepository.findAllByCaseUUID(stage.getCaseUUID());

        assertNotNull(returnedStage);
        assertEquals(stage.getUuid(), returnedStage.stream().findFirst().get().getUuid());
    }

    @Test
    public void findAllByCaseUUID_DeletedCase() {
        Stage stage = createActiveStageWithDeletedCase();

        var returnedStages = stageRepository.findAllByCaseUUID(stage.getCaseUUID());

        assertEquals(0, returnedStages.size());
    }

    @Test
    public void findAllByCaseUUIDIn() {
        Stage stage = createActiveStageWithActiveCase();

        var returnedStages = stageRepository.findAllByCaseUUIDIn(Set.of(stage.getCaseUUID()));

        assertEquals(1, returnedStages.size());
        assertEquals(stage.getUuid(), returnedStages.stream().findFirst().get().getUuid());
    }

    @Test
    public void findAllByCaseUUIDIn_InactiveStage() {
        Stage stage = createInactiveStageWithActiveCase();

        var returnedStages = stageRepository.findAllByCaseUUIDIn(Set.of(stage.getCaseUUID()));

        assertEquals(1, returnedStages.size());
        assertEquals(stage.getUuid(), returnedStages.stream().findFirst().get().getUuid());
    }

    @Test
    public void findAllByCaseUUIDIn_DeletedCase() {
        Stage stage = createActiveStageWithDeletedCase();

        var returnedStages = stageRepository.findAllByCaseUUIDIn(Set.of(stage.getCaseUUID()));

        assertEquals(0, returnedStages.size());
    }

    @Test
    public void findAllByCaseUUIDIn_EmptyCase() {
        createActiveStageWithDeletedCase();

        var returnedStages = stageRepository.findAllByCaseUUIDIn(Set.of());

        assertEquals(0, returnedStages.size());
    }

    @Test
    public void findAllActiveByTeamUUID() {
        Stage stage = createActiveStageWithActiveCase();

        var returnedStages = stageRepository.findAllActiveByTeamUUID(stage.getTeamUUID());

        assertEquals(1, returnedStages.size());
        assertEquals(stage.getUuid(), returnedStages.stream().findFirst().get().getUuid());
    }

    @Test
    public void findAllActiveByTeamUUID_InactiveStage() {
        createInactiveStageWithActiveCase();

        var returnedStages = stageRepository.findAllActiveByTeamUUID(UUID.randomUUID());

        assertEquals(0, returnedStages.size());
    }

    @Test
    public void findAllActiveByTeamUUID_DeletedCase() {
        Stage stage = createActiveStageWithDeletedCase();

        var returnedStages = stageRepository.findAllActiveByTeamUUID(stage.getTeamUUID());

        assertEquals(0, returnedStages.size());
    }

    @Test
    public void findAllUnassignedAndActiveByTeamUUID() {
        Stage stage = createActiveStageWithActiveCase();

        var returnedStages = stageRepository.findAllUnassignedAndActiveByTeamUUID(stage.getTeamUUID());

        assertEquals(1, returnedStages.size());
        assertEquals(stage.getUuid(), returnedStages.stream().findFirst().get().getUuid());
    }

    @Test
    public void findAllUnassignedAndActiveByTeamUUID_InactiveStage() {
        createInactiveStageWithActiveCase();

        var returnedStages = stageRepository.findAllUnassignedAndActiveByTeamUUID(UUID.randomUUID());

        assertEquals(0, returnedStages.size());
    }

    @Test
    public void findAllUnassignedAndActiveByTeamUUID_DeletedCase() {
        Stage stage = createActiveStageWithDeletedCase();

        var returnedStages = stageRepository.findAllUnassignedAndActiveByTeamUUID(stage.getTeamUUID());

        assertEquals(0, returnedStages.size());
    }

    @Test
    public void findAllUnassignedAndActiveByTeamUUID_AssignedCase() {
        Stage stage = createActiveStageWithDeletedCase();
        stage.setUserUUID(UUID.randomUUID());
        stageRepository.save(stage);

        var returnedStages = stageRepository.findAllUnassignedAndActiveByTeamUUID(stage.getTeamUUID());

        assertEquals(0, returnedStages.size());
    }

    @Test
    public void findStageCaseUUIDsByUserUUIDTeamUUID() {
        Stage stage = createActiveStageWithActiveCase();
        stage.setUserUUID(UUID.randomUUID());
        stageRepository.save(stage);

        var returnedStages = stageRepository.findStageCaseUUIDsByUserUUIDTeamUUID(stage.getUserUUID(),
            stage.getTeamUUID());

        assertEquals(1, returnedStages.size());
        assertEquals(stage.getUuid(), returnedStages.stream().findFirst().get().getUuid());
    }

    @Test
    public void findStageCaseUUIDsByUserUUIDTeamUUID_InactiveStage() {
        createInactiveStageWithActiveCase();

        var returnedStages = stageRepository.findStageCaseUUIDsByUserUUIDTeamUUID(UUID.randomUUID(), UUID.randomUUID());

        assertEquals(0, returnedStages.size());
    }

    @Test
    public void findStageCaseUUIDsByUserUUIDTeamUUID_DeletedCase() {
        Stage stage = createActiveStageWithDeletedCase();
        stage.setUserUUID(UUID.randomUUID());
        stageRepository.save(stage);

        var returnedStages = stageRepository.findStageCaseUUIDsByUserUUIDTeamUUID(stage.getUserUUID(),
            stage.getTeamUUID());

        assertEquals(0, returnedStages.size());
    }

    @Test
    public void findByCaseReference() {
        Stage stage = createActiveStageWithActiveCase();
        CaseData caseData = caseDataRepository.findActiveByUuid(stage.getCaseUUID());

        var returnedStages = stageRepository.findByCaseReference(caseData.getReference());

        assertEquals(1, returnedStages.size());
        assertEquals(returnedStages.stream().findFirst().get().getUuid(), stage.getUuid());
    }

    @Test
    public void findByCaseReference_InactiveStage() {
        Stage stage = createInactiveStageWithActiveCase();
        CaseData caseData = caseDataRepository.findActiveByUuid(stage.getCaseUUID());

        var returnedStages = stageRepository.findByCaseReference(caseData.getReference());

        assertEquals(1, returnedStages.size());
        assertEquals(returnedStages.stream().findFirst().get().getUuid(), stage.getUuid());

    }

    @Test
    public void findByCaseReference_DeletedCase() {
        Stage stage = createActiveStageWithDeletedCase();
        CaseData caseData = caseDataRepository.findAnyByUuid(stage.getCaseUUID());

        var returnedStages = stageRepository.findByCaseReference(caseData.getReference());

        assertEquals(0, returnedStages.size());
    }

    @Test
    public void findByCaseReference_EmptyReference() {
        var returnedStages = stageRepository.findByCaseReference("");

        assertEquals(0, returnedStages.size());
    }

    @Test
    public void findByCaseReference_WrongReference() {
        var returnedStages = stageRepository.findByCaseReference(UUID.randomUUID().toString());

        assertEquals(0, returnedStages.size());
    }

    private Stage createActiveStageWithActiveCase() {
        CaseDataType type = CaseDataTypeFactory.from("MIN", "a1");
        Long caseNumber = 1234L;
        Map<String, String> data = new HashMap<>();
        LocalDate caseReceived = LocalDate.now();
        CaseData caseData = new CaseData(type, caseNumber, data, caseReceived);
        caseData.setCaseDeadline(LocalDate.now().plusDays(2));

        String stageType = "TEST";
        UUID teamUUID = UUID.randomUUID();
        var stage = new Stage(caseData.getUuid(), stageType, teamUUID, null, null);
        caseDataRepository.save(caseData);
        return stageRepository.save(stage);
    }

    private Stage createInactiveStageWithActiveCase() {
        CaseDataType type = CaseDataTypeFactory.from("MIN", "a1");
        Long caseNumber = 1234L;
        Map<String, String> data = new HashMap<>();
        LocalDate caseReceived = LocalDate.now();
        CaseData caseData = new CaseData(type, caseNumber, data, caseReceived);
        caseData.setCaseDeadline(LocalDate.now().plusDays(2));

        String stageType = "TEST";
        var stage = new Stage(caseData.getUuid(), stageType, null, null, null);
        caseDataRepository.save(caseData);
        return stageRepository.save(stage);
    }

    private Stage createActiveStageWithDeletedCase() {
        CaseDataType type = CaseDataTypeFactory.from("MIN", "a1");
        Long caseNumber = 1234L;
        Map<String, String> data = new HashMap<>();
        LocalDate caseReceived = LocalDate.now();
        CaseData caseData = new CaseData(type, caseNumber, data, caseReceived);
        caseData.setCaseDeadline(LocalDate.now().plusDays(2));
        caseData.setDeleted(true);

        String stageType = "TEST";
        UUID teamUUID = UUID.randomUUID();
        var stage = new Stage(caseData.getUuid(), stageType, teamUUID, null, null);
        caseDataRepository.save(caseData);
        return stageRepository.save(stage);
    }

}
