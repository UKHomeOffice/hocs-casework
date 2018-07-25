package uk.gov.digital.ho.hocs.casework.audit.model;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ReportLineTest {

    @Test
    public void shouldUserHeaderOrderNotInsertionOrder() {

        String header = "B,A,C,£";

        Map<String, String> data = new HashMap<>();
        data.put("£", "0");
        data.put("A", "1");
        data.put("B", "2");
        data.put("C", "3");

        ReportLine reportLine = ReportLine.from(header, data);

        String[] values = reportLine.getLineData().values().toArray(new String[0]);
        assertThat(values).hasSize(4);
        assertThat(values[0]).isEqualTo(data.get("B"));
        assertThat(values[1]).isEqualTo(data.get("A"));
        assertThat(values[2]).isEqualTo(data.get("C"));
        assertThat(values[3]).isEqualTo(data.get("£"));

    }

    @Test
    public void shouldHandleMissingSourceData() {

        String header = "B,A,C,£";

        Map<String, String> data = new HashMap<>();
        data.put("£", "0");
        data.put("A", "1");
        data.put("C", "3");

        ReportLine reportLine = ReportLine.from(header, data);

        String[] values = reportLine.getLineData().values().toArray(new String[0]);
        assertThat(values).hasSize(4);
        assertThat(values[0]).isEqualTo("");
        assertThat(values[1]).isEqualTo(data.get("A"));
        assertThat(values[2]).isEqualTo(data.get("C"));
        assertThat(values[3]).isEqualTo(data.get("£"));
    }

    @Test
    public void shouldHandleExtraSourceData() {

        String header = "B,A,C,£";

        Map<String, String> data = new HashMap<>();
        data.put("£", "0");
        data.put("A", "1");
        data.put("B", "2");
        data.put("C", "3");
        data.put("Extra", "3");

        ReportLine reportLine = ReportLine.from(header, data);

        String[] values = reportLine.getLineData().values().toArray(new String[0]);
        assertThat(values).hasSize(4);
        assertThat(values[0]).isEqualTo(data.get("B"));
        assertThat(values[1]).isEqualTo(data.get("A"));
        assertThat(values[2]).isEqualTo(data.get("C"));
        assertThat(values[3]).isEqualTo(data.get("£"));
    }

    @Test
    public void shouldHandleNullHeader() {

        Map<String, String> data = new HashMap<>();
        data.put("£", "0");
        data.put("A", "1");
        data.put("B", "2");
        data.put("C", "3");
        data.put("Extra", "3");

        ReportLine reportLine = ReportLine.from(null, data);

        String[] values = reportLine.getLineData().values().toArray(new String[0]);
        assertThat(values).hasSize(0);
    }

    @Test
    public void shouldHandleNullData() {

        String header = "B,A,C,£";

        ReportLine reportLine = ReportLine.from(header, null);

        String[] values = reportLine.getLineData().values().toArray(new String[0]);
        assertThat(values).hasSize(4);
        assertThat(values[0]).isEqualTo("");
        assertThat(values[1]).isEqualTo("");
        assertThat(values[2]).isEqualTo("");
        assertThat(values[3]).isEqualTo("");

    }

    /*
        Doesn't try to be clever with headers.
     */
    @Test
    public void shouldHandleEmptyHeader() {

        String header = "";

        Map<String, String> data = new HashMap<>();
        data.put("£", "0");
        data.put("A", "1");
        data.put("B", "2");
        data.put("C", "3");
        data.put("Extra", "3");

        ReportLine reportLine = ReportLine.from(header, data);

        String[] values = reportLine.getLineData().values().toArray(new String[0]);
        assertThat(values).hasSize(1);
        assertThat(values[0]).isEqualTo("");
    }

    @Test
    public void shouldHandleOddHeader() {

        String header = "A,B,,A";

        Map<String, String> data = new HashMap<>();
        data.put("£", "0");
        data.put("A", "1");
        data.put("B", "2");
        data.put("C", "3");
        data.put("Extra", "4");

        ReportLine reportLine = ReportLine.from(header, data);

        String[] values = reportLine.getLineData().values().toArray(new String[0]);
        assertThat(values).hasSize(3);
        assertThat(values[0]).isEqualTo(data.get("A"));
        assertThat(values[1]).isEqualTo(data.get("B"));
        assertThat(values[2]).isEqualTo("");

    }

}
