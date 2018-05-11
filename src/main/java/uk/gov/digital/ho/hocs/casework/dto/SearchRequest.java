package uk.gov.digital.ho.hocs.casework.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashMap;

@AllArgsConstructor
@NoArgsConstructor
public class SearchRequest {

    @Getter
    private String caseReference;

    @Getter
    private HashMap<String, String> caseData;
}
