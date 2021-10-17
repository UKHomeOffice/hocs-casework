package uk.gov.digital.ho.hocs.casework.contributions;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.api.SomuItemService;
import uk.gov.digital.ho.hocs.casework.domain.model.SomuItem;
import uk.gov.digital.ho.hocs.casework.domain.model.Stage;

import java.time.LocalDate;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static org.wildfly.common.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class ContributionsProcessorTest {
    private final UUID caseUUID = UUID.randomUUID();
    private final UUID somuUUID = UUID.randomUUID();
    private final UUID somuTypeUuid = UUID.randomUUID();
    private final UUID teamUUID = UUID.randomUUID();
    private final UUID userUUID = UUID.randomUUID();
    private final UUID transitionNoteUUID = UUID.randomUUID();

    @Mock
    private SomuItemService somuItemService;

    private ContributionsProcessorImpl contributionsProcessor;

    @Before
    public void before() {
        contributionsProcessor = new ContributionsProcessorImpl(somuItemService);
    }

    @Test
    public void shouldReturnWithSomuItemsProvided() {
        var spiedContributionsProcessor = spy(contributionsProcessor);

        Stage stage = spy(new Stage(caseUUID, "ANY", teamUUID, userUUID, transitionNoteUUID));

        when(somuItemService.getCaseItemsByCaseUuids(Set.of(caseUUID))).thenReturn(Collections.emptySet());

        spiedContributionsProcessor.processContributionsForStages(Set.of(stage));

        verify(spiedContributionsProcessor).processContributionsForStages(Set.of(stage));
        verify(somuItemService).getCaseItemsByCaseUuids(Set.of(caseUUID));
        verifyNoMoreInteractions(spiedContributionsProcessor, somuItemService);
    }

    @Test
    public void shouldReturnWithNoContributionsProvided() {
        var spiedContributionsProcessor = spy(contributionsProcessor);

        Stage stage = spy(new Stage(caseUUID, "ANY", teamUUID, userUUID, transitionNoteUUID));
        SomuItem somuItem = new SomuItem(somuUUID, caseUUID, somuTypeUuid, "{ \"TEST\" : \"TEST\"}");

        when(somuItemService.getCaseItemsByCaseUuids(Set.of(caseUUID))).thenReturn(Set.of(somuItem));

        spiedContributionsProcessor.processContributionsForStages(Set.of(stage));

        verify(spiedContributionsProcessor).processContributionsForStages(Set.of(stage));
        verify(somuItemService).getCaseItemsByCaseUuids(Set.of(caseUUID));
        verify(spiedContributionsProcessor).filterContributions(Set.of(somuItem));
        verifyNoMoreInteractions(spiedContributionsProcessor, somuItemService);
    }

    @Test
    public void shouldAddOverdueContributionsForCompCase() {
        String stageType = "COMP";
        Stage stage = spy(new Stage(caseUUID, stageType, teamUUID, userUUID, transitionNoteUUID));
        doReturn("COMP").when(stage).getCaseDataType();

        SomuItem somuItem = new SomuItem(somuUUID, caseUUID, somuTypeUuid, "{ \"contributionDueDate\" : \"2020-10-10\"}");

        when(somuItemService.getCaseItemsByCaseUuids(Set.of(caseUUID))).thenReturn(Set.of(somuItem));

        contributionsProcessor.processContributionsForStages(Set.of(stage));
        assertEquals("2020-10-10", stage.getDueContribution());
        assertEquals("Overdue", stage.getContributions());
    }

    @Test
    public void shouldAddDueContributionsForCompCase() {
        String stageType = "COMP";
        Stage stage = spy(new Stage(caseUUID, stageType, teamUUID, userUUID, transitionNoteUUID));
        doReturn("COMP").when(stage).getCaseDataType();

        SomuItem somuItem = new SomuItem(somuUUID, caseUUID, somuTypeUuid, "{ \"contributionDueDate\" : \"9999-10-10\"}");

        when(somuItemService.getCaseItemsByCaseUuids(Set.of(caseUUID))).thenReturn(Set.of(somuItem));

        contributionsProcessor.processContributionsForStages(Set.of(stage));
        assertEquals("9999-10-10", stage.getDueContribution());
        assertEquals("Due", stage.getContributions());
    }

    @Test
    public void shouldAddOverdueContributionsForMPAMCase() {
        String stageType = "MPAM_TRIAGE";
        Stage stage = spy(new Stage(caseUUID, stageType, teamUUID, userUUID, transitionNoteUUID));
        doReturn("MPAM_TRIAGE").when(stage).getStageType();

        SomuItem somuItem = new SomuItem(somuUUID, caseUUID, somuTypeUuid, "{ \"contributionDueDate\" : \"2020-10-10\"}");

        when(somuItemService.getCaseItemsByCaseUuids(Set.of(caseUUID))).thenReturn(Set.of(somuItem));

        contributionsProcessor.processContributionsForStages(Set.of(stage));
        assertEquals("2020-10-10", stage.getDueContribution());
        assertEquals("Overdue", stage.getContributions());
    }

    @Test
    public void shouldAddDueContributionsForMPAMCase() {
        String stageType = "MPAM_TRIAGE";
        Stage stage = spy(new Stage(caseUUID, stageType, teamUUID, userUUID, transitionNoteUUID));
        doReturn("MPAM_TRIAGE").when(stage).getStageType();

        SomuItem somuItem = new SomuItem(somuUUID, caseUUID, somuTypeUuid, "{ \"contributionDueDate\" : \"9999-10-10\"}");

        when(somuItemService.getCaseItemsByCaseUuids(Set.of(caseUUID))).thenReturn(Set.of(somuItem));

        contributionsProcessor.processContributionsForStages(Set.of(stage));
        assertEquals("9999-10-10", stage.getDueContribution());
        assertEquals("Due", stage.getContributions());
    }

    @Test
    public void shouldAddOverdueContributionsForFOICase() {
        String stageType = "FOI_APPROVAL";
        Stage stage = spy(new Stage(caseUUID, stageType, teamUUID, userUUID, transitionNoteUUID));
        doReturn("FOI_APPROVAL").when(stage).getStageType();

        SomuItem somuItem = new SomuItem(somuUUID, caseUUID, somuTypeUuid, "{ \"contributionDueDate\" : \"2020-10-10\"}");

        when(somuItemService.getCaseItemsByCaseUuids(Set.of(caseUUID))).thenReturn(Set.of(somuItem));

        contributionsProcessor.processContributionsForStages(Set.of(stage));
        assertEquals("2020-10-10", stage.getDueContribution());
        assertEquals("Overdue", stage.getContributions());
    }

    @Test
    public void shouldAddDueContributionsForFOICase() {
        String stageType = "FOI_APPROVAL";
        Stage stage = spy(new Stage(caseUUID, stageType, teamUUID, userUUID, transitionNoteUUID));
        doReturn("FOI_APPROVAL").when(stage).getStageType();

        SomuItem somuItem = new SomuItem(somuUUID, caseUUID, somuTypeUuid, "{ \"contributionDueDate\" : \"9999-10-10\"}");

        when(somuItemService.getCaseItemsByCaseUuids(Set.of(caseUUID))).thenReturn(Set.of(somuItem));

        contributionsProcessor.processContributionsForStages(Set.of(stage));
        assertEquals("9999-10-10", stage.getDueContribution());
        assertEquals("Due", stage.getContributions());
    }

    @Test
    public void shouldNotActionIfStageDoesNotMatchRequired() {
        String stageType = "TEST";
        Stage stage = spy(new Stage(caseUUID, stageType, teamUUID, userUUID, transitionNoteUUID));
        doReturn("TEST").when(stage).getStageType();

        SomuItem somuItem = new SomuItem(somuUUID, caseUUID, somuTypeUuid, "{ \"contributionDueDate\" : \"9999-10-10\"}");

        when(somuItemService.getCaseItemsByCaseUuids(Set.of(caseUUID))).thenReturn(Set.of(somuItem));

        Set<Stage> preTest = Set.of(stage);

        contributionsProcessor.processContributionsForStages(preTest);

        assertEquals(preTest, Set.of(stage));
    }

    @Test
    public void shouldCalculateDueContribution() {
        SomuItem somuItem1 = new SomuItem(somuUUID, caseUUID, somuTypeUuid, "{ \"contributionDueDate\" : \"2020-10-10\"}");
        SomuItem somuItem2 = new SomuItem(somuUUID, caseUUID, somuTypeUuid, "{ \"contributionDueDate\" : \"2020-09-10\"}");
        LocalDate dueContributionDate = contributionsProcessor.calculateDueContributionDate(Set.of(somuItem1, somuItem2)).orElseThrow();
        assertEquals("2020-09-10", dueContributionDate.toString());
    }

    @Test
    public void shouldCalculateDueContributionWithStatus() {
        SomuItem somuItem1 = new SomuItem(somuUUID, caseUUID, somuTypeUuid, "{ \"contributionDueDate\" : \"2020-10-10\"}");
        SomuItem somuItem2 = new SomuItem(somuUUID, caseUUID, somuTypeUuid, "{ \"contributionDueDate\" : \"2020-09-10\", \"contributionStatus\" : \"contributionReceived\"}");
        LocalDate dueContributionDate = contributionsProcessor.calculateDueContributionDate(Set.of(somuItem1, somuItem2)).orElseThrow();
        assertEquals("2020-10-10", dueContributionDate.toString());
    }

    @Test
    public void shouldNotCalculateDueContributionWithStatus() {
        SomuItem somuItem1 = new SomuItem(somuUUID, caseUUID, somuTypeUuid, "{ \"contributionDueDate\" : \"2020-10-10\", \"contributionStatus\" : \"contributionReceived\"}");
        SomuItem somuItem2 = new SomuItem(somuUUID, caseUUID, somuTypeUuid, "{ \"contributionDueDate\" : \"2020-09-10\", \"contributionStatus\" : \"contributionReceived\"}");
        assertTrue(contributionsProcessor.calculateDueContributionDate(Set.of(somuItem1, somuItem2)).isEmpty());
    }

    @Test
    public void shouldReturnCancelledBeforeReceived() {
        SomuItem somuItem1 = new SomuItem(somuUUID, caseUUID, somuTypeUuid, "{ \"contributionDueDate\" : \"2020-10-10\", \"contributionStatus\" : \"contributionReceived\"}");
        SomuItem somuItem2 = new SomuItem(somuUUID, caseUUID, somuTypeUuid, "{ \"contributionDueDate\" : \"2020-09-10\", \"contributionStatus\" : \"contributionCancelled\"}");
        ContributionStatus contributionStatus = contributionsProcessor.highestContributionStatus(Set.of(somuItem1, somuItem2), LocalDate.of(2020, 10, 10)).orElse(null);
        assertEquals(ContributionStatus.CONTRIBUTION_CANCELLED, contributionStatus);
    }

    @Test
    public void shouldReturnDueBeforeCancelled() {
        SomuItem somuItem1 = new SomuItem(somuUUID, caseUUID, somuTypeUuid, "{ \"contributionDueDate\" : \"2020-10-11\", \"contributionStatus\" : \"\"}");
        SomuItem somuItem2 = new SomuItem(somuUUID, caseUUID, somuTypeUuid, "{ \"contributionDueDate\" : \"2020-09-10\", \"contributionStatus\" : \"contributionCancelled\"}");
        ContributionStatus contributionStatus = contributionsProcessor.highestContributionStatus(Set.of(somuItem1, somuItem2), LocalDate.of(2020, 10, 10)).orElse(null);
        assertEquals(ContributionStatus.CONTRIBUTION_DUE, contributionStatus);
    }

    @Test
    public void shouldReturnOverdueBeforeDue() {
        SomuItem somuItem1 = new SomuItem(somuUUID, caseUUID, somuTypeUuid, "{ \"contributionDueDate\" : \"2020-10-10\", \"contributionStatus\" : \"\"}");
        SomuItem somuItem2 = new SomuItem(somuUUID, caseUUID, somuTypeUuid, "{ \"contributionDueDate\" : \"2020-09-10\"}");
        ContributionStatus contributionStatus = contributionsProcessor.highestContributionStatus(Set.of(somuItem1, somuItem2), LocalDate.of(2020, 10, 10)).orElse(null);
        assertEquals(ContributionStatus.CONTRIBUTION_OVERDUE, contributionStatus);
    }

    @Test
    public void shouldReturnDueWithOtherCompleted() {
        SomuItem somuItem1 = new SomuItem(somuUUID, caseUUID, somuTypeUuid, "{ \"contributionDueDate\" : \"2020-10-10\", \"contributionStatus\" : \"contributionReceived\"}");
        SomuItem somuItem2 = new SomuItem(somuUUID, caseUUID, somuTypeUuid, "{ \"contributionDueDate\" : \"2020-09-10\", \"contributionStatus\" : \"contributionCancelled\"}");
        SomuItem somuItem3 = new SomuItem(somuUUID, caseUUID, somuTypeUuid, "{ \"contributionDueDate\" : \"2020-11-10\"}");
        ContributionStatus contributionStatus = contributionsProcessor.highestContributionStatus(Set.of(somuItem1, somuItem2, somuItem3), LocalDate.of(2020, 10, 10)).orElse(null);
        assertEquals(ContributionStatus.CONTRIBUTION_DUE, contributionStatus);
    }

    @Test
    public void shouldReturnDueForeDueFutureDueDate() {
        SomuItem somuItem = new SomuItem(somuUUID, caseUUID, somuTypeUuid, "{ \"contributionDueDate\" : \"2020-11-10\"}");
        ContributionStatus contributionStatus = contributionsProcessor.highestContributionStatus(Set.of(somuItem), LocalDate.of(2020, 10, 10)).orElse(null);
        assertEquals(ContributionStatus.CONTRIBUTION_DUE, contributionStatus);
    }

    @Test
    public void shouldReturnFilteredSomuItems() {
        SomuItem somuItem1 = new SomuItem(somuUUID, caseUUID, somuTypeUuid, "{ \"contributionDueDate\" : \"2020-11-10\"}");
        SomuItem somuItem2 = new SomuItem(somuUUID, caseUUID, somuTypeUuid, "{ \"TEST\" : \"2020-11-10\"}");
        Set<SomuItem> contributions = contributionsProcessor.filterContributions(Set.of(somuItem1, somuItem2));
        assertEquals(contributions.size(),1);
        assertTrue(contributions.contains(somuItem1));
    }

    @Test
    public void shouldReturnFilteredSomuItems_noContributions() {
        SomuItem somuItem1 = new SomuItem(somuUUID, caseUUID, somuTypeUuid, "{ \"TEST\" : \"2020-11-10\"}");
        SomuItem somuItem2 = new SomuItem(somuUUID, caseUUID, somuTypeUuid, "{ \"TEST\" : \"2020-11-10\"}");
        Set<SomuItem> contributions = contributionsProcessor.filterContributions(Set.of(somuItem1, somuItem2));
        assertEquals(contributions.size(),0);
    }

    @Test
    public void shouldReturnFilteredSomuItems_allContributions() {
        SomuItem somuItem1 = new SomuItem(somuUUID, caseUUID, somuTypeUuid, "{ \"contributionDueDate\" : \"2020-11-10\"}");
        SomuItem somuItem2 = new SomuItem(somuUUID, caseUUID, somuTypeUuid, "{ \"contributionDueDate\" : \"2020-11-11\"}");
        Set<SomuItem> contributions = contributionsProcessor.filterContributions(Set.of(somuItem1, somuItem2));
        assertEquals(contributions.size(),2);
        assertTrue(contributions.contains(somuItem1));
        assertTrue(contributions.contains(somuItem2));
    }

    @Test
    public void shouldReturnFilteredSomuItemsByCase() {
        SomuItem somuItem1 = new SomuItem(somuUUID, caseUUID, somuTypeUuid, "{ \"contributionDueDate\" : \"2020-11-10\"}");
        SomuItem somuItem2 = new SomuItem(somuUUID, UUID.randomUUID(), somuTypeUuid, "{ \"contributionDueDate\" : \"2020-11-10\"}");
        Set<SomuItem> contributions = contributionsProcessor.filterContributionsByCase(caseUUID, Set.of(somuItem1, somuItem2));
        assertEquals(contributions.size(),1);
        assertTrue(contributions.contains(somuItem1));
    }

    @Test
    public void shouldReturnFilteredSomuItemsByCase_noContributions() {
        SomuItem somuItem1 = new SomuItem(somuUUID, UUID.randomUUID(), somuTypeUuid, "{ \"contributionDueDate\" : \"2020-11-10\"}");
        SomuItem somuItem2 = new SomuItem(somuUUID, UUID.randomUUID(), somuTypeUuid, "{ \"contributionDueDate\" : \"2020-11-10\"}");
        Set<SomuItem> contributions = contributionsProcessor.filterContributionsByCase(caseUUID, Set.of(somuItem1, somuItem2));
        assertEquals(contributions.size(),0);
    }

    @Test
    public void shouldReturnFilteredSomuItemsByCase_allContributions() {
        SomuItem somuItem1 = new SomuItem(somuUUID, caseUUID, somuTypeUuid, "{ \"contributionDueDate\" : \"2020-11-10\"}");
        SomuItem somuItem2 = new SomuItem(somuUUID, caseUUID, somuTypeUuid, "{ \"contributionDueDate\" : \"2020-11-11\"}");
        Set<SomuItem> contributions = contributionsProcessor.filterContributions(Set.of(somuItem1, somuItem2));
        assertEquals(contributions.size(),2);
        assertTrue(contributions.contains(somuItem1));
        assertTrue(contributions.contains(somuItem2));
    }
}
