package uk.gov.digital.ho.hocs.casework.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;
import java.util.Objects;

@NoArgsConstructor
@AllArgsConstructor
@Getter

@Entity(name = "CaseDeadlineExtension")
@Table(name = "case_deadline_extension")
public class CaseDeadlineExtension {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_uuid", referencedColumnName="uuid")
    private CaseData caseData;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type")
    private CaseDeadlineExtensionType caseDeadlineExtensionType;

    @Column(name = "created")
    private Date created = new Date();

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;

        if (other == null || getClass() != other.getClass())
            return false;

        CaseDeadlineExtension otherCaseDeadlineExtension = (CaseDeadlineExtension) other;
        return Objects.equals(caseData, otherCaseDeadlineExtension.caseData) &&
                Objects.equals(caseDeadlineExtensionType, otherCaseDeadlineExtension.caseDeadlineExtensionType);
    }

    public CaseDeadlineExtension(CaseData caseData, CaseDeadlineExtensionType caseDeadlineExtensionType) {
        this.caseData = caseData;
        this.caseDeadlineExtensionType = caseDeadlineExtensionType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(caseData, caseDeadlineExtensionType);
    }
}
