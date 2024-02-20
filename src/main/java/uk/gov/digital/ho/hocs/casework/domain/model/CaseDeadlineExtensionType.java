package uk.gov.digital.ho.hocs.casework.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "case_deadline_extension_type")
// todo: remove
public class CaseDeadlineExtensionType {

    @Id
    @Getter
    @Column(name = "type")
    String type;

    @Getter
    @Column(name = "working_days")
    int workingDays;

}