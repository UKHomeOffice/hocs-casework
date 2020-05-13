package uk.gov.digital.ho.hocs.casework.client.infoclient;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@AllArgsConstructor()
@NoArgsConstructor()
@Getter
public class PriorityPolicyDto {

    private String policyType;
    private String caseType;
    private Map<String, String> config;
}
