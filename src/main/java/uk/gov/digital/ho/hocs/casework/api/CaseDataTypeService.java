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

    public Stream<CaseDataType> getCaseDataTypes(Predicate<CaseDataType> predicate) {
        return infoClient.getAllCaseDataTypes().stream().filter(predicate);
    }

    public CaseDataType getCaseDataType(String caseType) {
        var type = getNonNullValue(caseType);
        return getCaseDataTypes(it -> it.getDisplayCode().equals(type)).findFirst().orElseThrow(() -> getNotFoundException(type));
    }

    public CaseDataType getCaseDataType(UUID caseUUID){
        var shortCode = getNonNullValue(caseUUID).toString().substring(34);
        return getCaseDataTypes(it -> it.getShortCode().equals(shortCode)).findFirst().orElseThrow(() -> getNotFoundException(shortCode));
    }

    private static <T> T getNonNullValue(T value) {
        return Optional.ofNullable(value).orElseThrow(() -> getNotFoundException("null"));
    }

    private static ApplicationExceptions.EntityNotFoundException getNotFoundException(String type) {
        return new ApplicationExceptions.EntityNotFoundException(String.format("CaseDataType: %s, not found!", type), CASE_TYPE_LOOKUP_FAILED);
    }

}
