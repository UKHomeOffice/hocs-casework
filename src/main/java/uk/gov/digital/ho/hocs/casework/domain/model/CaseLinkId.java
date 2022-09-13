package uk.gov.digital.ho.hocs.casework.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CaseLinkId implements Serializable {

    private UUID primaryCase;

    private UUID secondaryCase;

}
