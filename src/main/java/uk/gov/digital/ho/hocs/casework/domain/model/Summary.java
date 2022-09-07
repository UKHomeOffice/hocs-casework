package uk.gov.digital.ho.hocs.casework.domain.model;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
public class Summary {

    private UUID teamUuid;

    private Integer count;

    public UUID getTeamUuid() {
        return teamUuid;
    }

    public void setTeamUuid(UUID teamUuid) {
        this.teamUuid = teamUuid;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

}
