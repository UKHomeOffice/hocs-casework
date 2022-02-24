package uk.gov.digital.ho.hocs.casework.api.dto;

import org.junit.Test;
import uk.gov.digital.ho.hocs.casework.api.utils.CaseDataTypeFactory;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class MigrateCaseRequestTest {

    @Test
    public void getMigrateCaseRequest() {

        CaseDataType caseDataType = CaseDataTypeFactory.from("MIN", "a1");
        Map<String, String> data = new HashMap<>();
        LocalDate caseReceived = LocalDate.now();
        UUID fromUUID = UUID.randomUUID();

        MigrateCaseRequest migrateCaseRequest = new MigrateCaseRequest(caseDataType.getDisplayCode(), data, caseReceived, fromUUID);

        assertThat(migrateCaseRequest.getType()).isEqualTo(caseDataType.getDisplayCode());
        assertThat(migrateCaseRequest.getData()).isEqualTo(data);

    }

    @Test
    public void getMigrateCaseRequestNull() {

        MigrateCaseRequest migrateCaseRequest = new MigrateCaseRequest(null, null, null, null);

        assertThat(migrateCaseRequest.getType()).isNull();
        assertThat(migrateCaseRequest.getData()).isNull();
    }

}