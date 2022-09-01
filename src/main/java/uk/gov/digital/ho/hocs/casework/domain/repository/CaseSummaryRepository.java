package uk.gov.digital.ho.hocs.casework.domain.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.domain.model.SummaryFields;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
public class CaseSummaryRepository extends JsonConfigFolderReader {

    public final Map<String, List<SummaryFields.SummaryField>> summaryFields;

    public CaseSummaryRepository(ObjectMapper objectMapper) {
        super(objectMapper);

        summaryFields = readValueFromFolder(new TypeReference<SummaryFields>() {});
    }

    public List<SummaryFields.SummaryField> getSummaryFieldsByCaseType(String type) {
        return summaryFields.getOrDefault(type, Collections.emptyList());
    }

    @Override
    String getFolderName() {
        return "summary";
    }

}
