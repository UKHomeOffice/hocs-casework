package uk.gov.digital.ho.hocs.casework.api.dto;

import org.junit.Test;
import uk.gov.digital.ho.hocs.casework.domain.model.SomuItem;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class UpdateCaseDataValueTest {
    
    @Test
    public void updateCaseDataValueTest() {
        UpdateCaseDataValue updateCaseDataValue = new UpdateCaseDataValue("test");
        
        assertThat(updateCaseDataValue.getData()).isEqualTo("test");
    }
    
}
