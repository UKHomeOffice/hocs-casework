package uk.gov.digital.ho.hocs.casework.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.Objects;

@NoArgsConstructor
@AllArgsConstructor
@Getter

@Entity(name = "CaseDeadlineExtension")
@Table(name = "case_deadline_extension")
// todo: remove
public class CaseDeadlineExtension {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "case_uuid", referencedColumnName = "uuid")
    private CaseData caseData;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "type")
    private CaseDeadlineExtensionType caseDeadlineExtensionType;

    @Getter
    @Column(name = "created")
    private LocalDateTime created = LocalDateTime.now();

    ;

    @Getter
    @Setter
    @Column(name = "note")
    private String note;

    public CaseDeadlineExtension(CaseData caseData, CaseDeadlineExtensionType caseDeadlineExtensionType, String note) {
        this.caseData = caseData;
        this.caseDeadlineExtensionType = caseDeadlineExtensionType;
        this.note = note;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {return true;}

        if (other == null || getClass() != other.getClass()) {return false;}

        CaseDeadlineExtension otherCaseDeadlineExtension = (CaseDeadlineExtension) other;
        return Objects.equals(caseData, otherCaseDeadlineExtension.caseData) && Objects.equals(
            caseDeadlineExtensionType, otherCaseDeadlineExtension.caseDeadlineExtensionType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(caseData, caseDeadlineExtensionType);
    }

}
