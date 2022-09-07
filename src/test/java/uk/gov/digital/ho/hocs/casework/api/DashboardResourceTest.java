package uk.gov.digital.ho.hocs.casework.api;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DashboardResourceTest {

    private final UUID caseUuid = UUID.randomUUID();

    @Mock
    private DashboardService dashboardService;

    private DashboardResource dashboardResource;

    @Before
    public void setUp() {
        dashboardResource = new DashboardResource(dashboardService);
    }

    @Test
    public void getDashboard_returnsListOfResults() {
        Map<UUID, Map<String, Integer>> result = Map.of(caseUuid, Map.of("Result1", 0, "Result2", 1));

        when(dashboardService.getDashboard()).thenReturn(result);

        var response = dashboardResource.getDashboard();

        assertThat(response).isNotNull();
        assertThat(Objects.requireNonNull(response.getBody()).getDashboardSummaries().get(0).getTeamUuid()).isEqualTo(
            caseUuid);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    public void getDashboard_returnsEmptyMap_returnEmptyObject() {
        when(dashboardService.getDashboard()).thenReturn(Collections.emptyMap());

        var response = dashboardResource.getDashboard();

        assertThat(response).isNotNull();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

}
