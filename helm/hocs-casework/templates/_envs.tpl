{{- define "deployment.envs" }}
- name: JAVA_OPTS
  value: '-XX:MaxRAMPercentage=70 -Djava.security.egd=file:/dev/./urandom -Djavax.net.ssl.trustStore=/etc/keystore/truststore.jks -Dhttps.proxyHost=hocs-outbound-proxy.{{ .Values.namespace }}.svc.cluster.local -Dhttps.proxyPort=31290 -Dhttp.nonProxyHosts=*.{{ .Values.namespace }}.svc.cluster.local'
- name: SERVER_PORT
  value: "{{ .Values.app.port }}"
- name: SPRING_PROFILES_ACTIVE
  value: 'sns, sqs'
- name: HOCS_AUDIT_SERVICE
  value: 'https://hocs-audit.{{ .Values.namespace }}.svc.cluster.local'
- name: HOCS_INFO_SERVICE
  value: 'https://hocs-info-service.{{ .Values.namespace }}.svc.cluster.local'
- name: HOCS_SEARCH_SERVICE
  value: 'https://hocs-search.{{ .Values.namespace }}.svc.cluster.local'
- name: HOCS_DOCUMENT_SERVICE
  value: 'https://hocs-docs.{{ .Values.namespace }}.svc.cluster.local'
- name: HOCS_BASICAUTH
  valueFrom:
    secretKeyRef:
      name: ui-casework-creds
      key: plaintext
- name: DB_HOST
  valueFrom:
    secretKeyRef:
      name: {{ .Values.namespace }}-casework-rds
      key: host
- name: DB_PORT
  valueFrom:
    secretKeyRef:
      name: {{ .Values.namespace }}-casework-rds
      key: port
- name: DB_NAME
  valueFrom:
    secretKeyRef:
      name: {{ .Values.namespace }}-casework-rds
      key: name
- name: DB_SCHEMA_NAME
  valueFrom:
    secretKeyRef:
      name: {{ .Values.namespace }}-casework-rds
      key: schema_name
- name: DB_USERNAME
  valueFrom:
    secretKeyRef:
      name: {{ .Values.namespace }}-casework-rds
      key: user_name
- name: DB_PASSWORD
  valueFrom:
    secretKeyRef:
      name: {{ .Values.namespace }}-casework-rds
      key: password
- name: AWS_SNS_AUDIT_SEARCH_TOPIC_NAME
  value: {{ .Values.namespace }}-sns
- name: AWS_SNS_AUDIT_SEARCH_ACCOUNT_ACCESS_KEY
  valueFrom:
    secretKeyRef:
      name: {{ .Values.namespace }}-audit-sqs
      key: access_key_id
- name: AWS_SNS_AUDIT_SEARCH_ACCOUNT_SECRET_KEY
  valueFrom:
    secretKeyRef:
      name: {{ .Values.namespace }}-audit-sqs
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
      name: {{ .Values.namespace }}-notify-sqs
      key: sqs_queue_url
- name: AWS_SQS_NOTIFY_ACCOUNT_ACCESS_KEY
  valueFrom:
    secretKeyRef:
      name: {{ .Values.namespace }}-notify-sqs
      key: access_key_id
- name: AWS_SQS_NOTIFY_ACCOUNT_SECRET_KEY
  valueFrom:
    secretKeyRef:
      name: {{ .Values.namespace }}-notify-sqs
      key: secret_access_key
{{- end -}}
