package uk.gov.digital.ho.hocs.casework.domain.model;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
public class Statistic {

    private UUID teamUuid;
    private Integer count;

    public void setTeamUuid(UUID teamUuid) {
        this.teamUuid = teamUuid;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    public UUID getTeamUuid() {
        return teamUuid;
    }

    public Integer getCount() {
        return count;
    }
}
