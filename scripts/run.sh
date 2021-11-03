#!/usr/bin/env bash

NAME=${NAME:-casework}

exec java ${JAVA_OPTS} -Dcom.sun.management.jmxremote.local.only=false -Djava.security.egd=file:/dev/./urandom org.springframework.boot.loader.JarLauncher
