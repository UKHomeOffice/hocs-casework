package uk.gov.digital.ho.hocs.casework.security.filters;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.api.dto.AdditionalFieldDto;
import uk.gov.digital.ho.hocs.casework.api.dto.CaseActionDataResponseDto;
import uk.gov.digital.ho.hocs.casework.api.dto.FieldDto;
//import uk.gov.digital.ho.hocs.casework.api.dto.GetCaseSummaryResponse;
import uk.gov.digital.ho.hocs.casework.api.dto.GetCaseSummaryResponse;
import uk.gov.digital.ho.hocs.casework.client.documentclient.DocumentDto;
import uk.gov.digital.ho.hocs.casework.client.documentclient.GetDocumentsResponse;
import uk.gov.digital.ho.hocs.casework.security.AccessLevel;
import uk.gov.digital.ho.hocs.casework.security.SecurityExceptions;
import uk.gov.digital.ho.hocs.casework.security.UserPermissionsService;

import java.util.*;

import static net.logstash.logback.argument.StructuredArguments.value;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.AUTH_FILTER_SUCCESS;
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.EVENT;

@Slf4j
@Service
public class GetCaseActionDataAuthFilterService implements AuthFilter {

    private final UserPermissionsService userPermissionsService;

    @Autowired
    public GetCaseActionDataAuthFilterService(UserPermissionsService userPermissionsService) {
        this.userPermissionsService = userPermissionsService;
    }

    @Override
    public String getKey() {
        return CaseActionDataResponseDto.class.getSimpleName();
    }

    @Override
    public Object applyFilter(ResponseEntity<?> responseEntityToFilter, AccessLevel userAccessLevel, Object[] collectionAsArray) throws SecurityExceptions.AuthFilterException {

        CaseActionDataResponseDto getCaseActionDataResponse = verifyAndReturnAsObjectType(responseEntityToFilter, CaseActionDataResponseDto.class);

        if (getCaseActionDataResponse == null) {
            return responseEntityToFilter;
        }

        UUID userId = userPermissionsService.getUserId();
        log.debug("Filtering GetCaseSummaryResponse for request by userId: {}", userId);

        Map<String, FieldDto> permittedFields = new HashMap<>();

        // is this filtering correct - I have hard wired it to BF for testing - will need another user to shake this down
        userPermissionsService.getFieldsByCaseTypeAndPermissionLevel("BF", userAccessLevel)
                .forEach(fieldDto -> permittedFields.put(fieldDto.getName(),fieldDto));

        GetCaseActionDataAuthFilterService.SettableCaseActionDataDtoSetResponse response  =
                new GetCaseActionDataAuthFilterService.SettableCaseActionDataDtoSetResponse(getCaseActionDataResponse);

        log.info("Issuing filtered GetCaseActionDataResponse for userId: {}", userId, value(EVENT, AUTH_FILTER_SUCCESS));
        return new ResponseEntity<CaseActionDataResponseDto>(response, responseEntityToFilter.getStatusCode());
    }

    public static class SettableCaseActionDataDtoSetResponse extends CaseActionDataResponseDto {

        public SettableCaseActionDataDtoSetResponse(CaseActionDataResponseDto response) {
            super(
                    response.getCaseActionData(),
                    response.getCaseTypeActionData(),
                    response.getCurrentCaseDeadline(),
                    response.getRemainingDaysUntilDeadline()
            );
        }

    }

}

//
//{
//        "caseActionData": {
//          "appeals": [],
//          "recordInterest": [
//              {
//                "actionType": "INTEREST_OUT",
//                "interestedPartyType": "BF_INTERESTED_PARTY_PRESS_OFFICE",
//                "interestedPartyEntity": {
//                "title": "Press Office"
//              },
//                "detailsOfInterest": "some details of the Ext Interest",
//                "note": null,
//                "uuid": "484e4b13-2bdb-4526-b54d-e90b9aa27ac5",
//                "caseTypeActionUuid": "170d2da6-4c21-4091-a7a9-ba09ab1ae789",
//                "caseSubtype": null,
//                "caseTypeActionLabel": "RECORD_INTEREST"
//              }
//          ],
//          "extensions": []
//        },
//        "caseTypeActionData": [
//          {
//              "uuid": "170d2da6-4c21-4091-a7a9-ba09ab1ae789",
//              "caseTypeUuid": "66f9c01a-3b7c-4613-9bcb-baa531530d41",
//              "caseType": "BF",
//              "actionType": "EXTERNAL_INTEREST",
//              "actionSubtype": "EXTERNAL_INTEREST",
//              "actionLabel": "External Interest",
//              "maxConcurrentEvents": 99999,
//              "sortOrder": 10,
//              "active": true,
//              "props": "{\"interestChoices\": \"BF_INTERESTED_PARTIES\"}"
//          }
//        ],
//        "currentCaseDeadline": "2022-05-20",
//        "remainingDaysUntilDeadline": 21
//        }