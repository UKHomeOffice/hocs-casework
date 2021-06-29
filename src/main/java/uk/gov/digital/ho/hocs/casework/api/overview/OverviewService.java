package uk.gov.digital.ho.hocs.casework.api.overview;

import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseOverview;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseOverviewRaw;
import uk.gov.digital.ho.hocs.casework.domain.repository.OverviewRepository;

@Service
@AllArgsConstructor
public class OverviewService {

  protected final OverviewRepository repository;
  protected final OverviewHydrationService hydrationService;

  public Page<CaseOverview> getOverview(PageRequest pageRequest) {
    Page<CaseOverviewRaw> unhydratedPage = repository.findByQuery(pageRequest);
    Page<CaseOverview> hydratedPage = hydrationService.hydrate(unhydratedPage);
    return hydratedPage;
  }
}
