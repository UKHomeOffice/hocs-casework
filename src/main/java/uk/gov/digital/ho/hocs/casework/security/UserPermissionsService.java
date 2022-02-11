package uk.gov.digital.ho.hocs.casework.security;

import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.api.dto.FieldDto;
import uk.gov.digital.ho.hocs.casework.application.RequestData;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.client.infoclient.PermissionDto;
import uk.gov.digital.ho.hocs.casework.client.infoclient.TeamDto;

import java.nio.BufferUnderflowException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class UserPermissionsService {

    private final RequestData requestData;
    private final InfoClient infoClient;

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
                .filter(e -> e.getCaseTypeCode().equals(caseType))
                .max(Comparator.comparing(PermissionDto::getAccessLevel));
        return maxPermission.orElse(
                new PermissionDto("", AccessLevel.UNSET)
        ).getAccessLevel();
    }

    public Set<UUID> getUserTeams() {
        String[] groups = requestData.groupsArray();

        return Stream.of(groups)
                .map(this::getUUIDFromBase64)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    public boolean isUserInTeam(UUID teamUuid) {
        return getExpandedUserTeams().contains(teamUuid);
    }

    public Set<UUID> getExpandedUserTeams() {
        var userTeamUuids = getUserTeams();

        if (userTeamUuids.isEmpty()) {
            return userTeamUuids;
        }

        var allTeams = infoClient.getTeams();

        var userCaseAdminCaseTypes = getUserCaseAdminCaseTypes(allTeams, userTeamUuids);

        if (userCaseAdminCaseTypes.isEmpty()) {
            return userTeamUuids;
        }

        userTeamUuids.addAll(getTeamsWithCaseAdminPermissions(allTeams, userCaseAdminCaseTypes));

        return userTeamUuids;
    }

    public Set<String> getCaseTypesIfUserTeamIsCaseTypeAdmin() {
        Set<TeamDto> teamDtos = infoClient.getTeams();
        Set<UUID> userTeams = getUserTeams();

        return getUserCaseAdminCaseTypes(teamDtos, userTeams);
    }

    private Set<PermissionDto> getUserPermission() {
        Set<TeamDto> teamDtos = infoClient.getTeams();
        Set<UUID> userTeams = getUserTeams();
        return teamDtos.stream()
                .filter(t -> userTeams.contains(t.getUuid()))
                .flatMap(t -> t.getPermissionDtos().stream())
                .collect(Collectors.toSet());
    }

    private Set<UUID> getTeamsWithCaseAdminPermissions(Set<TeamDto> allTeams, Set<String> userCaseAdminCaseTypes) {
        return allTeams.stream()
                .filter(team -> team.getPermissionDtos().stream()
                        .anyMatch(permissionDto -> userCaseAdminCaseTypes.contains(permissionDto.getCaseTypeCode())))
                .map(TeamDto::getUuid)
                .collect(Collectors.toSet());
    }

    private Set<String> getUserCaseAdminCaseTypes(Set<TeamDto> allTeams, Set<UUID> uuids) {
        var userTeamsPermissions = allTeams.stream()
                .filter(team -> uuids.contains(team.getUuid()))
                .flatMap(team -> team.getPermissionDtos().stream());

        return userTeamsPermissions
                .filter(permission -> permission.getAccessLevel() == AccessLevel.CASE_ADMIN)
                .map(PermissionDto::getCaseTypeCode)
                .collect(Collectors.toSet());
    }

    private UUID getUUIDFromBase64(String uuid) {
        if (uuid.startsWith("/")) {
            uuid = uuid.substring(1);
        }
        try {
            return Base64UUID.base64StringToUUID(uuid);
        } catch (BufferUnderflowException e) {
            return null;
        }
    }

    public List<FieldDto> getRestrictedFieldNames() {
        // todo: is a more case_type_schema option required??? I'm not sure screening on name is restrictive enough.
        return infoClient.getRestrictedFields();
    }
}
