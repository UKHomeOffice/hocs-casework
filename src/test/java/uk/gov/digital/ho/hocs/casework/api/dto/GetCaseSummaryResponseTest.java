package uk.gov.digital.ho.hocs.casework.api.dto;

import org.junit.Test;
import uk.gov.digital.ho.hocs.casework.domain.model.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class GetCaseSummaryResponseTest {

    @Test
    public void getCaseSummaryResponse_shouldOrderAdditionalFields() {
        LocalDate caseCreated = LocalDate.now().minusDays(10);
        LocalDate caseDeadline = LocalDate.now().plusDays(20);

        Map<String, LocalDate> stageDeadlines = new HashMap<>();
        stageDeadlines.put("Stage1", LocalDate.now().plusDays(10));
        stageDeadlines.put("Stage2", LocalDate.now().plusDays(5));
        stageDeadlines.put("Stage3", LocalDate.now().plusDays(12));

        AdditionalField field1 = new AdditionalField("label1", "Value", "Type", null);
        AdditionalField field2 = new AdditionalField("label2", "Value", "Type", null );
        AdditionalField field3 = new AdditionalField("label3", "Value", "Type", null );
        AdditionalField field4 = new AdditionalField("label4", "Value", "Type", null );
        AdditionalField field5 = new AdditionalField("label5", "Value", "Type", null );

        Set<AdditionalField> additionalFields = new HashSet<>();
        additionalFields.add(field5);
        additionalFields.add(field3);
        additionalFields.add(field1);
        additionalFields.add(field2);
        additionalFields.add(field4);

        CaseSummary caseSummary = new CaseSummary(caseCreated, caseDeadline, stageDeadlines, additionalFields, null, null,null, null, null);

        GetCaseSummaryResponse getCaseSummaryResponse = GetCaseSummaryResponse.from(caseSummary);

        assertThat(getCaseSummaryResponse.getCaseCreated()).isEqualTo(caseCreated);
        assertThat(getCaseSummaryResponse.getCaseDeadline()).isEqualTo(caseDeadline);
        assertThat(getCaseSummaryResponse.getAdditionalFields().size()).isEqualTo(5);
        assertThat(getCaseSummaryResponse.getAdditionalFields().get(0).getLabel()).isEqualTo(field1.getLabel());
        assertThat(getCaseSummaryResponse.getAdditionalFields().get(1).getLabel()).isEqualTo(field2.getLabel());
        assertThat(getCaseSummaryResponse.getAdditionalFields().get(2).getLabel()).isEqualTo(field3.getLabel());
        assertThat(getCaseSummaryResponse.getAdditionalFields().get(3).getLabel()).isEqualTo(field4.getLabel());
        assertThat(getCaseSummaryResponse.getAdditionalFields().get(4).getLabel()).isEqualTo(field5.getLabel());
    }
}
