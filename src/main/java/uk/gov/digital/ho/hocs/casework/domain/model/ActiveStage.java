package uk.gov.digital.ho.hocs.casework.domain.model;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@Entity
@Table(name = "active_stage")
public class ActiveStage implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Getter
    @Column(name = "uuid", columnDefinition = "uuid")
    private UUID uuid;

    @Getter
    @Column(name = "created")
    private LocalDateTime created;

    @Getter
    @Column(name = "type")
    private String stageType;

    @Getter
    @Column(name = "deadline")
    private LocalDate deadline;

    @Getter
    @Column(name = "transition_note_uuid", columnDefinition = "uuid")
    private UUID transitionNoteUUID;

    @Getter
    @Column(name = "case_uuid", columnDefinition = "uuid")
    private UUID caseUUID;

    @Getter
    @Column(name = "team_uuid", columnDefinition = "uuid")
    private UUID teamUUID;

    @Getter
    @Column(name = "user_uuid", columnDefinition = "uuid")
    private UUID userUUID;
}