package uk.gov.digital.ho.hocs.casework.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "case_deadline_extension_type")
public class CaseDeadlineExtensionType {

    @Id
    @Getter
    @Column(name = "type")
    String type;

    @Getter
    @Column(name = "working_days")
    int workingDays;
}