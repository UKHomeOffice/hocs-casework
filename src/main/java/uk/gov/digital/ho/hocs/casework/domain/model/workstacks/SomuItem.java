package uk.gov.digital.ho.hocs.casework.domain.model.workstacks;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Entity(name = "WorkstackSomuItem")
@Table(name = "somu_item")
public class SomuItem implements Serializable {

    @Id
    @Getter
    @Column(name = "uuid")
    private UUID uuid;

    @Getter
    @Setter
    @Column(name = "case_uuid")
    private UUID caseUuid;

    @Getter
    @Setter
    @Column(name = "somu_uuid")
    private UUID somuUuid;

    @Getter
    @Setter
    @Column(name = "data")
    private String data;

    public boolean isDeleted() {
        return (data==null);
    }

    @Override
    public boolean equals(Object o) {
        if (this==o) {return true;}
        if (o==null || getClass()!=o.getClass()) {return false;}
        SomuItem somuItem = (SomuItem) o;
        return uuid.equals(somuItem.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }

}
