package uk.gov.digital.ho.hocs.casework.domain.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseTypeDocumentTags;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.CASE_DATA_DOCUMENT_TAG_NOT_FOUND;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.EVENT;

@Service
@Slf4j
public class CaseTypeDocumentTagRepository extends JsonConfigFolderReader {

    public final Map<String, List<String>> documentTags;

    public CaseTypeDocumentTagRepository(ObjectMapper objectMapper) {
        super(objectMapper);

        documentTags = readValueFromFolder(new TypeReference<CaseTypeDocumentTags>() {});
    }

    public List<String> getTagsByType(String type) {
        var tags = documentTags.get(type);

        if (tags == null) {
            log.warn("No mapping found for type: {}", type, value(EVENT, CASE_DATA_DOCUMENT_TAG_NOT_FOUND));
            return Collections.emptyList();
        }
        return tags;
    }

    @Override
    String getFolderName() {
        return "document-tags";
    }

}
