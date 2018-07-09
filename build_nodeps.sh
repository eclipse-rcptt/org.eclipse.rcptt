#!/bin/sh
#*******************************************************************************
# Copyright (c) 2009, 2016 Xored Software Inc and others.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
#  
# Contributors:
# 	Xored Software Inc - initial API and implementation and/or initial documentation
#*******************************************************************************
export MAVEN_OPTS="-Xms512m -Xmx1024m -XX:MaxMetaspaceSize=256m -XX:MaxPermSize=256m"

OPTIONS="-Dtycho.localArtifacts=ignore $@"

mvn clean verify -f releng/core/pom.xml $OPTIONS || exit 101
mvn clean verify -f releng/runtime/pom.xml -P runtime3x $OPTIONS || exit 102
mvn clean verify -f releng/runtime/pom.xml -P runtime4x $OPTIONS || exit 103
mvn clean verify -f releng/ide/pom.xml $OPTIONS || exit 105

mvn clean verify -f releng/rap/pom.xml -P core $OPTIONS || exit 110
mvn clean verify -f releng/rap/pom.xml -P ide $OPTIONS || exit 111

mvn clean verify -f releng/rcptt/pom.xml $OPTIONS || exit 106
