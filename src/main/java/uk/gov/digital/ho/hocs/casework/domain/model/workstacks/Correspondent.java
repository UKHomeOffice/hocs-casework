package uk.gov.digital.ho.hocs.casework.domain.model.workstacks;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity(name = "WorkstackCorrespondent")
@Table(name = "correspondent")
public class Correspondent implements Serializable {

    @Id
    @Getter
    @Column(name = "uuid", columnDefinition = "uuid")
    protected UUID uuid;

    @Setter
    @Getter
    @Column(name = "created")
    protected LocalDateTime created;

    @Getter
    @Column(name = "type")
    protected String correspondentType;

    @Getter
    @Setter
    @Transient
    protected String correspondentTypeName;

    @Getter
    @Column(name = "case_uuid")
    protected UUID caseUUID;

    @Setter
    @Getter
    @Column(name = "fullname")
    protected String fullName;

    @Setter
    @Getter
    @Column(name = "organisation")
    protected String organisation;

    @Setter
    @Getter
    @Column(name = "postcode")
    protected String postcode;

    @Setter
    @Getter
    @Column(name = "address1")
    protected String address1;

    @Setter
    @Getter
    @Column(name = "address2")
    protected String address2;

    @Setter
    @Getter
    @Column(name = "address3")
    protected String address3;

    @Setter
    @Getter
    @Column(name = "country")
    protected String country;

    @Setter
    @Getter
    @Column(name = "telephone")
    protected String telephone;

    @Setter
    @Getter
    @Column(name = "email")
    protected String email;

    @Getter
    @Column(name = "external_key")
    protected String externalKey;

    @Setter
    @Getter
    @Column(name = "reference")
    protected String reference;

    @Setter
    @Getter
    @Column(name = "deleted")
    protected boolean deleted;

    @Override
    public boolean equals(Object o) {
        if (this==o) {return true;}
        if (o==null || getClass()!=o.getClass()) {return false;}
        Correspondent that = (Correspondent) o;
        return uuid.equals(that.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }

}
