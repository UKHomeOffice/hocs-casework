package uk.gov.digital.ho.hocs.casework.application;

public enum LogEvent {
    ACTION_DATA_CREATE_FAILURE,
    ACTION_DATA_CREATE_SUCCESS,
    ACTION_DATA_UPDATE_FAILURE,
    ACTION_DATA_UPDATE_SUCCESS,
    ALL_CASE_TOPICS_RETRIEVED,
    AUDIT_CLIENT_DELETE_AUDITS_FOR_CASE_FAILURE,
    AUDIT_CLIENT_DELETE_AUDITS_FOR_CASE_SUCCESS,
    AUDIT_CLIENT_GET_AUDITS_FOR_CASE_FAILURE,
    AUDIT_CLIENT_GET_AUDITS_FOR_CASE_SUCCESS,
    AUDIT_EVENT_CREATED,
    AUDIT_FAILED,
    AUTH_FILTER_FAILURE,
    AUTH_FILTER_SUCCESS,
    CACHE_PRIME_FAILED,
    CALCULATED_TOTALS,
    CASE_COMPLETED,
    CASE_CREATED,
    CASE_CREATE_FAILURE,
    CASE_DATA_DETAILS_NOT_FOUND,
    CASE_DATA_JSON_PARSE_ERROR,
    CASE_DATA_DOCUMENT_TAG_NOT_FOUND,
    CASE_DELETED,
    CASE_DOCUMENTS_RETRIEVED,
    CASE_DOCUMENT_PDF_RETRIEVED,
    CASE_DOCUMENT_RETRIEVED,
    CASE_NOTE_CREATED,
    CASE_NOTE_CREATE_FAILURE,
    CASE_NOTE_DELETED,
    CASE_NOTE_NOT_FOUND,
    CASE_NOTE_RETRIEVED,
    CASE_NOTE_UPDATED,
    CASE_NOT_FOUND,
    CASE_NOT_UPDATED_NULL_DATA,
    CASE_RETRIEVED,
    CASE_SUMMARY_CANNOT_PARSE_SOMU_ITEM,
    CASE_SUMMARY_RETRIEVED,
    CASE_TAG_CONFLICT,
    CASE_TOPICS_RETRIEVED,
    CASE_TOPIC_CREATE,
    CASE_TOPIC_DELETED,
    CASE_TOPIC_NOT_FOUND,
    CASE_TOPIC_RETRIEVED,
    CASE_TOPIC_UUID_NOT_GIVEN,
    CASE_TYPE_LOOKUP_FAILED,
    CASE_TYPE_TEMPLATE_CACHE_INVALIDATED,
    CASE_UPDATED,
    CASE_WITHDRAWN,
    CONFIG_FOLDER_NOT_FOUND_FAILURE,
    CONFIG_PARSE_FAILURE,
    CORRESPONDENTS_RETRIEVED,
    CORRESPONDENT_CREATED,
    CORRESPONDENT_CREATE_FAILURE,
    CORRESPONDENT_DELETED,
    CORRESPONDENT_NOT_FOUND,
    CORRESPONDENT_RETRIEVED,
    CORRESPONDENT_UPDATED,
    CORRESPONDENT_UPDATE_FAILURE,
    DATA_MAPPING_EXCEPTION,
    DATA_MAPPING_SUCCESS,
    DOCUMENT_CLIENT_DELETE_DOCUMENT_SUCCESS,
    DOCUMENT_CLIENT_GET_DOCUMENTS_SUCCESS,
    DOCUMENT_CLIENT_GET_DOCUMENT_DTO_SUCCESS,
    DOCUMENT_CLIENT_GET_DOCUMENT_PDF_SUCCESS,
    DOCUMENT_CLIENT_GET_DOCUMENT_SUCCESS,
    DOCUMENT_CLIENT_CREATE_SUCCESS,
    EXTENSION_APPLIED,
    EXTENSION_APPLY_FAILED,
    GET_CASE_REF_BY_UUID,
    GET_CASE_REF_BY_UUID_FAILURE,
    INFO_CLIENT_GET_ALL_SOMU_TYPES_FOR_CASE_TYPE,
    INFO_CLIENT_GET_BANK_HOLIDAYS_BY_CASE_TYPE_SUCCESS,
    INFO_CLIENT_GET_CASE_CONFIG_SUCCESS,
    INFO_CLIENT_GET_CASE_DEADLINE_SUCCESS,
    INFO_CLIENT_GET_CASE_TYPES_SUCCESS,
    INFO_CLIENT_GET_CASE_TYPE_SHORT_SUCCESS,
    INFO_CLIENT_GET_CASE_TYPE_SUCCESS,
    INFO_CLIENT_GET_CONTACTS_SUCCESS,
    INFO_CLIENT_GET_CONTRIBUTIONS_SUCCESS,
    INFO_CLIENT_GET_DEADLINES_SUCCESS,
    INFO_CLIENT_GET_DEFAULT_USERS_FOR_STAGE_SUCCESS,
    INFO_CLIENT_GET_ENTITY_BY_SIMPLE_NAME,
    INFO_CLIENT_GET_ENTITY_LIST,
    INFO_CLIENT_GET_FIELDS_BY_PERMISSION_SUCCESS,
    INFO_CLIENT_GET_PRIORITY_POLICIES_SUCCESS,
    INFO_CLIENT_GET_PROFILE_BY_CASE_TYPE_SUCCESS,
    INFO_CLIENT_GET_ALL_STAGE_TYPES,
    INFO_CLIENT_GET_STAGE_TYPE_BY_TYPE_STRING,
    INFO_CLIENT_GET_STAGE_DEADLINE_SUCCESS,
    INFO_CLIENT_GET_STAGE_DEADLINE_WARNING_SUCCESS,
    INFO_CLIENT_GET_STANDARD_LINES_SUCCESS,
    INFO_CLIENT_GET_STANDARD_LINE_SUCCESS,
    INFO_CLIENT_GET_SUMMARY_FIELDS_SUCCESS,
    INFO_CLIENT_GET_TEAMS_SUCCESS,
    INFO_CLIENT_GET_TEAM_BY_UUID_SUCCESS,
    INFO_CLIENT_GET_TEAM_FOR_STAGE_SUCCESS,
    INFO_CLIENT_GET_TEMPLATES_SUCCESS,
    INFO_CLIENT_GET_TEMPLATE_SUCCESS,
    INFO_CLIENT_GET_TOPIC_SUCCESS,
    INFO_CLIENT_GET_USER,
    INFO_CLIENT_GET_ALL_USERS,
    INFO_CLIENT_GET_USERS_FOR_TEAM_SUCCESS,
    INFO_CLIENT_GET_USER_SUCCESS,
    INFO_CLIENT_GET_WORKING_DAYS_FOR_CASE_TYPE_SUCCESS,
    INFO_CLIENT_REMAINING_DAYS_FOR_CASE_TYPE_AND_DEADLINE_SUCCESS,
    MIGRATION_CASE_NOT_FOUND,
    MIGRATION_CASE_NOT_UPDATED_NULL_DATA,
    MIGRATION_CASE_RETRIEVED,
    MIGRATION_CASE_UPDATED,
    MISSING_TEAM_FOR_STAGE,
    NOTIFY_EMAIL_FAILED,
    OFFLINE_QA_EMAIL_SENT,
    PRIMARY_CORRESPONDENT_UPDATED,
    PRIMARY_TOPIC_UPDATED,
    REFRESH_BANK_HOLIDAYS,
    REPORT_MAPPER_USER_CACHE_REFRESH,
    REPORT_MAPPER_USER_CACHE_ERROR,
    REPORT_MAPPER_TEAM_CACHE_REFRESH,
    REPORT_MAPPER_TEAM_CACHE_ERROR,
    REPORT_MAPPER_STAGE_CACHE_REFRESH,
    REPORT_MAPPER_STAGE_CACHE_ERROR,
    REPORT_MAPPER_EXEMPTION_DATE_CACHE_REFRESH,
    REPORT_MAPPER_EXEMPTION_DATE_CACHE_ERROR,
    REPORT_RESOURCE_FAILED_TO_STREAM_BODY,
    REPORT_RESOURCE_UNSUPPORTED_CASE_TYPE,
    REST_HELPER_DELETE,
    REST_HELPER_GET,
    REST_HELPER_GET_BAD_REQUEST,
    REST_HELPER_GET_FORBIDDEN,
    REST_HELPER_GET_NOT_FOUND,
    REST_HELPER_GET_UNAUTHORIZED,
    REST_HELPER_POST,
    REST_HELPER_POST_FAILURE,
    SEARCH_CLIENT_SEARCH_SUCCESS,
    SEARCH_STAGE_LIST_EMPTY,
    SEARCH_STAGE_LIST_RETRIEVED,
    SECURITY_CASE_NOT_ALLOCATED_TO_TEAM,
    SECURITY_CASE_NOT_ALLOCATED_TO_USER,
    SECURITY_FORBIDDEN,
    SECURITY_PARSE_ERROR,
    SECURITY_UNAUTHORISED,
    SOMU_ITEM_CREATED,
    SOMU_ITEM_DELETED,
    SOMU_ITEM_NOT_FOUND,
    SOMU_ITEM_RETRIEVED,
    SOMU_ITEM_UPDATED,
    STAGES_NOT_FOUND,
    STAGE_ASSIGNED_TEAM,
    STAGE_ASSIGNED_USER,
    STAGE_ASSIGNED_USER_FAILURE,
    STAGE_COMPLETED,
    STAGE_CREATED,
    STAGE_CREATE_FAILURE,
    STAGE_DEADLINE_UPDATED,
    STAGE_NOT_FOUND,
    STAGE_RECREATED,
    STAGE_TRANSITION_NOTE_UPDATED,
    TEAMS_STAGE_LIST_EMPTY,
    TEAMS_STAGE_LIST_RETRIEVED,
    TEAM_EMAIL_SENT,
    TOPIC_CREATE_FAILED,
    TOPIC_STANDARD_LINE_CACHE_INVALIDATED,
    UNCAUGHT_EXCEPTION,
    USERS_TEAMS_STAGE_LIST_RETRIEVED,
    USER_EMAIL_SENT;

    public static final String EVENT = "event_id";

    public static final String EXCEPTION = "exception";

    public static final String STACKTRACE = "stacktrace";
}
