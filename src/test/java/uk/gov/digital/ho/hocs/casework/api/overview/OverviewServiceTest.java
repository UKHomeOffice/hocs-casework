package uk.gov.digital.ho.hocs.casework.api.overview;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.springframework.data.domain.Page;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseOverview;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseOverviewRaw;
import uk.gov.digital.ho.hocs.casework.domain.repository.OverviewRepository;

public class OverviewServiceTest {

  @Test
  public void shouldFindPageWithRepositoryAndHydrate() {

    //given
    OverviewRepository repository = mock(OverviewRepository.class);
    OverviewHydrationService hydrationService = mock(OverviewHydrationService.class);
    OverviewService overviewService = new OverviewService(repository, hydrationService);
    PageRequest pageRequest = mock(PageRequest.class);
    Page<CaseOverviewRaw> rawPage = mock(Page.class);
    Page<CaseOverview> hydratedPage = mock(Page.class);
    when(repository.findByQuery(pageRequest)).thenReturn(rawPage);
    when(hydrationService.hydrate(rawPage)).thenReturn(hydratedPage);

    //when
    Page<CaseOverview> response = overviewService.getOverview(pageRequest);

    //then
    assertThat(response).isEqualTo(hydratedPage);
  }
}
