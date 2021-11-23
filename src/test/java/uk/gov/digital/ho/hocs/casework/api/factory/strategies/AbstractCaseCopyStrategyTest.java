package uk.gov.digital.ho.hocs.casework.api.factory.strategies;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class AbstractCaseCopyStrategyTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static String FROM_CLOB;
    private static String EXISTING_CLOB;

    static {
        Map<String, String> fromCaseValues = Map.of("key1", "value1", "key2", "value2", "key3", "value3", "key4", "value4");
        Map<String, String> existingTargetValues = Map.of("exkey1", "exvalue1", "exkey2", "exvalue2");
        try {
            FROM_CLOB = MAPPER.writeValueAsString(fromCaseValues);
            EXISTING_CLOB = MAPPER.writeValueAsString(existingTargetValues);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    private static CaseData FROM_CASE = new CaseData(1L,
            null,
            null,
            null,
            null,
            false,
            FROM_CLOB,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            false,
            null,
            null);

    private CaseData toCase;
    private AbstractCaseCopyStrategy abstractStrategy;

    @Before
    public void setUp() {
        abstractStrategy = new AbstractCaseCopyStrategy(new ObjectMapper()) {
        };
        toCase = new CaseData(2L,
                null,
                null,
                null,
                null,
                false,
                EXISTING_CLOB,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                false,
                null,
                null);
    }


    @Test
    public void shouldCopyDataValidKeys() {

        // given
        String[] copyClobKeys = new String[] {"key2", "key3"};

        // when
        abstractStrategy.copyClobData(FROM_CASE, toCase, copyClobKeys);

        // then
        Map<String, String> expectedValues = Map.of("exkey1", "exvalue1",
                "exkey2", "exvalue2",
                "key2", "value2",
                "key3", "value3");
        assertThat(toCase.getDataMap(MAPPER)).isEqualTo(expectedValues);
    }

    @Test
    public void shouldCopyDataWithMissingCopyKeys() {

        // given
        String[] copyClobKeys = new String[] {"key2", "key3", "missingKey"};

        // when
        abstractStrategy.copyClobData(FROM_CASE, toCase, copyClobKeys);

        // then
        Map<String, String> expectedValues = Map.of("exkey1", "exvalue1",
                "exkey2", "exvalue2",
                "key2", "value2",
                "key3", "value3");
        assertThat(toCase.getDataMap(MAPPER)).isEqualTo(expectedValues);
    }

}