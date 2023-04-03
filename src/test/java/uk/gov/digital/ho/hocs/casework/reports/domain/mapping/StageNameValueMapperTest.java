package uk.gov.digital.ho.hocs.casework.reports.domain.mapping;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.digital.ho.hocs.casework.api.dto.StageTypeDto;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class StageNameValueMapperTest {

    @Mock
    private InfoClient infoClient;

    private static final String STAGE_NAME_IN_CACHE = "STAGE_IN_CACHE";
    private static final String STAGE_NAME_NOT_CACHED = "STAGE_NOT_CACHED";
    private static final String STAGE_NAME_MISSING = "STAGE_MISSING";

    private static final StageTypeDto TEST_STAGE_IN_CACHE = new StageTypeDto(
        "In Cache",
        "IC",
        STAGE_NAME_IN_CACHE,
        0,
        0,
        0
    );

    private static final StageTypeDto TEST_STAGE_NOT_CACHED = new StageTypeDto(
        "Not from Cache",
        "IC",
        STAGE_NAME_NOT_CACHED,
        0,
        0,
        0
    );

    @Test
    public void whenMappingAKeyInTheCache_theCachedTeamIsReturned() {
        givenInfoServiceStageTypes();

        StageNameValueMapper underTest = new StageNameValueMapper(infoClient);
        Optional<String> mappedName = underTest.map(STAGE_NAME_IN_CACHE);

        assertThat(mappedName).isEqualTo(Optional.of("In Cache"));
        verify(infoClient, times(0)).getStageTypeByTypeString(any());
    }

    @Test
    public void whenMappingAnUncachedKey_theTeamIsLoadedFromInfoService() {
        givenInfoServiceStageTypes();
        when(infoClient.getStageTypeByTypeString(STAGE_NAME_NOT_CACHED)).thenReturn(TEST_STAGE_NOT_CACHED);

        StageNameValueMapper underTest = new StageNameValueMapper(infoClient);
        Optional<String> mappedName = underTest.map(STAGE_NAME_NOT_CACHED);

        assertThat(mappedName).isEqualTo(Optional.of("Not from Cache"));
    }

    @Test
    public void whenMappingAnMissingKey_thenEmptyIsReturned() {
        givenInfoServiceStageTypes();
        when(infoClient.getStageTypeByTypeString(STAGE_NAME_MISSING)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

        StageNameValueMapper underTest = new StageNameValueMapper(infoClient);
        Optional<String> mappedName = underTest.map(STAGE_NAME_MISSING);

        assertThat(mappedName).isEqualTo(Optional.of(STAGE_NAME_MISSING));
        verify(infoClient).getStageTypeByTypeString(STAGE_NAME_MISSING);
    }

    private void givenInfoServiceStageTypes() {
        when(infoClient.getAllStageTypes()).thenReturn(Set.of(TEST_STAGE_IN_CACHE));
    }

}
