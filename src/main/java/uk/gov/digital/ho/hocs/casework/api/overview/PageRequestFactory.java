package uk.gov.digital.ho.hocs.casework.api.overview;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import uk.gov.digital.ho.hocs.casework.api.overview.ColumnFilter.FilterType;
import uk.gov.digital.ho.hocs.casework.api.overview.ColumnSort.SortOrder;
import uk.gov.digital.ho.hocs.casework.client.infoclient.CaseTypeDto;
import uk.gov.digital.ho.hocs.casework.client.infoclient.InfoClient;
import uk.gov.digital.ho.hocs.casework.client.infoclient.StageTypeDto;
import uk.gov.digital.ho.hocs.casework.client.infoclient.TeamDto;
import uk.gov.digital.ho.hocs.casework.client.infoclient.UserDto;

@Component
public class PageRequestFactory {

  public static final int MAX_ID_MATCHES = 10;

  protected final InfoClient infoClient;

  public PageRequestFactory(InfoClient infoClient) {
    this.infoClient = infoClient;
  }

  public PageRequest build(int pageNumber, int resultsPerPage, String filterCriteriaString, String sortCriteriaString, Set<CaseTypeDto> permittedCaseTypeDtos) {

    List<ColumnSort> columnSortCriteria = getColumnSorts(sortCriteriaString);
    List<ColumnFilter> columnFilterCriteria = getColumnFilters(filterCriteriaString);
    Set<String> permittedCaseTypes = permittedCaseTypeDtos.stream().map(ct -> ct.getType()).collect(Collectors.toSet());
    return new PageRequest(pageNumber, resultsPerPage, columnSortCriteria, columnFilterCriteria, permittedCaseTypes);
  }

  private List<ColumnSort> getColumnSorts(String sortCriteriaString) {

    List<ColumnSort> columnSorts = new ArrayList<>();

    if (sortCriteriaString != null) {
      String[] fields = sortCriteriaString.split(",");
      for (String field : fields) {
        String[] fieldParams = field.split(":", 2);
        String fieldName = fieldParams[0];
        String sortValue = fieldParams[1];

        if (fieldName.equals("teamName")) {
          columnSorts.add(new ColumnSort("teamUuid", "desc".equals(sortValue) ? SortOrder.DESCENDING : SortOrder.ASCENDING));
          continue;
        }
        if (fieldName.equals("allocatedUserEmail")) {
          columnSorts.add(new ColumnSort("allocatedUserUuid", "desc".equals(sortValue) ? SortOrder.DESCENDING : SortOrder.ASCENDING));
          continue;
        }
        if (fieldName.equals("ownerEmail")) {
          columnSorts.add(new ColumnSort("ownerUuid", "desc".equals(sortValue) ? SortOrder.DESCENDING : SortOrder.ASCENDING));
          continue;
        }
        columnSorts.add(new ColumnSort(fieldName, "desc".equals(sortValue) ? SortOrder.DESCENDING : SortOrder.ASCENDING));
      }
    }

    return columnSorts;
  }

  private List<ColumnFilter> getColumnFilters(String filterCriteriaString) {
    List<ColumnFilter> columnFilters = new ArrayList<>();
    if (filterCriteriaString != null) {
      String[] fields = filterCriteriaString.split(",");
      for (String field : fields) {
        String[] fieldParams = field.split(":", 2);
        String fieldName = fieldParams[0];
        String filterValue = fieldParams[1];

        if (fieldName.equals("teamName")) {
          List<TeamDto> matchedTeams = infoClient.getTeams().stream().filter(t -> t.getDisplayName().toLowerCase(Locale.ROOT).contains(filterValue.toLowerCase())).collect(Collectors.toList());
          if (matchedTeams.size() < MAX_ID_MATCHES) {
            List<String> matchedUuids = matchedTeams.stream().map(t -> t.getUuid().toString()).collect(Collectors.toList());
            String csvJoined = String.join(",", matchedUuids);
            columnFilters.add(new ColumnFilter("teamUuid", csvJoined, FilterType.IN));
          }
          continue;
        }

        if (fieldName.equals("allocatedUserEmail")) {
          List<UserDto> matchedUsers = infoClient.getUsers().stream().filter(t -> t.getEmail().toLowerCase(Locale.ROOT).contains(filterValue.toLowerCase())).collect(Collectors.toList());
          if (matchedUsers.size() < MAX_ID_MATCHES) {
            List<String> matchedUuids = matchedUsers.stream().map(UserDto::getId).collect(Collectors.toList());
            String csvJoined = String.join(",", matchedUuids);
            columnFilters.add(new ColumnFilter("allocatedUserUuid", csvJoined, FilterType.IN));
          }
          continue;
        }

        if (fieldName.equals("ownerEmail")) {
          List<UserDto> matchedUsers = infoClient.getUsers().stream().filter(t -> t.getEmail().toLowerCase(Locale.ROOT).contains(filterValue.toLowerCase())).collect(Collectors.toList());
          if (matchedUsers.size() < MAX_ID_MATCHES) {
            List<String> matchedUuids = matchedUsers.stream().map(UserDto::getId).collect(Collectors.toList());
            String csvJoined = String.join(",", matchedUuids);
            columnFilters.add(new ColumnFilter("ownerUuid", csvJoined, FilterType.IN));
          }
          continue;
        }

        if (fieldName.equals("stageType")) { List<StageTypeDto> matchedStageTypes = infoClient.getStageTypes().stream().filter(st -> st.getDisplayName().toLowerCase(Locale.ROOT).contains(filterValue.toLowerCase())).collect(Collectors.toList());
          if (matchedStageTypes.size() < MAX_ID_MATCHES) {
            List<String> matchedTypes = matchedStageTypes.stream().map(StageTypeDto::getType).collect(Collectors.toList());
            String csvJoined = String.join(",", matchedTypes);
            columnFilters.add(new ColumnFilter("stageType", csvJoined, FilterType.IN));
          }
          continue;
        }

        if (fieldName.equals("reference")) {
          columnFilters.add(new ColumnFilter(fieldName, filterValue, FilterType.CASE_INSENSITIVE_LIKE));
          continue;
        }

        columnFilters.add(new ColumnFilter(fieldName, filterValue, FilterType.EQUALS));
      }
    }
    return columnFilters;
  }

}
