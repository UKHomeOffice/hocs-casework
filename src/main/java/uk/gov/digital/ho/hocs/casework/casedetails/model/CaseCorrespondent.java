package uk.gov.digital.ho.hocs.casework.casedetails.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.UUID;

import static java.lang.Boolean.FALSE;

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

    @Column(name = "deleted")
    @Setter
    private Boolean deleted = FALSE;


    public CaseCorrespondent(UUID caseUUID, UUID correspondentUUID, CorrespondentType type) {
        this.caseUUID = caseUUID;
        this.correspondentUUID = correspondentUUID;
        this.type = type.toString();
    }

    public void update(CorrespondentType correspondentType) {
        this.type = correspondentType.toString();
        this.deleted = false;
    }

    public void delete() {
        this.deleted = Boolean.TRUE;

    }

}
