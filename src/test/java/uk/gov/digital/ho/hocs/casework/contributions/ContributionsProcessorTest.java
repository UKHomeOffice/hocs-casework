package uk.gov.digital.ho.hocs.casework.contributions;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.domain.model.workstacks.ActiveStage;
import uk.gov.digital.ho.hocs.casework.domain.model.workstacks.CaseData;
import uk.gov.digital.ho.hocs.casework.domain.model.workstacks.SomuItem;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@SpringBootTest
@RunWith(SpringRunner.class)
@ActiveProfiles("local")
public class ContributionsProcessorTest {

    private final UUID caseUuid = UUID.randomUUID();

    private final UUID somuUuid = UUID.randomUUID();

    private final UUID somuTypeUuid = UUID.randomUUID();

    private final UUID teamUuid = UUID.randomUUID();

    private final UUID userUuid = UUID.randomUUID();

    private final UUID transitionNoteUuid = UUID.randomUUID();

    @Autowired
    private ObjectMapper objectMapper;

    @Mock
    private InfoClient infoClient;

    private ContributionsProcessorImpl contributionsProcessor;

    private UUID caseUUID = UUID.randomUUID();

    private LocalDateTime caseCreated = LocalDateTime.of(2022, 11, 1, 0, 0);

    private LocalDate dateReveived = LocalDate.of(2022, 11, 1);

    @Before
    public void before() {
        contributionsProcessor = spy(new ContributionsProcessorImpl(objectMapper, infoClient));
    }

    @Test
    public void shouldNotReturnDataForNonContribution() {
        CaseData caseData = mock(CaseData.class);

        when(caseData.getType()).thenReturn("COMP");
        when(caseData.getSomu_items()).thenReturn(Collections.emptySet());

        contributionsProcessor.processContributionsForCase(caseData);

        verify(contributionsProcessor).processContributionsForCase(caseData);
        verifyNoMoreInteractions(contributionsProcessor);
    }

    @Test
    public void shouldReturnDataWithValidDueContribution() {

        SomuItem somuItem = new SomuItem(somuUuid, caseUuid, somuTypeUuid,
            "{ \"contributionDueDate\" : \"9999-12-31\"}");

        CaseData caseData = new CaseData(caseUUID, caseCreated, "COMP", "COMP/123456/22", false, Map.of(), null, null,
            null, null, Collections.emptySet(), null, null, dateReveived, false, null, Collections.emptySet(),
            Set.of(somuItem));

        ActiveStage activeStage = new ActiveStage(UUID.randomUUID(), LocalDateTime.now(), "COMP_SERVICE_TRIAGE", null,
            null, transitionNoteUuid, caseUuid, teamUuid, userUuid, caseData, null, null, null);

        caseData.setActiveStages(Set.of(activeStage));

        when(infoClient.getStageContributions(activeStage.getStageType())).thenReturn(true);

        contributionsProcessor.processContributionsForCase(caseData);

        verify(contributionsProcessor).processContributionsForCase(caseData);
        verify(contributionsProcessor).calculateDueContributionDate(any());
        verify(contributionsProcessor).highestContributionStatus(any());
        verify(contributionsProcessor).highestContributionStatus(any(), any());
        verifyNoMoreInteractions(contributionsProcessor);

        assertEquals(activeStage.getDueContribution(), "9999-12-31");
        assertEquals(activeStage.getContributions(),
            Contribution.ContributionStatus.CONTRIBUTION_DUE.getDisplayedStatus());
    }

    @Test
    public void shouldReturnDataWithValidOverdueContribution() {

        SomuItem somuItem = new SomuItem(somuUuid, caseUuid, somuTypeUuid,
            "{ \"contributionDueDate\" : \"0000-12-31\"}");

        CaseData caseData = new CaseData(caseUUID, caseCreated, "COMP", "COMP/123456/22", false, Map.of(), null, null,
            null, null, Collections.emptySet(), null, null, dateReveived, false, null, Collections.emptySet(),
            Set.of(somuItem));

        ActiveStage activeStage = new ActiveStage(UUID.randomUUID(), LocalDateTime.now(), "COMP_SERVICE_TRIAGE", null,
            null, transitionNoteUuid, caseUuid, teamUuid, userUuid, caseData, null, null, null);

        caseData.setActiveStages(Set.of(activeStage));

        when(infoClient.getStageContributions(activeStage.getStageType())).thenReturn(true);

        contributionsProcessor.processContributionsForCase(caseData);

        verify(contributionsProcessor).processContributionsForCase(caseData);
        verify(contributionsProcessor).calculateDueContributionDate(any());
        verify(contributionsProcessor).highestContributionStatus(any());
        verify(contributionsProcessor).highestContributionStatus(any(), any());
        verifyNoMoreInteractions(contributionsProcessor);

        assertEquals(activeStage.getDueContribution(), "0000-12-31");
        assertEquals(activeStage.getContributions(),
            Contribution.ContributionStatus.CONTRIBUTION_OVERDUE.getDisplayedStatus());
    }

    @Test
    public void shouldReturnDataWithValidReceivedContribution() {

        SomuItem somuItem = new SomuItem(somuUuid, caseUuid, somuTypeUuid,
            "{ \"contributionDueDate\" : \"9999-12-31\", \"contributionStatus\": \"contributionReceived\"}");

        CaseData caseData = new CaseData(caseUUID, caseCreated, "COMP", "COMP/123456/22", false, Map.of(), null, null,
            null, null, Collections.emptySet(), null, null, dateReveived, false, null, Collections.emptySet(),
            Set.of(somuItem));

        ActiveStage activeStage = new ActiveStage(UUID.randomUUID(), LocalDateTime.now(), "COMP_SERVICE_TRIAGE", null,
            null, transitionNoteUuid, caseUuid, teamUuid, userUuid, caseData, null, null, null);

        caseData.setActiveStages(Set.of(activeStage));

        when(infoClient.getStageContributions(activeStage.getStageType())).thenReturn(true);

        contributionsProcessor.processContributionsForCase(caseData);

        verify(contributionsProcessor).processContributionsForCase(caseData);
        verify(contributionsProcessor).calculateDueContributionDate(any());
        verify(contributionsProcessor).highestContributionStatus(any());
        verify(contributionsProcessor).highestContributionStatus(any(), any());
        verifyNoMoreInteractions(contributionsProcessor);

        assertNull(activeStage.getDueContribution());
        assertEquals(activeStage.getContributions(),
            Contribution.ContributionStatus.CONTRIBUTION_RECEIVED.getDisplayedStatus());
    }

    @Test
    public void shouldReturnDataWithValidCancelledContribution() {

        SomuItem somuItem = new SomuItem(somuUuid, caseUuid, somuTypeUuid,
            "{ \"contributionDueDate\" : \"9999-12-31\", \"contributionStatus\": \"contributionCancelled\"}");

        CaseData caseData = new CaseData(caseUUID, caseCreated, "COMP", "COMP/123456/22", false, Map.of(), null, null,
            null, null, Collections.emptySet(), null, null, dateReveived, false, null, Collections.emptySet(),
            Set.of(somuItem));

        ActiveStage activeStage = new ActiveStage(UUID.randomUUID(), LocalDateTime.now(), "COMP_SERVICE_TRIAGE", null,
            null, transitionNoteUuid, caseUuid, teamUuid, userUuid, caseData, null, null, null);

        caseData.setActiveStages(Set.of(activeStage));

        when(infoClient.getStageContributions(activeStage.getStageType())).thenReturn(true);

        contributionsProcessor.processContributionsForCase(caseData);

        verify(contributionsProcessor).processContributionsForCase(caseData);
        verify(contributionsProcessor).calculateDueContributionDate(any());
        verify(contributionsProcessor).highestContributionStatus(any());
        verify(contributionsProcessor).highestContributionStatus(any(), any());
        verifyNoMoreInteractions(contributionsProcessor);

        assertNull(activeStage.getDueContribution());
        assertEquals(activeStage.getContributions(),
            Contribution.ContributionStatus.CONTRIBUTION_CANCELLED.getDisplayedStatus());
    }

    @Test
    public void shouldCalculateDueContribution() {
        Contribution contribution1 = new Contribution(LocalDate.parse("2020-10-10"), null);
        Contribution contribution2 = new Contribution(LocalDate.parse("2020-09-10"), null);

        LocalDate dueContributionDate = contributionsProcessor.calculateDueContributionDate(
            Set.of(contribution1, contribution2)).orElseThrow();
        assertEquals("2020-09-10", dueContributionDate.toString());
    }

    @Test
    public void shouldCalculateDueContributionWithStatus() {
        Contribution contribution1 = new Contribution(LocalDate.parse("2020-10-10"), null);
        Contribution contribution2 = new Contribution(LocalDate.parse("2020-09-10"), "contributionReceived");

        LocalDate dueContributionDate = contributionsProcessor.calculateDueContributionDate(
            Set.of(contribution1, contribution2)).orElseThrow();
        assertEquals("2020-10-10", dueContributionDate.toString());
    }

    @Test
    public void shouldNotCalculateDueContributionWithStatus() {
        Contribution contribution1 = new Contribution(LocalDate.parse("2020-10-10"), "contributionReceived");
        Contribution contribution2 = new Contribution(LocalDate.parse("2020-09-10"), "contributionReceived");

        assertTrue(contributionsProcessor.calculateDueContributionDate(Set.of(contribution1, contribution2)).isEmpty());
    }

    @Test
    public void shouldReturnCancelledBeforeReceived() {
        Contribution contribution1 = new Contribution(LocalDate.parse("2020-10-10"), "contributionReceived");
        Contribution contribution2 = new Contribution(LocalDate.parse("2020-09-10"), "contributionCancelled");

        Contribution.ContributionStatus contributionStatus = contributionsProcessor.highestContributionStatus(
            Set.of(contribution1, contribution2), LocalDate.of(2020, 10, 10)).orElse(null);
        assertEquals(Contribution.ContributionStatus.CONTRIBUTION_CANCELLED, contributionStatus);
    }

    @Test
    public void shouldReturnDueBeforeCancelled() {
        Contribution contribution1 = new Contribution(LocalDate.parse("2020-10-11"), "");
        Contribution contribution2 = new Contribution(LocalDate.parse("2020-09-10"), "contributionCancelled");

        Contribution.ContributionStatus contributionStatus = contributionsProcessor.highestContributionStatus(
            Set.of(contribution1, contribution2), LocalDate.of(2020, 10, 10)).orElse(null);
        assertEquals(Contribution.ContributionStatus.CONTRIBUTION_DUE, contributionStatus);
    }

    @Test
    public void shouldReturnOverdueBeforeDue() {
        Contribution contribution1 = new Contribution(LocalDate.parse("2020-10-10"), "");
        Contribution contribution2 = new Contribution(LocalDate.parse("2020-09-10"), null);

        Contribution.ContributionStatus contributionStatus = contributionsProcessor.highestContributionStatus(
            Set.of(contribution1, contribution2), LocalDate.of(2020, 10, 10)).orElse(null);
        assertEquals(Contribution.ContributionStatus.CONTRIBUTION_OVERDUE, contributionStatus);
    }

    @Test
    public void shouldReturnDueWithOtherCompleted() {
        Contribution contribution1 = new Contribution(LocalDate.parse("2020-10-10"), "contributionReceived");
        Contribution contribution2 = new Contribution(LocalDate.parse("2020-09-10"), "contributionCancelled");
        Contribution contribution3 = new Contribution(LocalDate.parse("2020-11-10"), null);

        Contribution.ContributionStatus contributionStatus = contributionsProcessor.highestContributionStatus(
            Set.of(contribution1, contribution2, contribution3), LocalDate.of(2020, 10, 10)).orElse(null);
        assertEquals(Contribution.ContributionStatus.CONTRIBUTION_DUE, contributionStatus);
    }

    @Test
    public void shouldReturnDueForDueFutureDueDate() {
        Contribution contribution1 = new Contribution(LocalDate.parse("2020-11-10"), null);

        Contribution.ContributionStatus contributionStatus = contributionsProcessor.highestContributionStatus(
            Set.of(contribution1), LocalDate.of(2020, 10, 10)).orElse(null);
        assertEquals(Contribution.ContributionStatus.CONTRIBUTION_DUE, contributionStatus);
    }

}
