#!/usr/bin/env bash
#*******************************************************************************
# Copyright (c) 2009 Xored Software Inc and others.
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v2.0
# which accompanies this distribution, and is available at
# https://www.eclipse.org/legal/epl-v20.html
#
# Contributors:
# 	Xored Software Inc - initial API and implementation and/or initial documentation
#*******************************************************************************

set -xeuo pipefail
export MAVEN_OPTS="-Xms512m -Xmx1024m"

VERSION="$1"
if [ ! "$VERSION" ]; then
    echo "Version is not specified. Please, specify version number in x.y.z format."
    exit 110
fi

VERSION_WITH_DECORATOR="$VERSION-${2:-SNAPSHOT}"

GOAL="org.eclipse.tycho:tycho-versions-plugin:set-version"
OPTIONS=(
    -Dtycho.mode=maven
    -Dtycho.localArtifacts=ignore
    "-DnewVersion=$VERSION_WITH_DECORATOR"
    -DupdateVersionRangeMatchingBounds
    -DgenerateBackupPoms=false
    -B
)

echo "================= Updating All Components ================="
mvn "$GOAL" -f releng/pom.xml -P update-version "${OPTIONS[@]}" || exit 100

echo "================== Updating Maven Plugin =================="
mvn clean install -f maven-plugin/pom.xml || exit 109 # Ensure previous version is available to resolve deps for "its"
mvn "$GOAL" -f maven-plugin/pom.xml "${OPTIONS[@]}" || exit 101
mvn "$GOAL" -f maven-plugin/its/pom.xml "${OPTIONS[@]}" || exit 108

echo "================== Updating Maven Script =================="
mvn versions:set -f clean-pom.xml "${OPTIONS[@]}" || exit 102
mvn clean install -f maven-plugin/pom.xml # Ensure next version is available to resolve rcpttTests

echo "================== Updating RCPTT Tests =================="
mvn versions:use-dep-version -f ./rcpttTests/rcptt_ide/ECL_IDE_module/pom.xml  -Dincludes=com.xored.q7:q7contexts.shared -DdepVersion="$VERSION_WITH_DECORATOR" -DgenerateBackupPoms=false -DforceVersion=true -B || exit 103
mvn versions:set --file ./rcpttTests "${OPTIONS[@]}" || exit 104
mvn versions:set-property --file ./rcpttTests -Dproperty=rcptt-maven-version "${OPTIONS[@]}" || exit 105
mvn versions:set-property --file ./rcpttTests -Dproperty=runner-version "${OPTIONS[@]}" || exit 106
