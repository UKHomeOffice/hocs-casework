package uk.gov.digital.ho.hocs.casework.api.dto;

import org.junit.Test;
import uk.gov.digital.ho.hocs.casework.api.utils.CaseDataTypeFactory;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class MigrateCaseResponseTest {

    @Test
    public void getMigrateCaseResponse() {

        CaseDataType type = CaseDataTypeFactory.from("MIN", "a1");
        Long caseNumber = 1234L;
        Map<String, String> data = new HashMap<>();
        LocalDate caseReceived = LocalDate.now();
        CaseData caseData = new CaseData(type, caseNumber, data,caseReceived);

        MigrateCaseResponse migrateCaseResponse = MigrateCaseResponse.from(caseData);

        assertThat(migrateCaseResponse.getUuid()).isEqualTo(caseData.getUuid());
        assertThat(migrateCaseResponse.getReference()).isEqualTo(caseData.getReference());

    }

    @Test
    public void getMigrateCaseResponseNull() {

        CaseDataType type = CaseDataTypeFactory.from("MIN", "a1");
        Long caseNumber = 1234L;

        LocalDate caseReceived = LocalDate.now();
        CaseData caseData = new CaseData(type, caseNumber, null, caseReceived);

        MigrateCaseResponse migrateCaseResponse = MigrateCaseResponse.from(caseData);

        assertThat(migrateCaseResponse.getUuid()).isEqualTo(caseData.getUuid());
        assertThat(migrateCaseResponse.getReference()).isEqualTo(caseData.getReference());

    }

}
