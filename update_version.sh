#!/bin/sh
#*******************************************************************************
# Copyright (c) 2009, 2019 Xored Software Inc and others.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v2.0
# which accompanies this distribution, and is available at
# https://www.eclipse.org/legal/epl-v20.html
#
# Contributors:
# 	Xored Software Inc - initial API and implementation and/or initial documentation
#*******************************************************************************
export MAVEN_OPTS="-Xms512m -Xmx1024m"

VERSION="$1"
if [ ! $VERSION ]; then
    echo "Version is not specified. Please, specify version number in x.y.z format."
    exit 900
fi

DECORATOR="$2"
if [ $DECORATOR ]; then
    VERSION_WITH_DECORATOR="$VERSION.$DECORATOR"
else
    VERSION_WITH_DECORATOR="$VERSION-SNAPSHOT"
fi

GOAL="org.eclipse.tycho:tycho-versions-plugin:set-version"
OPTIONS="-Dtycho.mode=maven -Dtycho.localArtifacts=ignore -DnewVersion=$VERSION_WITH_DECORATOR -DupdateVersionRangeMatchingBounds"

echo "================= Updating All Components ================="
mvn $GOAL -f releng/pom.xml -P update-version $OPTIONS -B || exit 100

echo "================== Updating Maven Plugin =================="
mvn clean install -f maven-plugin/pom.xml || exit 109 # Ensure that previous version is vailable to resolve deps for "its"
mvn $GOAL -f maven-plugin/pom.xml $OPTIONS -B || exit 101
mvn $GOAL -f maven-plugin/its/pom.xml $OPTIONS -B || exit 108

echo "================== Updating Maven Script =================="
mvn versions:set -f clean-pom.xml -DnewVersion=$VERSION_WITH_DECORATOR -DgenerateBackupPoms=false -B || exit 102

echo "================== Updating RCPTT Tests =================="
mvn versions:use-dep-version -f ./rcpttTests/ECL_IDE_module/pom.xml  -Dincludes=com.xored.q7:q7contexts.shared -DdepVersion=$VERSION_WITH_DECORATOR -DgenerateBackupPoms=false -DforceVersion=true -B || exit 103
mvn versions:set -f ./rcpttTests/pom-base.xml -DnewVersion=$VERSION_WITH_DECORATOR -DgenerateBackupPoms=false -B || exit 104
mvn versions:set-property -f ./rcpttTests/pom-base.xml -Dproperty=rcptt-maven-version -DnewVersion=$VERSION_WITH_DECORATOR -DgenerateBackupPoms=false -B || exit 105
mvn versions:set-property -f ./rcpttTests/pom-base.xml -Dproperty=runner-version -DnewVersion=$VERSION_WITH_DECORATOR -DgenerateBackupPoms=false -B || exit 106
mvn versions:set-property -f ./rcpttTests/pom-base.xml -Dproperty=rcpttRepo -DnewVersion="http://download.eclipse.org/rcptt/nightly/$VERSION/latest/repository" -DgenerateBackupPoms=false -B || exit 107
