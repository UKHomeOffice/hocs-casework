package uk.gov.digital.ho.hocs.casework.domain.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.digital.ho.hocs.casework.domain.repository.JsonConfigFolderReader.CaseTypeObject;

import java.util.List;

public class CaseTypeDocumentTags implements CaseTypeObject<List<String>> {

    private final String type;
    private final List<String> tags;

    @JsonCreator
    public CaseTypeDocumentTags(@JsonProperty("type") String type,
                                @JsonProperty("tags") List<String> tags) {
        this.type = type;
        this.tags = tags;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public List<String> getValue() {
        return tags;
    }

}
