package uk.gov.digital.ho.hocs.casework.domain.model;

 import lombok.AllArgsConstructor;

 @AllArgsConstructor
public class HocsFormProperty {

     public String name;

     public String label;

     public HocsFormFieldKVP[] choices;
}