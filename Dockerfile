FROM amazoncorretto:11

ENV USER user_hocs_casework
ENV USER_ID 1000
ENV NAME hocs-casework
ENV JAR_PATH build/libs

RUN yum update -y glibc && \
    yum update -y nss && \
    yum update -y bind-license && \
    yum clean all

WORKDIR /app

CMD /usr/sbin/adduser -r -u ${USER_ID} ${USER} -d /app && \
    mkdir -p /app && \
    chown -R ${USER} /app

COPY ${JAR_PATH}/${NAME}*.jar /app

ADD scripts /app/scripts

RUN chmod a+x /app/scripts/*

EXPOSE 8000

USER ${USER_ID}

CMD /app/scripts/run.sh
