#!/bin/sh
#*******************************************************************************
# Copyright (c) 2009, 2020 Xored Software Inc and others.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v2.0
# which accompanies this distribution, and is available at
# https://www.eclipse.org/legal/epl-v20.html
#  
# Contributors:
# 	Xored Software Inc - initial API and implementation and/or initial documentation
#*******************************************************************************
export MAVEN_OPTS="-Xms512m -Xmx1024m -XX:MaxMetaspaceSize=256m"

CLEAN=""
if [ "$1" = "clean" ]; then
    CLEAN="clean"
    shift
fi

OPTIONS="-Ddash.batch=200 -Dtycho.localArtifacts=ignore $@"

mvn $CLEAN verify -f releng/mirroring/pom.xml $OPTIONS || exit 100

./build_nodeps.sh $CLEAN $OPTIONS || exit $?

./build_runner.sh $CLEAN $OPTIONS || exit $?

mvn $CLEAN install --file maven-plugin $OPTIONS || exit $?

mvn $CLEAN verify --file maven-plugin/its $OPTIONS || exit $?
