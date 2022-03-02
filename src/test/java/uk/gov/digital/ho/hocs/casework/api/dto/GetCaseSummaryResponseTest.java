package uk.gov.digital.ho.hocs.casework.api.dto;

import org.junit.Test;
import uk.gov.digital.ho.hocs.casework.domain.model.AdditionalField;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseSummary;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class GetCaseSummaryResponseTest {

    public static final UUID PREVIOUS_CASE_UUID = UUID.randomUUID();
    public static final UUID PREVIOUS_STAGE_UUID = UUID.randomUUID();
    public static final String PREV_CASE_REF = "REF/1234567/21";

    @Test
    public void getCaseSummaryResponseIncludeAllFields() {
        LocalDate caseCreated = LocalDate.now().minusDays(10);
        LocalDate caseDeadline = LocalDate.now().plusDays(20);

        Map<String, LocalDate> stageDeadlines = new HashMap<>();
        stageDeadlines.put("Stage1", LocalDate.now().plusDays(10));
        stageDeadlines.put("Stage2", LocalDate.now().plusDays(5));
        stageDeadlines.put("Stage3", LocalDate.now().plusDays(12));

        AdditionalField field1 = new AdditionalField("label1", "Value", "Type", null, "label1");
        AdditionalField field2 = new AdditionalField("label2", "Value", "Type", null, "label2");
        AdditionalField field3 = new AdditionalField("label3", "Value", "Type", null, "label3");
        AdditionalField field4 = new AdditionalField("label4", "Value", "Type", null, "label4");
        AdditionalField field5 = new AdditionalField("label5", "Value", "Type", null, "label5");

        Set<AdditionalField> additionalFields = new HashSet<>();
        additionalFields.add(field5);
        additionalFields.add(field3);
        additionalFields.add(field1);
        additionalFields.add(field2);
        additionalFields.add(field4);

        Map<String, List<ActionDataDto>> actionData = new HashMap<>();

        CaseActionDataResponseDto actions = CaseActionDataResponseDto.from(actionData, List.of(), caseDeadline, 10);


        CaseSummary caseSummary = new CaseSummary(
                "type",
                caseCreated,
                caseDeadline,
                stageDeadlines,
                additionalFields,
                null,
                null,
                null,
                PREV_CASE_REF,
                PREVIOUS_CASE_UUID,
                PREVIOUS_STAGE_UUID,
                actions);

        GetCaseSummaryResponse response = GetCaseSummaryResponse.from(caseSummary);

        assertThat(response.getCaseCreated()).isEqualTo(caseCreated);
        assertThat(response.getCaseDeadline()).isEqualTo(caseDeadline);
        List<AdditionalFieldDto> addFields = response.getAdditionalFields();
        assertThat(addFields.size()).isEqualTo(5);
        assertThat(addFields.get(0).getLabel()).isEqualTo(field1.getLabel());
        assertThat(addFields.get(1).getLabel()).isEqualTo(field2.getLabel());
        assertThat(addFields.get(2).getLabel()).isEqualTo(field3.getLabel());
        assertThat(addFields.get(3).getLabel()).isEqualTo(field4.getLabel());
        assertThat(addFields.get(4).getLabel()).isEqualTo(field5.getLabel());

        CaseSummaryLink link = response.getPreviousCase();
        assertThat(link).isNotNull();
        assertThat(link.getCaseUUID()).isEqualTo(PREVIOUS_CASE_UUID);
        assertThat(link.getStageUUID()).isEqualTo(PREVIOUS_STAGE_UUID);
        assertThat(link.getCaseReference()).isEqualTo(PREV_CASE_REF);
    }
}
