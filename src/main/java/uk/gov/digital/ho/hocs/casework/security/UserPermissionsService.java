package uk.gov.digital.ho.hocs.casework.security;

import com.amazonaws.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.digital.ho.hocs.casework.application.RequestData;
import uk.gov.digital.ho.hocs.casework.domain.model.CaseDataType;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserPermissionsService {

    private RequestData requestData;

    @Autowired
    public UserPermissionsService(RequestData requestData) {
        this.requestData = requestData;
    }

    public UUID getUserId() {
        return UUID.fromString(requestData.userId());
    }

    public AccessLevel getMaxAccessLevel(CaseDataType caseType) {

        return getUserAccessLevels(caseType).stream()
                .max(Comparator.comparing(level -> level.getLevel()))
                .orElseThrow(() ->
                        new SecurityExceptions.PermissionCheckException("User does not have any permissions for this case type"));
    }

    public Set<AccessLevel> getUserAccessLevels(CaseDataType caseType) {

        return  getUserPermission().entrySet().stream()
                .flatMap(unit -> unit.getValue().values().stream())
                .flatMap(type -> type.getOrDefault(caseType, new HashSet<>()).stream())
                .collect(Collectors.toSet());
    }

    public Set<String> getUserUnits() {
        return getUserPermission().entrySet().stream()
                .map(unit -> unit.getKey())
                .collect(Collectors.toSet());
    }

    public Set<UUID> getUserTeams() {
      return getUserPermission().entrySet().stream()
                .flatMap(unit -> unit.getValue().entrySet().stream())
                .map(team -> UUID.fromString(team.getKey()))
                .collect(Collectors.toSet());
    }

    public Set<CaseDataType> getUserCaseTypes() {
        return getUserPermission().entrySet().stream()
                .flatMap(unit -> unit.getValue().values().stream())
                .flatMap(team -> team.entrySet().stream())
                .map(caseType -> caseType.getKey())
                .collect(Collectors.toSet());
    }

    public Map<String, Map<String, Map<CaseDataType,Set<AccessLevel>>>> getUserPermission() {
        List<List<String>> groups = Arrays.stream(requestData.groups().split(","))
                .map(g -> Arrays.asList(g.split("/")))
                .collect(Collectors.toList());

        Map<String, Map<String, Map<CaseDataType,Set<AccessLevel>>>> permissions = new HashMap<>();
        for(List<String> permission : groups) {
            if(permission.size() > 4) {
                getAccessLevel(permissions, permission);
            }
        }
        return permissions;
    }

    private void getAccessLevel(Map<String, Map<String, Map<CaseDataType, Set<AccessLevel>>>> permissions, List<String> permission) {
        try {
            String unit ="";
            if (!StringUtils.isNullOrEmpty(permission.get(1))) {
                unit = Optional.ofNullable(permission.get(1)).orElseThrow(() ->new SecurityExceptions.PermissionCheckException("Null unit Found"));
            }

            String team="";
            if (!StringUtils.isNullOrEmpty(permission.get(2))) {
                team = Optional.ofNullable(permission.get(2)).orElseThrow(() ->new SecurityExceptions.PermissionCheckException("Null team Found"));
            }

            CaseDataType type = null;
            if (!StringUtils.isNullOrEmpty(permission.get(3))) {
                String caseType = Optional.ofNullable(permission.get(3)).orElseThrow(() ->new SecurityExceptions.PermissionCheckException("Invalid case type Found"));
                type = CaseDataType.valueOf(caseType);
            }

            AccessLevel level = null;
            if (!StringUtils.isNullOrEmpty(permission.get(4))) {
                String accessLevel = Optional.ofNullable(permission.get(4)).orElseThrow(() ->new SecurityExceptions.PermissionCheckException("Invalid access type Found"));
                level = AccessLevel.valueOf(accessLevel);
            }

            if (!permissions.containsKey(unit)) {
                permissions.put(unit, new HashMap<>());
            }

            if (!permissions.get(unit).containsKey(team)) {
                permissions.get(unit).put(team, new HashMap<>());
            }

            if (!permissions.get(unit).get(team).containsKey(type)) {
                permissions.get(unit).get(team).put(type, new HashSet<>());
            }

            permissions.get(unit).get(team).get(type).add(level);

        }
        catch(SecurityExceptions.PermissionCheckException e) {
            log.error(e.getMessage());
        }
    }


}
