{{- define "deployment.envs" }}
- name: JAVA_OPTS
  value: '{{ tpl .Values.app.env.javaOpts . }}'
{{- if not .Values.proxy.enabled }}
- name: SERVER_SSL_KEY_STORE_TYPE
  value: 'PKCS12'
- name: SERVER_SSL_KEY_STORE_PASSWORD
  value: 'changeit'
- name: SERVER_SSL_KEY_STORE
  value: 'file:/etc/keystore/keystore.jks'
- name: SERVER_COMPRESSION_ENABLED
  value: 'true'
- name: SERVER_SSL_ENABLED
  value: 'true'
{{- end }}
- name: SERVER_PORT
  value: '{{ include "hocs-app.port" . }}'
- name: SPRING_PROFILES_ACTIVE
  value: '{{ tpl .Values.app.env.springProfiles . }}'
- name: HOCS_AUDIT_SERVICE
  value: '{{ tpl .Values.app.env.auditService . }}'
- name: HOCS_INFO_SERVICE
  value: '{{ tpl .Values.app.env.infoService . }}'
- name: HOCS_SEARCH_SERVICE
  value: '{{ tpl .Values.app.env.searchService . }}'
- name: HOCS_DOCUMENT_SERVICE
  value: '{{ tpl .Values.app.env.docsService . }}'
- name: MIGRATION_USERID
  value: '{{ tpl .Values.app.env.migrationUserId . }}'
- name: MIGRATION_USERNAME
  value: '{{ tpl .Values.app.env.migrationUserName . }}'
- name: MIGRATION_GROUP
  value: '{{ tpl .Values.app.env.migrationGroup . }}'
- name: HOCS_BASICAUTH
  valueFrom:
    secretKeyRef:
      name: ui-casework-creds
      key: plaintext
- name: DB_HOST
  valueFrom:
    secretKeyRef:
      name: {{ .Release.Namespace }}-casework-rds
      key: host
- name: DB_PORT
  valueFrom:
    secretKeyRef:
      name: {{ .Release.Namespace }}-casework-rds
      key: port
- name: DB_NAME
  valueFrom:
    secretKeyRef:
      name: {{ .Release.Namespace }}-casework-rds
      key: name
- name: DB_SCHEMA_NAME
  valueFrom:
    secretKeyRef:
      name: {{ .Release.Namespace }}-casework-rds
      key: schema_name
- name: DB_USERNAME
  valueFrom:
    secretKeyRef:
      name: {{ .Release.Namespace }}-casework-rds
      key: user_name
- name: DB_PASSWORD
  valueFrom:
    secretKeyRef:
      name: {{ .Release.Namespace }}-casework-rds
      key: password
- name: AWS_SNS_AUDIT_SEARCH_TOPIC_NAME
  value: {{ .Release.Namespace }}-sns
- name: AWS_SNS_AUDIT_SEARCH_ACCOUNT_ACCESS_KEY
  valueFrom:
    secretKeyRef:
      name: {{ .Release.Namespace }}-audit-sqs
      key: access_key_id
- name: AWS_SNS_AUDIT_SEARCH_ACCOUNT_SECRET_KEY
  valueFrom:
    secretKeyRef:
      name: {{ .Release.Namespace }}-audit-sqs
      key: secret_access_key
- name: AWS_SNS_AUDIT_SEARCH_ACCOUNT_ID
  valueFrom:
    configMapKeyRef:
      name: hocs-queue-config
      key: AWS_ACCOUNT_ID
- name: AUDITING_DEPLOYMENT_NAME
  valueFrom:
    fieldRef:
      fieldPath: metadata.name
- name: AUDITING_DEPLOYMENT_NAMESPACE
  valueFrom:
    fieldRef:
      fieldPath: metadata.namespace
- name: AWS_SQS_NOTIFY_URL
  valueFrom:
    secretKeyRef:
      name: {{ .Release.Namespace }}-notify-sqs
      key: sqs_queue_url
- name: AWS_SQS_NOTIFY_ACCOUNT_ACCESS_KEY
  valueFrom:
    secretKeyRef:
      name: {{ .Release.Namespace }}-notify-sqs
      key: access_key_id
- name: AWS_SQS_NOTIFY_ACCOUNT_SECRET_KEY
  valueFrom:
    secretKeyRef:
      name: {{ .Release.Namespace }}-notify-sqs
      key: secret_access_key
{{- end -}}
