package uk.gov.digital.ho.hocs.casework.domain.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor
@Entity
@Table(name = "case_link")
@IdClass(CaseLinkId.class)
public class CaseLink {

    @Getter
    @Id
    @Column(name = "primary_case_uuid")
    private UUID primaryCase;

    @Getter
    @Id
    @Column(name = "secondary_case_uuid")
    private UUID secondaryCase;

}
