#!/usr/bin/env bash

helm upgrade ${CHART_NAME} \
 ./helm/${CHART_NAME} \
--atomic \
--cleanup-on-fail \
--install \
--reset-values \
--timeout 3m \
--history-max 3 \
--namespace ${KUBE_NAMESPACE} \
--set version=${VERSION} $*
