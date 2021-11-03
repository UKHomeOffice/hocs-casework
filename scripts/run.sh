#!/usr/bin/env bash

exec java ${JAVA_OPTS} -noverify -Dcom.sun.management.jmxremote.local.only=false -Djava.security.egd=file:/dev/./urandom org.springframework.boot.loader.JarLauncher
