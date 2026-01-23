#/usr/bin/env bash

export DIR=`mktemp -d -t repocheck`
REPOSITORY="$1"
shift

cd "$DIR"
 "${HOME}/Applications/Eclipse Installer.app/Contents/MacOS/eclipse-inst" \
 -application org.eclipse.oomph.p2.core.RepositoryIntegrityAnalyzer \
 -consoleLog \
 -noSplash \
 -o "$DIR/o" \
 -s "$USER" \
 -v \
 -t "$DIR/tests" \
 -p "$DIR/p" \
 "$REPOSITORY" \
 -vmargs \
   -Dfile.encoding=UTF-8 \
   -Dorg.eclipse.emf.ecore.plugin.EcorePlugin.doNotLoadResourcesPlugin=true \
   -Xmx8g