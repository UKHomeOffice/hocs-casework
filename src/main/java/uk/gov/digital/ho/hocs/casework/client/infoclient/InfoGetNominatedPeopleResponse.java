package uk.gov.digital.ho.hocs.casework.client.infoclient;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.Set;

@Getter
public class InfoGetNominatedPeopleResponse {

    @JsonProperty("nominatedPeople")
    private Set<InfoNominatedPeople> nominatedPeople;
}
