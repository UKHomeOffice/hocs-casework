#!/bin/bash

export KUBE_NAMESPACE=${ENVIRONMENT}
export KUBE_SERVER=${KUBE_SERVER}

if [[ -z ${VERSION} ]] ; then
    export VERSION=${IMAGE_VERSION}
fi

if [[ ${KUBE_NAMESPACE} == *prod ]]
then
    export MIN_REPLICAS="2"
    export MAX_REPLICAS="6"
else
    export MIN_REPLICAS="1"
    export MAX_REPLICAS="2"
fi

if [[ ${KUBE_NAMESPACE} == "cs-prod" ]] ; then
    echo "deploy ${VERSION} to PROD namespace, using HOCS_CASEWORK_PROD_CS drone secret"
    export KUBE_TOKEN=${HOCS_CASEWORK_PROD_CS}
elif [[ ${KUBE_NAMESPACE} == "wcs-prod" ]] ; then
    echo "deploy ${VERSION} to PROD namespace, using HOCS_CASEWORK_PROD_WCS drone secret"
    export KUBE_TOKEN=${HOCS_CASEWORK_PROD_WCS}
elif [[ ${KUBE_NAMESPACE} == "cs-qa" ]] ; then
    echo "deploy ${VERSION} to QA namespace, using HOCS_CASEWORK_QA_CS drone secret"
    export KUBE_TOKEN=${HOCS_CASEWORK_QA_CS}
elif [[ ${KUBE_NAMESPACE} == "wcs-qa" ]] ; then
    echo "deploy ${VERSION} to QA namespace, using HOCS_CASEWORK_QA_WCS drone secret"
    export KUBE_TOKEN=${HOCS_CASEWORK_QA_WCS}
elif [[ ${KUBE_NAMESPACE} == "cs-demo" ]] ; then
    echo "deploy ${VERSION} to DEMO namespace, using HOCS_CASEWORK_DEMO_CS drone secret"
    export KUBE_TOKEN=${HOCS_CASEWORK_DEMO_CS}
elif [[ ${KUBE_NAMESPACE} == "wcs-demo" ]] ; then
    echo "deploy ${VERSION} to DEMO namespace, using HOCS_CASEWORK_DEMO_WCS drone secret"
    export KUBE_TOKEN=${HOCS_CASEWORK_DEMO_WCS}
elif [[ ${KUBE_NAMESPACE} == "cs-dev" ]] ; then
    echo "deploy ${VERSION} to DEV namespace, using HOCS_CASEWORK_DEV_CS drone secret"
    export KUBE_TOKEN=${HOCS_CASEWORK_DEV_CS}
elif [[ ${KUBE_NAMESPACE} == "wcs-dev" ]] ; then
    echo "deploy ${VERSION} to DEV namespace, using HOCS_CASEWORK_DEV_WCS drone secret"
    export KUBE_TOKEN=${HOCS_CASEWORK_DEV_WCS}
else
    echo "Unable to find environment: ${ENVIRONMENT}"
fi

if [[ -z ${KUBE_TOKEN} ]] ; then
    echo "Failed to find a value for KUBE_TOKEN - exiting"
    exit -1
fi

cd kd

kd --insecure-skip-tls-verify \
   --timeout 10m \
    -f deployment.yaml \
    -f service.yaml \
    -f autoscale.yaml
