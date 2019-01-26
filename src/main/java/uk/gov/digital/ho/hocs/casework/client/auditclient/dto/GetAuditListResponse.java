package uk.gov.digital.ho.hocs.casework.client.auditclient.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Set;

@AllArgsConstructor
@Getter
public class GetAuditListResponse {

    private Set<GetAuditResponse> audits;

}