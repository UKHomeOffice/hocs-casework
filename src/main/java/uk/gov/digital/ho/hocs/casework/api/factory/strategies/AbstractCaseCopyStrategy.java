package uk.gov.digital.ho.hocs.casework.api.factory.strategies;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseData;

import java.util.Map;

@Slf4j
public abstract class AbstractCaseCopyStrategy {

    protected ObjectMapper mapper;

    protected AbstractCaseCopyStrategy(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    protected void copyClobData(CaseData fromCase, CaseData toCase, String[] dataClobKeys) {

        if (fromCase == null) {
            throw new IllegalArgumentException("Parameter fromCase cannot be null");
        }

        if (toCase == null) {
            throw new IllegalArgumentException("Parameter fromCase cannot be null");
        }

        if (dataClobKeys == null) {
            throw new IllegalArgumentException("Parameter dataClobKeys cannot be null");
        }

        Map<String, String> fromCaseClobData = fromCase.getDataMap(mapper);
        Map<String, String> toCaseClobData = toCase.getDataMap(mapper);

        for (String dataClobKey : dataClobKeys) {
            if (fromCaseClobData.containsKey(dataClobKey)) {
                toCaseClobData.put(dataClobKey, fromCaseClobData.get(dataClobKey));
            } else {
                log.warn("Cannot copy key:{} from case uuid:{}", dataClobKey, fromCase.getUuid());
            }
        }

        toCase.update(toCaseClobData, mapper);
    }
}
