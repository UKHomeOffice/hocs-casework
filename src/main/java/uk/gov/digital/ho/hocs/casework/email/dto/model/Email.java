package uk.gov.digital.ho.hocs.casework.email.dto.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@AllArgsConstructor
@Getter
public class Email {

    private String emailAddress;

    private String teamName;

    private UUID caseUUID;

    private String caseReference;

    private String caseStatus;

    private String templateId;

    public static String toJsonString(ObjectMapper objectMapper, Email email) {
        String ret = "";
        try {
            ret = objectMapper.writeValueAsString(email);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return ret;
    }
}
