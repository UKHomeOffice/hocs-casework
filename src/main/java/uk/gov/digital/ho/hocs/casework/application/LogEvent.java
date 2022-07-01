package uk.gov.digital.ho.hocs.casework.application;

public enum LogEvent {
    CASE_RETRIEVED,
    CASE_CREATED,
    CASE_CREATE_FAILURE,
    CASE_NOT_FOUND,
    CASE_UPDATED,
    CASE_NOT_UPDATED_NULL_DATA,
    CASE_COMPLETED,
    CASE_WITHDRAWN,
    CASE_DELETED,
    CASE_SUMMARY_RETRIEVED,
    CASE_SUMMARY_CANNOT_PARSE_SOMU_ITEM,
    CASE_TYPE_LOOKUP_FAILED,
    CASE_DATA_JSON_PARSE_ERROR,
    CASE_TOPICS_RETRIEVED,
    CASE_TOPIC_RETRIEVED,
    CASE_TOPIC_NOT_FOUND,
    CASE_TOPIC_CREATE,
    CASE_TOPIC_UUID_NOT_GIVEN,
    CASE_TOPIC_DELETED,
    TOPIC_CREATE_FAILED,
    STAGE_CREATE_FAILURE,
    STAGE_NOT_FOUND,
    STAGES_NOT_FOUND,
    STAGE_CREATED,
    STAGE_RECREATED,
    STAGE_DEADLINE_UPDATED,
    STAGE_ASSIGNED_TEAM,
    STAGE_ASSIGNED_USER,
    STAGE_ASSIGNED_USER_FAILURE,
    STAGE_TRANSITION_NOTE_UPDATED,
    SEARCH_STAGE_LIST_EMPTY,
    SEARCH_STAGE_LIST_RETRIEVED,
    TEAMS_STAGE_LIST_EMPTY,
    TEAMS_STAGE_LIST_RETRIEVED,
    USERS_TEAMS_STAGE_LIST_RETRIEVED,
    STAGE_COMPLETED,
    CASE_NOTE_CREATED,
    CASE_NOTE_CREATE_FAILURE,
    CASE_NOTE_UPDATED,
    CASE_NOTE_DELETED,
    CASE_NOTE_RETRIEVED,
    CASE_NOTE_NOT_FOUND,
    SOMU_ITEM_CREATED,
    SOMU_ITEM_UPDATED,
    SOMU_ITEM_DELETED,
    SOMU_ITEM_RETRIEVED,
    SOMU_ITEM_NOT_FOUND,
    CASE_DOCUMENTS_RETRIEVED,
    CASE_DOCUMENT_RETRIEVED,
    CASE_DOCUMENT_PDF_RETRIEVED,
    CORRESPONDENTS_RETRIEVED,
    CORRESPONDENT_RETRIEVED,
    CORRESPONDENT_NOT_FOUND,
    CORRESPONDENT_CREATE_FAILURE,
    CORRESPONDENT_UPDATE_FAILURE,
    CORRESPONDENT_CREATED,
    CORRESPONDENT_UPDATED,
    CORRESPONDENT_DELETED,
    DOCUMENT_CLIENT_GET_DOCUMENTS_SUCCESS,
    DOCUMENT_CLIENT_GET_DOCUMENT_DTO_SUCCESS,
    DOCUMENT_CLIENT_GET_DOCUMENT_SUCCESS,
    DOCUMENT_CLIENT_DELETE_DOCUMENT_SUCCESS,
    DOCUMENT_CLIENT_GET_DOCUMENT_PDF_SUCCESS,
    UNCAUGHT_EXCEPTION,
    SECURITY_PARSE_ERROR,
    SECURITY_UNAUTHORISED,
    SECURITY_FORBIDDEN,
    SECURITY_CASE_NOT_ALLOCATED_TO_USER,
    SECURITY_CASE_NOT_ALLOCATED_TO_TEAM,
    AUDIT_EVENT_CREATED,
    AUDIT_FAILED,
    AUDIT_CLIENT_DELETE_AUDITS_FOR_CASE_SUCCESS,
    AUDIT_CLIENT_DELETE_AUDITS_FOR_CASE_FAILURE,
    AUDIT_CLIENT_GET_AUDITS_FOR_CASE_SUCCESS,
    AUDIT_CLIENT_GET_AUDITS_FOR_CASE_FAILURE,
    SEARCH_CLIENT_SEARCH_SUCCESS,
    INFO_CLIENT_GET_CASE_TYPES_SUCCESS,
    INFO_CLIENT_GET_CASE_TYPE_SHORT_SUCCESS,
    INFO_CLIENT_GET_CASE_TYPE_SUCCESS,
    INFO_CLIENT_GET_CASE_CONFIG_SUCCESS,
    INFO_CLIENT_GET_TOPIC_SUCCESS,
    INFO_CLIENT_GET_STANDARD_LINE_SUCCESS,
    INFO_CLIENT_GET_STANDARD_LINES_SUCCESS,
    INFO_CLIENT_GET_TEMPLATE_SUCCESS,
    INFO_CLIENT_GET_TEMPLATES_SUCCESS,
    INFO_CLIENT_GET_SUMMARY_FIELDS_SUCCESS,
    INFO_CLIENT_GET_ALL_SOMU_TYPES_FOR_CASE_TYPE,
    INFO_CLIENT_GET_USERS_FOR_TEAM_SUCCESS,
    INFO_CLIENT_GET_DEFAULT_USERS_FOR_STAGE_SUCCESS,
    INFO_CLIENT_GET_CASE_DEADLINE_SUCCESS,
    INFO_CLIENT_GET_CONTRIBUTIONS_SUCCESS,
    INFO_CLIENT_GET_STAGE_DEADLINE_SUCCESS,
    INFO_CLIENT_GET_STAGE_DEADLINE_WARNING_SUCCESS,
    INFO_CLIENT_GET_DEADLINES_SUCCESS,
    INFO_CLIENT_GET_CONTACTS_SUCCESS,
    INFO_CLIENT_GET_USER_SUCCESS,
    INFO_CLIENT_GET_TEAMS_SUCCESS,
    INFO_CLIENT_GET_PRIORITY_POLICIES_SUCCESS,
    INFO_CLIENT_GET_WORKING_DAYS_FOR_CASE_TYPE_SUCCESS,
    INFO_CLIENT_GET_USER,
    INFO_CLIENT_GET_ENTITY_LIST,
    INFO_CLIENT_GET_ENTITY_BY_SIMPLE_NAME,
    INFO_CLIENT_GET_PROFILE_BY_CASE_TYPE_SUCCESS,
    INFO_CLIENT_GET_BANK_HOLIDAYS_BY_CASE_TYPE_SUCCESS,
    CACHE_PRIME_FAILED,
    CALCULATED_TOTALS,
    PRIMARY_CORRESPONDENT_UPDATED,
    PRIMARY_TOPIC_UPDATED,
    REFRESH_BANK_HOLIDAYS,
    REST_HELPER_POST,
    REST_HELPER_POST_FAILURE,
    REST_HELPER_DELETE,
    REST_HELPER_GET,
    REST_HELPER_GET_UNAUTHORIZED,
    REST_HELPER_GET_FORBIDDEN,
    REST_HELPER_GET_NOT_FOUND,
    REST_HELPER_GET_BAD_REQUEST,
    TEAM_EMAIL_SENT,
    OFFLINE_QA_EMAIL_SENT,
    NOTIFY_EMAIL_FAILED,
    TOPIC_STANDARD_LINE_CACHE_INVALIDATED,
    CASE_TYPE_TEMPLATE_CACHE_INVALIDATED,
    ALL_CASE_TOPICS_RETRIEVED,
    EXTENSION_APPLIED,
    EXTENSION_APPLY_FAILED,
    ACTION_DATA_CREATE_SUCCESS,
    ACTION_DATA_CREATE_FAILURE,
    ACTION_DATA_UPDATE_SUCCESS,
    ACTION_DATA_UPDATE_FAILURE,
    INFO_CLIENT_REMAINING_DAYS_FOR_CASE_TYPE_AND_DEADLINE_SUCCESS,
    USER_EMAIL_SENT,
    GET_CASE_REF_BY_UUID,
    GET_CASE_REF_BY_UUID_FAILURE,
    INFO_CLIENT_GET_TEAM_FOR_STAGE_SUCCESS,
    MISSING_TEAM_FOR_STAGE,
    INFO_CLIENT_GET_FIELDS_BY_PERMISSION_SUCCESS,
    AUTH_FILTER_FAILURE,
    AUTH_FILTER_SUCCESS,
    DATA_MAPPING_EXCEPTION,
    DATA_MAPPING_SUCCESS,
    CONFIG_PARSE_FAILURE;

    public static final String EVENT = "event_id";
    public static final String EXCEPTION = "exception";
    public static final String STACKTRACE = "stacktrace";
}
