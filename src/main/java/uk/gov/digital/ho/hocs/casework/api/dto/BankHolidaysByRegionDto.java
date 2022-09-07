package uk.gov.digital.ho.hocs.casework.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class BankHolidaysByRegionDto {

    private List<Event> events;

    @AllArgsConstructor
    @NoArgsConstructor
    @Setter
    @Getter
    public static class Event {

        private String date;

    }

}
