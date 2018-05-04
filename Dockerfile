FROM quay.io/ukhomeofficedigital/openjdk8


ENV USER user_hocs_casework
ENV GROUP group_hocs_casework
ENV NAME hocs-casework
ENV JAR_PATH build/libs

RUN yum update -y glibc && \
    yum update -y nss && \
    yum update -y bind-license

WORKDIR /app

RUN groupadd -r ${GROUP} && \
    useradd -r -g ${GROUP} ${USER} -d /app && \
    mkdir -p /app && \
    chown -R ${USER}:${GROUP} /app

COPY ${JAR_PATH}/${NAME}*.jar /app

ADD scripts /app/scripts

RUN chmod a+x /app/scripts/*

EXPOSE 8000

USER ${USER}

CMD /app/scripts/run.sh
