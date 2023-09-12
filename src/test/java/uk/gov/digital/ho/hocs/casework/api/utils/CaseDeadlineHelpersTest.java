package uk.gov.digital.ho.hocs.casework.api.utils;

import org.junit.Before;
import org.junit.Test;
import uk.gov.digital.ho.hocs.casework.domain.model.ActiveStage;
import uk.gov.digital.ho.hocs.casework.domain.model.Address;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseNote;
import uk.gov.digital.ho.hocs.casework.domain.model.Correspondent;
import uk.gov.digital.ho.hocs.casework.domain.model.Topic;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.Assert.*;

public class CaseDeadlineHelpersTest {

    private static final UUID CASE_UUID = UUID.fromString("f51721df-c533-434c-9455-6dc0b3718ae2");

    private static final String TOPIC_NAME = "topic_name";

    private static final UUID TOPIC_NAME_UUID = UUID.fromString("c046fb65-0c3b-4892-b80a-2c239dceded8");

    private static final String CASE_REFERENCE = "TYPE/1234567/21";

    private static final String CASE_TYPE = "TYPE";

    private static final int CASE_TYPE_SLA = 20;

    private static final String CORRESPONDENT_TYPE = "correspondent_type";

    private static final String FULLNAME = "fullname";

    private static final String ORGANISATION = "organisation";

    private static final String ADDR_1 = "addr1";

    private static final String ADDR_2 = "addr2";

    private static final String ADDR_3 = "addr3";

    private static final String ADDR_4 = "add4";

    private static final String ADDR_5 = "addr5";

    private static final String TELEPHONE = "string 1";

    private static final String EMAIL = "string 2";

    private static final String REFERENCE = "string 3";

    private static final String EXTERNAL_KEY = "string 4";

    private static final Map<String, String> DATA_CLOB = new HashMap<>() {{
        put("key1", "value1");
        put("key2", "value2");
    }};

    private static final LocalDate ORIGINAL_CASE_DEADLINE = LocalDate.of(2020, Month.APRIL, 30);

    private static final LocalDate ORIGINAL_CASE_DEADLINE_WARNING = LocalDate.of(2020, Month.APRIL, 28);

    private CaseData EXISTING_CASE;

    @Before
    public void setUp() throws Exception {
        Correspondent primaryCorrespondent = new Correspondent(CASE_UUID, CORRESPONDENT_TYPE, FULLNAME, ORGANISATION,
            new Address(ADDR_1, ADDR_2, ADDR_3, ADDR_4, ADDR_5), TELEPHONE, EMAIL, REFERENCE, EXTERNAL_KEY);

        ActiveStage stage1 = new ActiveStage();
        ActiveStage stage2 = new ActiveStage();
        stage1.setDeadline(ORIGINAL_CASE_DEADLINE.minusDays(3));
        stage1.setDeadlineWarning(ORIGINAL_CASE_DEADLINE.minusDays(5));
        stage2.setDeadline(ORIGINAL_CASE_DEADLINE.minusDays(2));
        stage2.setDeadlineWarning(ORIGINAL_CASE_DEADLINE.minusDays(4));

        Set<ActiveStage> activeStages = Set.of(stage1, stage2);

        EXISTING_CASE = new CaseData(1L, CASE_UUID, LocalDateTime.of(2020, Month.APRIL, 1, 0, 0), CASE_TYPE,
            CASE_REFERENCE, false, DATA_CLOB, TOPIC_NAME_UUID, new Topic(CASE_UUID, TOPIC_NAME, TOPIC_NAME_UUID),
            primaryCorrespondent.getUuid(), primaryCorrespondent, ORIGINAL_CASE_DEADLINE,
            ORIGINAL_CASE_DEADLINE_WARNING, ORIGINAL_CASE_DEADLINE.minusDays(CASE_TYPE_SLA), false, null, activeStages,
            Set.of(new CaseNote(CASE_UUID, "type", "text", "author")));
    }

    @Test
    public void testShouldOverrideTheStageDeadlinesToCaseDeadline() {

        // GIVEN
        final LocalDate newDeadline = LocalDate.of(2022, 4, 30);
        final LocalDate newDeadlineWarning = LocalDate.of(2022, 4, 27);
        LocalDate[] expectedStageDeadlines = new LocalDate[EXISTING_CASE.getActiveStages().size()];
        Arrays.fill(expectedStageDeadlines, newDeadline);
        LocalDate[] expectedStageDeadlineWarnings = new LocalDate[EXISTING_CASE.getActiveStages().size()];
        Arrays.fill(expectedStageDeadlineWarnings, newDeadlineWarning);

        EXISTING_CASE.setCaseDeadline(newDeadline);
        EXISTING_CASE.setCaseDeadlineWarning(newDeadlineWarning);

        // WHEN
        CaseDeadlineHelpers.overrideStageDeadlines(EXISTING_CASE);

        // THEN

        LocalDate[] stageDeadlines = EXISTING_CASE.getActiveStages().stream().map(ActiveStage::getDeadline).toArray(
            LocalDate[]::new);
        LocalDate[] stageDeadlinesWarnings = EXISTING_CASE.getActiveStages().stream().map(
            ActiveStage::getDeadlineWarning).toArray(LocalDate[]::new);

        assertArrayEquals("That all stage deadlines are set correctly", expectedStageDeadlines, stageDeadlines);
        assertArrayEquals("That all stage deadline warnings are set correctly", expectedStageDeadlineWarnings,
            stageDeadlinesWarnings);

    }

}
