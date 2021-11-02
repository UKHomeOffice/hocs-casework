FROM quay.io/ukhomeofficedigital/alpine:v3.13

USER root

RUN apk add openjdk11-jre

WORKDIR /app

ENV USER user_hocs
ENV USER_ID 1000
ENV GROUP group_hocs

RUN addgroup -S ${GROUP} && \
    adduser -S -u ${USER_ID} ${USER} -G ${GROUP} -h /app && \
    mkdir -p /app && \
    chown -R ${USER}:${GROUP} /app

COPY build/libs/hocs-casework.jar /app

ADD scripts /app/scripts

RUN chmod a+x /app/scripts/*

EXPOSE 8080

USER ${USER_ID}

CMD ["sh", "/app/scripts/run.sh"]
