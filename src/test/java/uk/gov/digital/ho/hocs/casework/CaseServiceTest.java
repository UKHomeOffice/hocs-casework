package uk.gov.digital.ho.hocs.casework;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.Silent.class)
public class CaseServiceTest {

    private static final CaseDetails CASE_DETAILS = new CaseDetails();

    @Mock
    private CaseRepository mockRepo;

    private CaseService caseService;


    @Before
    public void setUp() {
        caseService = new CaseService(mockRepo);
    }

    @Test
    public void create() {
        caseService.create(CASE_DETAILS);

        verify(mockRepo).save(CASE_DETAILS);
        verify(mockRepo, times(1)).save(any(CaseDetails.class));
    }
}