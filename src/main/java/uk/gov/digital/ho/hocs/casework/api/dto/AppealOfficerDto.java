package uk.gov.digital.ho.hocs.casework.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class AppealOfficerDto {

    @AllArgsConstructor
    @Getter
    @NoArgsConstructor
    public static class OfficerData {

        OfficerFieldSchemaDto appealOfficerData;

    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class OfficerFieldSchemaDto {

        private Field officer;

        private Field directorate;

    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public static class Field {

        private String label;

        private String value;

        private String choices;

    }

}
