package uk.gov.digital.ho.hocs.casework.api.utils;

import uk.gov.digital.ho.hocs.casework.api.dto.CaseDataType;

public class CaseDataTypeFactory {

    /**
     * Convenience constructor scoped to test classes.
     * @param type
     * @param shortCode
     * @return
     */
    public static CaseDataType from(String type, String shortCode) {
        return new CaseDataType( type, shortCode, type, null, 20, 15);
    }

    /**
     * Convenience constructor scoped to test classes.
     * @param type
     * @param shortCode
     * @param previousCaseType
     * @return
     */
    public static CaseDataType from(String type, String shortCode, String previousCaseType) {
        return new CaseDataType( type, shortCode, type, previousCaseType, 20, 15);
    }
}
