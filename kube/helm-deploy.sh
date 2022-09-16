#!/usr/bin/env bash

helm repo add hocs-helm-charts https://ukhomeoffice.github.io/hocs-helm-charts

helm dependency update ./helm/${CHART_NAME}

helm upgrade ${CHART_NAME} ./helm/${CHART_NAME} \
--atomic \
--cleanup-on-fail \
--install \
--reset-values \
--timeout 3m \
--history-max 3 \
--namespace ${KUBE_NAMESPACE} \
--set hocs-backend-service.version=${VERSION} $*
