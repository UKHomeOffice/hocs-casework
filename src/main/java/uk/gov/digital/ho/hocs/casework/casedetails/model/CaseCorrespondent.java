package uk.gov.digital.ho.hocs.casework.casedetails.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "case_correspondent")
public class CaseCorrespondent {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Getter
    @Column(name = "case_uuid")
    private UUID caseUUID;

    @Getter
    @Column(name = "correspondent_uuid")
    private UUID correspondentUUID;

    @Getter
    @Column(name = "type")
    private String type;

    public CaseCorrespondent(UUID caseUUID, UUID correspondentUUID, CorrespondentType type) {
        this.caseUUID = caseUUID;
        this.correspondentUUID = correspondentUUID;
        this.type = type.toString();
    }

    public void update(CorrespondentType correspondentType) {
        this.type = correspondentType.toString();
    }
}
