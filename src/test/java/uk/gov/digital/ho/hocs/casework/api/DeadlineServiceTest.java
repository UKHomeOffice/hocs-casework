package uk.gov.digital.ho.hocs.casework.api;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.casework.api.dto.CaseDataType;
import uk.gov.digital.ho.hocs.casework.api.utils.CaseDataTypeFactory;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;

import java.time.LocalDate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class DeadlineServiceTest {

    private DeadlineService deadlineService;

    @Mock
    InfoClient mockInfoService;

    private final CaseDataType caseType = CaseDataTypeFactory.from("A1", "a1");

    @Before
    public void setUp() {
        deadlineService = new DeadlineService(mockInfoService);
    }

    @Test
    public void shouldCalculateWorkingDaysForCase() {
        // given
        LocalDate originalReceivedDate = LocalDate.parse("2020-02-01");
        LocalDate expectedDeadline = LocalDate.parse("2020-03-02");

        // when
        final LocalDate result = deadlineService.calculateWorkingDaysForCaseType("A1", originalReceivedDate,
            caseType.getSla());

        // then
        assertThat(result).isEqualTo(expectedDeadline);
    }

}
