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

ENV NAME hocs-casework
ENV JAR_PATH build/libs

COPY ${JAR_PATH}/${NAME}*.jar /app

ADD scripts/run.sh /app/scripts/run.sh

RUN chmod a+x /app/scripts/*

EXPOSE 8080

USER ${USER_ID}

CMD ["sh", "/app/scripts/run.sh"]
