package uk.gov.digital.ho.hocs.casework.api;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.digital.ho.hocs.casework.api.dto.StageTypeDto;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;
import uk.gov.digital.ho.hocs.casework.domain.model.Stage;
import uk.gov.digital.ho.hocs.casework.domain.repository.CaseDataDetailsGroupsRepository;
import uk.gov.digital.ho.hocs.casework.domain.repository.CaseDataDetailsStagesRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("local")
public class CaseDataDetailsServiceTest {

    @MockBean
    private InfoClient infoClient;

    @MockBean
    private CaseDataService caseDataService;

    @MockBean
    private StageService stageService;

    @Autowired
    private CaseDataDetailsGroupsRepository caseDataDetailsGroupsRepository;

    @Autowired
    private CaseDataDetailsStagesRepository caseDataDetailsStagesRepository;

    private CaseDataDetailsService caseDataDetailsService;

    @Before
    public void setup() {
        caseDataDetailsService = new CaseDataDetailsService(caseDataService, stageService,
            caseDataDetailsGroupsRepository, caseDataDetailsStagesRepository, infoClient);
    }

    private void setupCaseData(String type) {
        CaseData caseData = new CaseData(1L, UUID.randomUUID(), LocalDateTime.now(), type, null, false,
            Map.of("TestField", "TestValue"), UUID.randomUUID(), null, UUID.randomUUID(), null, LocalDate.now(),
            LocalDate.now(), LocalDate.now(), null, Set.of(), Set.of());

        when(caseDataService.getCaseData(any())).thenReturn(caseData);
    }

    @Test
    public void shouldReturnEmptyIfCaseTypeNotFound() {
        setupCaseData("UNKNOWN");

        var result = caseDataDetailsService.getCaseDataDetails(UUID.randomUUID());

        assertThat(result.getFields()).isEmpty();
    }

    @Test
    public void shouldReturnFromGroupsForCaseType() {
        setupCaseData("TEST1");

        var result = caseDataDetailsService.getCaseDataDetails(UUID.randomUUID());

        var groups = result.getFields();

        assertThat(groups).hasSize(1).containsOnlyKeys("Group 1");
    }

    @Test
    public void shouldReturnFromStagesForCaseType() {
        setupCaseData("TEST2");
        when(stageService.getAllStagesByCaseUUID(any())).thenReturn(
            Set.of(new Stage(UUID.randomUUID(), "TEST_STAGE2", null, null, null)));
        when(infoClient.getAllStagesForCaseType("TEST2")).thenReturn(
            Set.of(new StageTypeDto("Test Stage 1", "1", "TEST_STAGE", 0, 0, 0),
                new StageTypeDto("Test Stage 2", "0", "TEST_STAGE2", 0, 0, 1)));

        var result = caseDataDetailsService.getCaseDataDetails(UUID.randomUUID());

        var groups = result.getFields();

        assertThat(groups).hasSize(1).containsOnlyKeys("Test Stage 2");
    }

}
