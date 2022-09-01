package uk.gov.digital.ho.hocs.casework.api;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("local")
public class CaseDataSummaryServiceTest {

    @Autowired
    CaseDataSummaryService caseDataSummaryService;

    @Test
    public void caseTypeNotWithinConfigurationReturnsEmptySet() {
        assertTrue(
                caseDataSummaryService.getAdditionalCaseDataFieldsByCaseType("UNKNOWN", Collections.emptyMap())
                        .isEmpty());
    }

    @Test
    public void emptyValuesReturnedWithNoMatchingCaseData() {
        assertTrue(
                caseDataSummaryService.getAdditionalCaseDataFieldsByCaseType("TEST", Collections.emptyMap())
                        .isEmpty());
    }

    @Test
    public void partialValuesReturnedWithPartialMatch() {
        var caseDataMap = Map.of("TestDate", "Test", "TestWithHardcodedChoice", "Test");

        assertEquals(
                2,
                caseDataSummaryService.getAdditionalCaseDataFieldsByCaseType("TEST", caseDataMap).size());
    }

    @Test
    public void conditionalChoicesReducedToSingleChoice() {
        var caseDataMap = Map.of("TestWithConditionalChoices", "Test", "TestField", "TestValue1");

        var values = caseDataSummaryService.getAdditionalCaseDataFieldsByCaseType("TEST", caseDataMap);

        assertEquals(1, values.size());
        assertEquals(
                "TEST_STATIC",
                values.stream().findFirst().orElseThrow(AssertionError::new).getChoices()
        );
    }

    @Test
    public void conditionalChoicesChoiceValueNotPresentReturnNoChoice() {
        var caseDataMap = Map.of("TestWithConditionalChoices", "Test");

        var values = caseDataSummaryService.getAdditionalCaseDataFieldsByCaseType("TEST", caseDataMap);

        assertEquals(1, values.size());
        assertNull(
                values.stream().findFirst().orElseThrow(AssertionError::new).getChoices()
        );
    }

}
