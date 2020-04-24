package uk.gov.digital.ho.hocs.casework.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.application.RequestData;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.client.infoclient.PermissionDto;
import uk.gov.digital.ho.hocs.casework.client.infoclient.TeamDto;

import java.nio.BufferUnderflowException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserPermissionsService {

    private RequestData requestData;
    private InfoClient infoClient;

    @Autowired
    public UserPermissionsService(RequestData requestData, InfoClient infoClient) {
        this.requestData = requestData;
        this.infoClient = infoClient;
    }

    public UUID getUserId() {
        return UUID.fromString(requestData.userId());
    }

    public AccessLevel getMaxAccessLevel(String caseType) {
        Set<PermissionDto> permissionDtos = getUserPermission();
        Optional<PermissionDto> maxPermission = permissionDtos.stream()
                .filter(e-> e.getCaseTypeCode().equals(caseType))
                .max(Comparator.comparing(PermissionDto::getAccessLevel));
        return maxPermission.orElse(
               new PermissionDto("", AccessLevel.UNSET)
        ).getAccessLevel();
    }


    public Set<UUID> getUserTeams() {
        String[] groups = requestData.groupsArray();
        return Arrays.stream(groups)
                .map(this::getUUIDFromBase64)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    public Set<String> getUserCaseTypes() {
        return getUserPermission().stream()
                .map(PermissionDto::getCaseTypeCode)
                .collect(Collectors.toSet());


    }

    Set<PermissionDto> getUserPermission() {
        Set<TeamDto> teamDtos = infoClient.getTeams();
        Set<UUID> userTeams = getUserTeams();
        return teamDtos.stream()
                .filter(t -> userTeams.contains(t.getUuid()))
                .flatMap(t -> t.getPermissionDtos().stream())
                .collect(Collectors.toSet());
    }


    private UUID getUUIDFromBase64(String uuid) {
        if(uuid.startsWith("/")) {
            uuid = uuid.substring(1);
        }
        try {
            return Base64UUID.base64StringToUUID(uuid);
        }
        catch (BufferUnderflowException e) {
            return null;
        }
    }
}