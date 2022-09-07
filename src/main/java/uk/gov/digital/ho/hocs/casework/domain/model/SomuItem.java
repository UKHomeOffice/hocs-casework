package uk.gov.digital.ho.hocs.casework.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "somu_item")
public class SomuItem implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    public SomuItem(UUID uuid, UUID caseUuid, UUID somuUuid, String data) {
        this.uuid = uuid;
        this.caseUuid = caseUuid;
        this.somuUuid = somuUuid;
        this.data = data;
    }

    public boolean isDeleted() {
        return (data == null);
    }

}
