package uk.gov.digital.ho.hocs.casework.domain.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseDataDetails;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.CASE_DATA_DETAILS_NOT_FOUND;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.EVENT;

@Service
@Slf4j
public class CaseDataDetailsGroupsRepository extends JsonConfigFolderReader {

    public final Map<String, Map<String, List<CaseDataDetails.Fields>>> detailsFields;

    public CaseDataDetailsGroupsRepository(ObjectMapper objectMapper) {
        super(objectMapper);

        detailsFields = readValueFromFolder(new TypeReference<CaseDataDetails>() {});
    }

    public Map<String, List<CaseDataDetails.Fields>> getGroupsDetailsFieldsByType(String type) {
        var detailFields = detailsFields.get(type);

        if (detailFields == null) {
            log.warn("No mapping found for type: {}", type, value(EVENT, CASE_DATA_DETAILS_NOT_FOUND));
            return Collections.emptyMap();
        }
        return detailFields;
    }

    @Override
    String getFolderName() {
        return "details/groups";
    }

}
