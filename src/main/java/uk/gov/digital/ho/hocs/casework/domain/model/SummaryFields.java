package uk.gov.digital.ho.hocs.casework.domain.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import uk.gov.digital.ho.hocs.casework.domain.repository.JsonConfigFolderReader.CaseTypeObject;

import java.util.List;

public class SummaryFields implements CaseTypeObject<List<SummaryFields.SummaryField>> {

    private final String type;
    private final List<SummaryField> fields;

    @JsonCreator
    public SummaryFields(@JsonProperty("type") String type,
                         @JsonProperty("fields") List<SummaryField> fields) {
        this.type = type;
        this.fields = fields;
    }

    @Override
    public String getType() {
        return type;
    }

    @Override
    public List<SummaryField> getValue() {
        return fields;
    }

    @Getter
    public static class SummaryField {

        private final String name;
        private final String label;
        private final String type;
        private List<ConditionChoices> conditionChoices;
        private Object choices;

        @JsonCreator
        public SummaryField(@JsonProperty("name") String name,
                            @JsonProperty("label") String label,
                            @JsonProperty("type") String type,
                            @JsonProperty("conditionChoices") List<ConditionChoices> conditionChoices,
                            @JsonProperty("choices") Object choices) {
            this.name = name;
            this.label = label;
            this.type = type;

            //TODO: HOCS-5559 raised to discuss implementation of choices with fields and here
            if (conditionChoices != null &&
                    choices != null) {
                throw new IllegalArgumentException("conditionChoices and choices cannot both be specified");
            } else if (conditionChoices != null) {
                this.conditionChoices = conditionChoices;
            } else if (choices != null) {
                this.choices = choices;
            }
        }

        @Getter
        public static class ConditionChoices {
            private final Object choices;
            private final String conditionPropertyName;
            private final String conditionPropertyValue;

            @JsonCreator
            public ConditionChoices(@JsonProperty("conditionPropertyName") String conditionPropertyName,
                                    @JsonProperty("conditionPropertyValue") String conditionPropertyValue,
                                    @JsonProperty("choices") Object choices) {
                this.conditionPropertyName = conditionPropertyName;
                this.conditionPropertyValue = conditionPropertyValue;
                this.choices = choices;
            }
        }

        @Getter
        public static class Choices {
            private final String label;
            private final String value;

            @JsonCreator
            public Choices(@JsonProperty("label") String label,
                           @JsonProperty("value") String value) {
                this.label = label;
                this.value = value;
            }
        }
    }
}
