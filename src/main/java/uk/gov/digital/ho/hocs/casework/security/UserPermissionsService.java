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
import static uk.gov.digital.ho.hocs.casework.application.LogEvent.*;

@Service
@Slf4j
public class UserPermissionsService {

    private RequestData requestData;
    private InfoClient infoClient;
//
//    @Autowired
//    public UserPermissionsService(RequestData requestData) {
//        this.requestData = requestData;
//    }
//
//    public UUID getUserId() {
//        return UUID.fromString(requestData.userId());
//    }
//
//    public AccessLevel getMaxAccessLevel(String caseType) {
//        return getUserPermission()
//                .flatMap(type -> type.getValue().getOrDefault(caseType, new HashSet<>()).stream())
//                .max(Comparator.comparing(AccessLevel::getLevel))
//                .orElseThrow(() ->
//                        new SecurityExceptions.PermissionCheckException("User does not have any permissions for this case type", SECURITY_UNAUTHORISED));
//    }
//
//    public Set<AccessLevel> getUserAccessLevels(CaseDataType caseType) {
//
//        return getUserPermission()
//                .flatMap(type -> type.getValue().getOrDefault(caseType.getDisplayCode(), new HashSet<>()).stream())
//                .collect(Collectors.toSet());
//    }
//
//
//    public Set<UUID> getUserTeams() {
//        return getUserPermission()
//                .map(k -> UUID.fromString(k.getKey()))
//                .collect(Collectors.toSet());
//    }
//
//    public Set<String> getUserCaseTypes() {
//        return getUserPermission()
//                .flatMap(team -> team.getValue().entrySet().stream())
//                .map(caseType -> caseType.getKey())
//                .collect(Collectors.toSet());
//    }
//
//    Stream<Map.Entry<String, Map<String, Set<AccessLevel>>>> getUserPermission() {
//        Map<String, Map<String, Set<AccessLevel>>> permissions = new HashMap<>();
//
//        Arrays.stream(requestData.groupsArray())
//                .map(group -> group.split("/"))
//                .filter(group -> group.length >= 4)
//                .forEach(group -> getAccessLevel(permissions, group));
//
//        return permissions.entrySet().stream();
//    }
//
//    private static void getAccessLevel(Map<String, Map<String, Set<AccessLevel>>> permissions, String[] permission) {
//        try {
//            String team = Optional.ofNullable(permission[1]).orElseThrow(() -> new SecurityExceptions.PermissionCheckException("Null team Found", SECURITY_PARSE_ERROR));
//            String caseType = Optional.ofNullable(permission[2]).orElseThrow(() -> new SecurityExceptions.PermissionCheckException("Invalid case type Found",SECURITY_PARSE_ERROR));
//            String accessLevel = Optional.ofNullable(permission[3]).orElseThrow(() -> new SecurityExceptions.PermissionCheckException("Invalid access type Found",SECURITY_PARSE_ERROR));
//
//            UUID decodedTeam = Base64UUID.Base64StringToUUID(team);
//
//            buildPermissions(permissions, decodedTeam.toString(), caseType, accessLevel);
//
//        } catch (SecurityExceptions.PermissionCheckException | BufferUnderflowException e) {
//            log.error(e.getMessage(),value(EVENT, SECURITY_PARSE_ERROR));
//        }
//    }
//
//    private static void buildPermissions(Map<String, Map<String, Set<AccessLevel>>> permissions, String team, String caseType, String accessLevel) {
//        try{
//            permissions.computeIfAbsent(team, map -> new HashMap<>())
//                    .computeIfAbsent(caseType, map -> new HashSet<>())
//                    .add(AccessLevel.from((Integer.valueOf(accessLevel))));
//        } catch (IllegalArgumentException e){
//            log.error("invalid access level found - {}", accessLevel, value(EVENT, INVALID_ACCESS_LEVEL_FOUND));
//        }
//    }

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
                .max(Comparator.comparing(e -> e.getAccessLevel()));
        return maxPermission.orElseThrow(
                () -> new SecurityExceptions.PermissionCheckException("No permissionDtos found for case type",SECURITY_PARSE_ERROR)
        ).getAccessLevel();
    }


    public Set<UUID> getUserTeams() {
        String[] groups = requestData.groupsArray();
        return Arrays.stream(groups)
                .map(group -> getUUIDFromBase64(group))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    public Set<String> getUserCaseTypes() {
        return getUserPermission().stream()
                .map(p -> p.getCaseTypeCode())
                .collect(Collectors.toSet());


    }

    Set<PermissionDto> getUserPermission() {
        Set<TeamDto> teamDtos = infoClient.getTeams();
        Set<UUID> userTeams = getUserTeams();
        Set<PermissionDto> set = teamDtos.stream()
                .filter(t -> userTeams.contains(t.getUuid()))
                .flatMap(t -> t.getPermissionDtos().stream())
                .collect(Collectors.toSet());
        return set;
    }


    private UUID getUUIDFromBase64(String uuid) {
        if(uuid.startsWith("/")) {
            uuid = uuid.substring(1);
        }
        try {
            return Base64UUID.Base64StringToUUID(uuid);
        }
        catch (BufferUnderflowException e) {
            return null;
        }
    }
}