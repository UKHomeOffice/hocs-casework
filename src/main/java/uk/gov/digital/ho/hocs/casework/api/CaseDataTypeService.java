package uk.gov.digital.ho.hocs.casework.api;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.api.dto.CaseDataType;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.domain.exception.ApplicationExceptions;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static uk.gov.digital.ho.hocs.casework.application.LogEvent.CASE_TYPE_LOOKUP_FAILED;

@Slf4j
@Service
public class CaseDataTypeService {

    private final InfoClient infoClient;

    @Autowired
    public CaseDataTypeService(InfoClient infoClient) {
        this.infoClient = infoClient;
    }

    public CaseDataType getCaseDataType(String caseType) {
        return getCaseDataType(it -> it.getDisplayCode().equals(caseType)).orElseThrow(() -> getNotFoundException(caseType));
    }

    public CaseDataType getCaseDataType(UUID caseUUID){
        var shortCode = caseUUID.toString().substring(34);
        return getCaseDataType(it -> it.getShortCode().equals(shortCode)).orElseThrow(() -> getNotFoundException(shortCode));
    }

    public Stream<CaseDataType> getAllCaseDataTypes(Predicate<CaseDataType> predicate) {
        return infoClient.getAllCaseDataTypes().stream().filter(predicate);
    }

    private Optional<CaseDataType> getCaseDataType(Predicate<CaseDataType> predicate) {
        return getAllCaseDataTypes(predicate).findFirst();
    }

    private static ApplicationExceptions.EntityNotFoundException getNotFoundException(String type) {
        return new ApplicationExceptions.EntityNotFoundException(String.format("CaseDataType: %s, not found!", type), CASE_TYPE_LOOKUP_FAILED);
    }

}
