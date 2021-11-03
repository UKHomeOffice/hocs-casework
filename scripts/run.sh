#!/usr/bin/env bash

NAME=${NAME:-casework}

JAR=$(find . -name ${NAME}*.jar|head -1)
exec java ${JAVA_OPTS} -noverify -Djava.security.egd=file:/dev/./urandom -jar "${JAR}"
