package uk.gov.digital.ho.hocs.casework.api.dto;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class GetCorrespondentTypeResponseTest {

    @Test
    public void getGetCorrespondentTypeResponse() {

        CorrespondentTypeDto correspondentType = new CorrespondentTypeDto();
        Set<CorrespondentTypeDto> correspondentTypes = new HashSet<>();
        correspondentTypes.add(correspondentType);

        GetCorrespondentTypeResponse getCorrespondentTypeResponse = GetCorrespondentTypeResponse.from(correspondentTypes);

        assertThat(getCorrespondentTypeResponse.getCorrespondentTypes()).hasSize(1);
    }
}
