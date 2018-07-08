package uk.gov.digital.ho.hocs.casework.audit.model;

import lombok.Getter;

public enum AuditAction {
    CREATE_CASE(0),
    CREATE_STAGE(1),
    UPDATE_CASE(2),
    UPDATE_STAGE(3),
    GET_CASE(4),
    SEARCH(5),
    SEND_EMAIL(6),
    CSV_EXTRACT(7),
    ADD_DOCUMENT(8),
    DELETE_DOCUMENT(9),
    UNDELETE_DOCUMENT(10),
    UPDATE_DOCUMENT(11);

    @Getter
    private int intValue;

    AuditAction(int intValue) {
        this.intValue = intValue;
    }
}
