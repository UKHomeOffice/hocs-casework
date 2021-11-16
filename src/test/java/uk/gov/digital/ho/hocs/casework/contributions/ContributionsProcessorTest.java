package uk.gov.digital.ho.hocs.casework.contributions;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.digital.ho.hocs.casework.api.SomuItemService;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.domain.model.SomuItem;
import uk.gov.digital.ho.hocs.casework.domain.model.Stage;

import java.time.LocalDate;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@SpringBootTest
@RunWith(SpringRunner.class)
public class ContributionsProcessorTest {
    private final UUID caseUuid = UUID.randomUUID();
    private final UUID somuUuid = UUID.randomUUID();
    private final UUID somuTypeUuid = UUID.randomUUID();
    private final UUID teamUuid = UUID.randomUUID();
    private final UUID userUuid = UUID.randomUUID();
    private final UUID transitionNoteUuid = UUID.randomUUID();

    @Autowired
    private ObjectMapper objectMapper;

    private ContributionsProcessor contributionsProcessor;

    @Before
    public void before() {
        contributionsProcessor = spy(new ContributionsProcessor(objectMapper));
    }

    @Test
    public void shouldReturnIfZeroSomuItems() {
        Stage stage = new Stage(caseUuid, "ANY", teamUuid, userUuid, transitionNoteUuid);

        contributionsProcessor.processContributionsForStages(stage, Collections.emptyList(), Collections.emptyList());

        verify(contributionsProcessor).processContributionsForStages(stage, Collections.emptyList(), Collections.emptyList());
        verifyNoMoreInteractions(contributionsProcessor);
    }

    @Test
    public void shouldNotReturnDataForNonContribution() {
        Stage stage = spy(new Stage(caseUuid, "ANY", teamUuid, userUuid, transitionNoteUuid));
        SomuItem somuItem = new SomuItem(somuUuid, caseUuid, somuTypeUuid, "{ \"TEST\" : \"TEST\"}");

        when(stage.getCaseDataType()).thenReturn("COMP");

        contributionsProcessor.processContributionsForStages(stage, List.of(somuItem), Collections.emptyList());

        verify(contributionsProcessor).processContributionsForStages(stage, List.of(somuItem), Collections.emptyList());
        verifyNoMoreInteractions(contributionsProcessor);
    }

    @Test
    public void shouldReturnDataWithValidDueContribution() {
        Stage stage = spy(new Stage(caseUuid, "COMP_SERVICE_TRIAGE", teamUuid, userUuid, transitionNoteUuid));
        SomuItem somuItem = new SomuItem(somuUuid, caseUuid, somuTypeUuid, "{ \"contributionDueDate\" : \"9999-12-31\"}");

        when(stage.getCaseDataType()).thenReturn("COMP");

        //when(infoClient.getStageContributions(stage.getStageType())).thenReturn(true);

        contributionsProcessor.processContributionsForStages(stage, List.of(somuItem), Collections.emptyList());

        verify(contributionsProcessor).processContributionsForStages(stage, List.of(somuItem), Collections.emptyList());
        verify(contributionsProcessor).calculateDueContributionDate(any());
        verify(contributionsProcessor).highestContributionStatus(any());
        verify(contributionsProcessor).highestContributionStatus(any(), any());
        verifyNoMoreInteractions(contributionsProcessor);

        assertEquals(stage.getDueContribution(), "9999-12-31");
        assertEquals(stage.getContributions(), Contribution.ContributionStatus.CONTRIBUTION_DUE.getDisplayedStatus());
    }

    @Test
    public void shouldReturnDataWithValidOverdueContribution() {
        Stage stage = spy(new Stage(caseUuid, "COMP_SERVICE_TRIAGE", teamUuid, userUuid, transitionNoteUuid));
        SomuItem somuItem = new SomuItem(somuUuid, caseUuid, somuTypeUuid, "{ \"contributionDueDate\" : \"0000-12-31\"}");

        when(stage.getCaseDataType()).thenReturn("COMP");

        //when(infoClient.getStageContributions(stage.getStageType())).thenReturn(true);

        contributionsProcessor.processContributionsForStages(stage, List.of(somuItem), Collections.emptyList());

        verify(contributionsProcessor).processContributionsForStages(stage, List.of(somuItem), Collections.emptyList());
        verify(contributionsProcessor).calculateDueContributionDate(any());
        verify(contributionsProcessor).highestContributionStatus(any());
        verify(contributionsProcessor).highestContributionStatus(any(), any());
        verifyNoMoreInteractions(contributionsProcessor);

        assertEquals(stage.getDueContribution(), "0000-12-31");
        assertEquals(stage.getContributions(), Contribution.ContributionStatus.CONTRIBUTION_OVERDUE.getDisplayedStatus());
    }

    @Test
    public void shouldReturnDataWithValidReceivedContribution() {
        Stage stage = spy(new Stage(caseUuid, "COMP_SERVICE_TRIAGE", teamUuid, userUuid, transitionNoteUuid));
        SomuItem somuItem = new SomuItem(somuUuid, caseUuid, somuTypeUuid, "{ \"contributionDueDate\" : \"9999-12-31\", \"contributionStatus\": \"contributionReceived\"}");

        when(stage.getCaseDataType()).thenReturn("COMP");

        //when(infoClient.getStageContributions(stage.getStageType())).thenReturn(true);

        contributionsProcessor.processContributionsForStages(stage, List.of(somuItem), Collections.emptyList());

        verify(contributionsProcessor).processContributionsForStages(stage, List.of(somuItem), Collections.emptyList());
        verify(contributionsProcessor).calculateDueContributionDate(any());
        verify(contributionsProcessor).highestContributionStatus(any());
        verify(contributionsProcessor).highestContributionStatus(any(), any());
        verifyNoMoreInteractions(contributionsProcessor);

        assertNull(stage.getDueContribution());
        assertEquals(stage.getContributions(), Contribution.ContributionStatus.CONTRIBUTION_RECEIVED.getDisplayedStatus());
    }

    @Test
    public void shouldReturnDataWithValidCancelledContribution() {
        Stage stage = spy(new Stage(caseUuid, "COMP_SERVICE_TRIAGE", teamUuid, userUuid, transitionNoteUuid));
        SomuItem somuItem = new SomuItem(somuUuid, caseUuid, somuTypeUuid, "{ \"contributionDueDate\" : \"9999-12-31\", \"contributionStatus\": \"contributionCancelled\"}");

        when(stage.getCaseDataType()).thenReturn("COMP");

        //when(infoClient.getStageContributions(stage.getStageType())).thenReturn(true);

        contributionsProcessor.processContributionsForStages(stage, List.of(somuItem), Collections.emptyList());

        verify(contributionsProcessor).processContributionsForStages(stage, List.of(somuItem), Collections.emptyList());
        verify(contributionsProcessor).calculateDueContributionDate(any());
        verify(contributionsProcessor).highestContributionStatus(any());
        verify(contributionsProcessor).highestContributionStatus(any(), any());
        verifyNoMoreInteractions(contributionsProcessor);

        assertNull(stage.getDueContribution());
        assertEquals(stage.getContributions(), Contribution.ContributionStatus.CONTRIBUTION_CANCELLED.getDisplayedStatus());
    }

    @Test
    public void shouldCalculateDueContribution() {
        Contribution contribution1 = new Contribution(LocalDate.parse("2020-10-10"), null);
        Contribution contribution2 = new Contribution(LocalDate.parse("2020-09-10"), null);

        LocalDate dueContributionDate = contributionsProcessor.calculateDueContributionDate(Set.of(contribution1, contribution2)).orElseThrow();
        assertEquals("2020-09-10", dueContributionDate.toString());
    }

    @Test
    public void shouldCalculateDueContributionWithStatus() {
        Contribution contribution1 = new Contribution(LocalDate.parse("2020-10-10"), null);
        Contribution contribution2 = new Contribution(LocalDate.parse("2020-09-10"), "contributionReceived");

        LocalDate dueContributionDate = contributionsProcessor.calculateDueContributionDate(Set.of(contribution1, contribution2)).orElseThrow();
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

        Contribution.ContributionStatus contributionStatus = contributionsProcessor.highestContributionStatus(Set.of(contribution1, contribution2), LocalDate.of(2020, 10, 10)).orElse(null);
        assertEquals(Contribution.ContributionStatus.CONTRIBUTION_CANCELLED, contributionStatus);
    }

    @Test
    public void shouldReturnDueBeforeCancelled() {
        Contribution contribution1 = new Contribution(LocalDate.parse("2020-10-11"), "");
        Contribution contribution2 = new Contribution(LocalDate.parse("2020-09-10"), "contributionCancelled");

        Contribution.ContributionStatus contributionStatus = contributionsProcessor.highestContributionStatus(Set.of(contribution1, contribution2), LocalDate.of(2020, 10, 10)).orElse(null);
        assertEquals(Contribution.ContributionStatus.CONTRIBUTION_DUE, contributionStatus);
    }

    @Test
    public void shouldReturnOverdueBeforeDue() {
        Contribution contribution1 = new Contribution(LocalDate.parse("2020-10-10"), "");
        Contribution contribution2 = new Contribution(LocalDate.parse("2020-09-10"), null);

        Contribution.ContributionStatus contributionStatus = contributionsProcessor.highestContributionStatus(Set.of(contribution1, contribution2), LocalDate.of(2020, 10, 10)).orElse(null);
        assertEquals(Contribution.ContributionStatus.CONTRIBUTION_OVERDUE, contributionStatus);
    }

    @Test
    public void shouldReturnDueWithOtherCompleted() {
        Contribution contribution1 = new Contribution(LocalDate.parse("2020-10-10"), "contributionReceived");
        Contribution contribution2 = new Contribution(LocalDate.parse("2020-09-10"), "contributionCancelled");
        Contribution contribution3 = new Contribution(LocalDate.parse("2020-11-10"), null);

        Contribution.ContributionStatus contributionStatus = contributionsProcessor.highestContributionStatus(Set.of(contribution1, contribution2, contribution3), LocalDate.of(2020, 10, 10)).orElse(null);
        assertEquals(Contribution.ContributionStatus.CONTRIBUTION_DUE, contributionStatus);
    }

    @Test
    public void shouldReturnDueForDueFutureDueDate() {
        Contribution contribution1 = new Contribution(LocalDate.parse("2020-11-10"), null);

        Contribution.ContributionStatus contributionStatus = contributionsProcessor.highestContributionStatus(Set.of(contribution1), LocalDate.of(2020, 10, 10)).orElse(null);
        assertEquals(Contribution.ContributionStatus.CONTRIBUTION_DUE, contributionStatus);
    }

}
